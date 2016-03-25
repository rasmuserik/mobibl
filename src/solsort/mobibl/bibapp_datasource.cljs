;; # BibApp Data Source (bibapp_datasource.cljs)
;;
(ns solsort.mobibl.bibapp-datasource
  (:require-macros
    [cljs.core.async.macros :refer [go go-loop alt!]]
    [reagent.ratom :as ratom :refer  [reaction run!]])
  (:require
    [solsort.util
     :refer
     [<ajax <seq<! js-seq normalize-css load-style! put!close!
      parse-json-or-nil log page-ready render dom->clj]]
    [clojure.walk :refer [keywordize-keys]]
    [reagent.core :as reagent :refer []]
    [clojure.data]
    [re-frame.core :as re-frame
     :refer [register-sub subscribe register-handler dispatch dispatch-sync]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

;; ## Dummy ids of books etc. with metadata

(def dummy-ids
  ["870970-basis:29820031" "870970-basis:45231402" "870970-basis:29146004"
   "870970-basis:28794630" "870970-basis:28904061" "870970-basis:45574881"
   "870970-basis:51604288" "870970-basis:44351641" "870970-basis:45470075"
   "870970-basis:27697917" "870970-basis:22324284" "870970-basis:28452551"
   "810010-katalog:008471560" "870970-basis:44741830" "870970-basis:28534698"
   "870970-basis:45583457" "870970-basis:45386864" "870970-basis:45421716"
   "870970-basis:28052472" "870970-basis:45493016" "870970-basis:44291738"
   "870970-basis:23060132" "810010-katalog:007071351" "870970-basis:45554813"
   "870970-basis:45237648" "870970-basis:28407513" "870970-basis:44950723"
   "830380-katalog:93161505" "870970-basis:27006434" "870970-basis:45618765"
   "870970-basis:26666074" "870970-basis:44695634" "870970-basis:27455344"
   "870970-basis:50914968" "870970-basis:45170306" "870970-basis:45233758"
   "870970-basis:29706328" "870970-basis:51582772" "870970-basis:45199088"
   "870970-basis:27880436" "870970-basis:29991537" "870970-basis:44313235"
   "870970-basis:23116642" "870970-basis:45233332" "870970-basis:44547759"
   "870970-basis:44910888" "870970-basis:51313380" "870970-basis:44887509"
   "870970-basis:26829798" "870970-basis:45005801" "870970-basis:25893018"
   "870970-basis:44364999" "870970-basis:44331225" "870970-basis:50625656"
   "870970-basis:45534952" "870970-basis:44591413" "870970-basis:44592045"
   "870970-basis:28522517" "870970-basis:29100160" "870970-basis:26396417"
   "870970-basis:50565858" "870970-basis:28930240" "870970-basis:28108990"
   "870970-basis:27195105" "870970-basis:28372531" "870970-basis:44831562"
   "870970-basis:50520846" "870970-basis:45182266" "870970-basis:29158746"
   "870970-basis:43917579" "870970-basis:45217345" "870970-basis:45263762"
   "870970-basis:50909794" "810010-katalog:007144163" "870970-basis:26952425"
   "870970-basis:27873251" "870970-basis:45350568" "870970-basis:44850001"
   "870970-basis:44520028" "870970-basis:44150484" "870970-basis:27561527"
   "870970-basis:27867138" "870970-basis:28539290" "870970-basis:45153843"
   "870970-basis:29287341" "870970-basis:26681316" "870970-basis:45281434"
   "870970-basis:28715730" "870970-basis:45300439" "870970-basis:45575969"
   "870970-basis:27374859" "820010-katalog:3096314" "870970-basis:26509904"
   "870970-basis:44406365" "870970-basis:44623234" "870970-basis:44973650"
   "870970-basis:44537052" "870970-basis:51283708" "870970-basis:45377458"
   "870970-basis:28009011" "870970-basis:45076261" "870970-basis:27165435"
   "870970-basis:24232123" "870970-basis:45164683" "870970-basis:44529807"])

;; ## Get work metadata from database
;;
;; Use data from http://solsort.com/db/bib

(register-handler
  :request-work
  (fn [db [_ id]]
    (when-not (get-in db [:requested id] false)
      (go
        (let
          [o (<! (<ajax (str "https://solsort.com/db/bib/" id)))
           isbn (str (first (o "isbn")))
           result
           {:title (first (o "title"))
            :creator (first (o "creator"))
            :cover-url (str
                         "http://www.bogpriser.dk/Covers/"
                         (.slice isbn -3) "/" isbn ".jpg")
            :keywords (get o "subject" [])
            :description (first (o "description"))
            :location nil
            :language (first (o "language"))
            }]
          ;(log 'loaded id o result)
          (dispatch [:work id result])
          )))
    (assoc-in db  [:requested id] true)))

(register-handler
  :request-search
  (fn [db [_ query page]]
    (if (get-in db [:search query page])
      db
      (assoc-in db [:search query page] 
                (take (js/Math.round (* (js/Math.random) 
                                        (js/Math.random) 
                                        100))
                      (shuffle dummy-ids))))))

;; ## Dummy data for status

(defn reservation [id]
  {:id id
   :until "2016-03-12" })
(defn arrived [id]
  {:id id
   :until "2016-03-12" })
(defn borrowed [id]
  {:id id
   :until "2016-03-25" })

(register-handler
  :request-status
  (fn [db _]
    (assoc db :status
           {:reservations
            (map reservation (take 5 (shuffle dummy-ids)))
            :arrived
            (map arrived (take 5 (shuffle dummy-ids)))
            :borrowed
            (map borrowed (take 5 (shuffle dummy-ids)))

            })))
