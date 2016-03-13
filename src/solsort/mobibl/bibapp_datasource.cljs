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

; http://solsort.com/es/bibapp/ting/_search?q=ost
; http://solsort.com/db/bib/870970-basis:44751143
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
