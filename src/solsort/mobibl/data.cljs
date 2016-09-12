(ns solsort.mobibl.data
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop alt!]]
   [solsort.toolbox.macros :refer [<?]]
   [reagent.ratom :as ratom :refer  [reaction run!]])
  (:require
   [solsort.util
    :refer
    [<p <load-script <ajax <seq<! js-seq normalize-css load-style! put!close!
     parse-json-or-nil log page-ready render dom->clj]]
   [solsort.toolbox.misc :refer [throttle]]
   [clojure.walk :refer [keywordize-keys]]
   [reagent.core :as reagent :refer []]
   [clojure.data]
   [solsort.toolbox.appdb :refer [db db! db-async!]]
   [re-frame.core :as re-frame
    :refer [register-sub subscribe register-handler dispatch dispatch-sync]]
   [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))
(defonce clid "a36227da-e477-491e-b4a2-ccd9df365cf9")
(defonce clsec "YfO7hc8OJ+vUGh9GhMZhJw06cyHxNi48fwWnVLJGPrPHvkZaYYj0cboM")
(defn <op [endpoint o]
  (go
    (when-not js/window.dbcOpenPlatform
      (<! (<load-script "https://openplatform.dbc.dk/v1/dbc_openplatform.min.js")))
    (when-not (js/dbcOpenPlatform.connected)
      (<! (<p (js/dbcOpenPlatform.connect clid clsec))))
    (js->clj (<! (<p  (.call (aget js/dbcOpenPlatform (name endpoint)) js/dbcOpenPlatform (clj->js o)))))))

(defn <load-user []
  (go (db! [:user] (try (<? (<op :user {}))
                        (catch js/Error e nil)))))
(def load-user (throttle <load-user 2000))
(defn get-user []
  (load-user)
  (db [:user]))

(defn do-login []
  (go
    (db! [:login :progress] true)
    (db! [:login :library] (db [:route :library]))
    (try
      (<? (<p (js/dbcOpenPlatform.connect
               clid clsec
               (str (db [:login :user] "") "@"
                    (get (db [:libraries (db [:route :library])]) "agencyId"))
               (db [:login :pin]))))
      (catch js/Error e
        (<! (<p (js/dbcOpenPlatform.connect clid clsec)))))
    (<! (<load-user))
    (db! [:login :progress] nil)))
(defn do-order [pids]
  (go
    (<! (<op :order {:pids pids :library (db [:login :library]) :expires (str (+ (.getFullYear (js/Date.)) 2) "-01-01")}))
    (<! (timeout 5000))
    (db! [:route :page] "status")))

(db! [:search ["" 0]] [])
(db! [:search [nil nil]] [])
(def facets ["subjectDBC0" "subjectDBCF" "subjectDBCN" "subjectDBCS" "subjectDK5Text" "subjectGenre" "language" "type" "creator" "contributor" "audience" "temporalDBCP"])
(defn dkabm->data [o]
  (letfn [(g [key] (first (get o key)))]
    (into o
          {:pid (g "pid")
           :title (g "dcTitle")
           :cover-url (:cover-url o)
           :creator (g "creator")
           :keywords (apply concat (for [facet facets] (get o facet [])))
           :location nil
           :year (g "date")
           :description (or (g "abstract") (g "description"))
           :language (g "language")})))

(def nils [nil nil nil nil nil nil nil nil nil nil])
(defn libraries []
  (db [:libraries] {}))
(defn- <suggester [s type]
  (go
    (concat (map #(assoc (clojure.walk/keywordize-keys %)
                         :type type)
                 (let [suggestions  (<! (<op :suggest {:q s :type type}))]
                   (if (coll? suggestions) suggestions nil))) nils)))
(defn get-suggest [s]
  (when-not (db [:suggest s])
    (db! [:suggest s] {:title nils :subject nils :creator nils})
    (when-not (= "" s)
      (go (db! [:suggest s :title] (<! (<suggester s "title"))))
      (go (db! [:suggest s :subject] (<! (<suggester s "subject"))))
      (go (db! [:suggest s :creator] (<! (<suggester s "creator"))))))
  (db [:suggest s]))
(defn get-work [pid]
  (when-not (db [:work pid]) (db! [:work pid] {}))
  (dkabm->data (db [:work pid])))
(defn get-search [q page]
  (let [results (db [:search [q page]])]
    (when-not results (db! [:search-request] [q page]))
    results))

(defn get-facets [q]
  (db [:facets q]))

(def view-fields ["dcTitle" "creator" "coverUrlFull" "subjectDBCF" "description" "language"])
(defn load-work [id]
  (go
    (db! [:work id :status-work] :loading)
    (let [[o] (<! (<op :work {:pids [id]}))
          o (into o {:status-work :loaded})]
      (db! [:work id] (into (db [:work id] {}) o)))))

(defn load-cover [id]
  (go
    (db! [:work id :cover-url] "assets/loading.png")
    (db! [:work id :cover-url]
         (first
          (get (first (<! (<op :work {:pids [id] :fields ["coverUrlFull"]})))
               "coverUrlFull"
               ["assets/no-cover.png"])))))
(defn transform-facets [facets]
  (reverse
   (sort-by
    :frequency
    (apply
     concat
     (for [[k v] facets]
       (for [facet v]
         (assoc (clojure.walk/keywordize-keys facet) :type k)))))))

(defn load-facets [q]
  (when-not (empty? q)
    (go
      (db! [:facets q] [])
      (db! [:facets q]
           (transform-facets
            (<? (<op :facets
                     {:fields ["creator" "subject" "language"
                               "form" "date" "audience" "period"
                               "type" "titleSeries" "partOf"]
                      :q q
                      :limit 20})))))))
(defn load-search [[q page]]
  (go
    (db! [:search [q page]] [])
    (let [results (<! (<op :search {:fields ["pid" "collection" "collectionDetails"] :q q :limit 10 :offset (* 10 page)}))]
      (when (coll? results)
        (db! [:search [q page]] (map #(first (get % "pid")) results))
        (doall
         (for [result results]
           (let [works (get result "collectionDetails")
                 collection (get result "collection")]
             (doall
              (for [work works]
                (let [pid (first (get work "pid"))]
                  (db! [:work pid] work)))))))))))
(defn load-libraries []
  (go
    (db! [:libraries]
         (into
          {}
          (map
           (fn [o] [(get o "branchId") o])
           (filter #(and
                     (get % "geolocation")
                     (= "Folkebibliotek" (get % "agencyType")))
                   (<! (<op :libraries {}))))))))

(defn needs-search [[q results]]
  (< (get results :wanted -1) (get results :requested -1)))
(defn key2? [k] #(not (nil? (get (second %) k))))

(defn sync []
  (doall (map load-work (keys (remove (key2? :status-work) (db [:work])))))
  (doall (map load-cover (keys (remove (key2? :cover-url) (db [:work])))))
  (when (empty? (db [:libraries]))
    (load-libraries))
  (when-not (db [:facets (first (db [:search-request]))])
    (load-facets (first (db [:search-request]))))
  (when-not (db [:search (db [:search-request])])
    (load-search (db [:search-request]))))

(def throttled-sync (throttle sync 1000))
(defonce -runner (run! (db) (throttled-sync)))
(sync)
(throttled-sync)
