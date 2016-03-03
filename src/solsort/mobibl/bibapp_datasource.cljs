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

(defn handle-change [works]
  (let [needs-update 
    (->> works
        (seq)
        (filter
          (fn [[id {status :status}]]
            (and (:requested status) (not (:sent status)))))
        (log)
        ; TODO: send request
        ; TODO: dispatch-sync sent-timestamp
        )]
    
    )
  (js/console.log (clj->js works)))
(defonce start (run! (handle-change @(subscribe [:works]))))
