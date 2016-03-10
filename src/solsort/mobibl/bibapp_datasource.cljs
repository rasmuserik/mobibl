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
    [reagent.core :as reagent :refer []]
    [clojure.data]
    [re-frame.core :as re-frame
     :refer [register-sub subscribe register-handler dispatch dispatch-sync]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

; http://solsort.com/es/bibapp/ting/_search?q=ost
; http://solsort.com/db/bib/870970-basis:44751143
(defn handle-change [works]
  (let [[id work] (->> works
                       (seq)
                       (filter
                         (fn [[id {status :status}]]
                           (and (:requested status) (not (:sent status)))))
                       (first))]

    (when id
      (dispatch-sync [:work-request-in-progress id])
      (go 
        (log 'req
             (<! (<ajax (str "https://solsort.com/db/bib/" id))))
        ))
    )
  (js/console.log (clj->js works)))
(defonce start (run! (handle-change @(subscribe [:works]))))
