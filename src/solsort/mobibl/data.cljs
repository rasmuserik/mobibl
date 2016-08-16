(ns solsort.mobibl.data
  (:require-macros
    [cljs.core.async.macros :refer [go go-loop alt!]]
    [reagent.ratom :as ratom :refer  [reaction run!]])
  (:require
    [solsort.util
     :refer
     [<p <load-script throttle <ajax <seq<! js-seq normalize-css load-style! put!close!
      parse-json-or-nil log page-ready render dom->clj]]
    [clojure.walk :refer [keywordize-keys]]
    [reagent.core :as reagent :refer []]
    [clojure.data]
    [solsort.appdb :refer [db db!]]
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
    (js->clj (<! (<p  (.call (aget js/dbcOpenPlatform (name endpoint)) js/dbcOpenPlatform (clj->js o)) )))))

(defn get-work [pid]
  (when-not (db [:work pid]) (db! [:work pid] {}))
  (db [:work pid]))

(def view-fields ["dcTitle" "creator" "coverUrlFull" "subjectDBCF" "description" "language"])
(defn load-work [id]
  (go
    (db! [:work id :status-work] :loading)
    (let [[o] (<! (<op :work {:pids [id]}))
          o (into o
                  {:title (first (o "dcTitle"))
                   :creator (first (o "creator"))
                   :keywords (get o "subjectDBCF" [])
                   :related []
                   :description (first (o "description"))
                   :location nil-iter
                   :status-work :loaded
                   :language (first (o "language"))})]
      (db! [:work id] (into o (db [:work id]))))))
 
(defn load-cover [id]
  (go
    (log 'load-cover id)
    (db! [:work id :cover-url] "assets/loading")
    (db! [:work id :cover-url]
          (first
           (get (first (<! (<op :work {:pids [id] :fields ["coverUrlFull"]})))
                 "coverUrlFull"
                 ["assets/nocover"])))
    (log 'loaded-cover id (db [:work id]))
    ))
(defn key2? [k] #(get (second %) k))
(defn sync []
  (doall (map load-work (keys (remove (key2? :status-work) (db [:work])))))
  (doall (map load-cover (keys (remove (key2? :cover-url) (db [:work])))))
  )

(def throttled-sync (throttle sync 1000))
(defonce -runner (run! (db) (throttled-sync)))
(log 'here)
(sync)
(throttled-sync)
