;; # Actual application logic (`mobibl.cljs`)
;;
(ns solsort.mobibl.mobibl
  (:require-macros
    [cljs.core.async.macros :refer [go go-loop alt!]]
    [reagent.ratom :as ratom :refer  [reaction]])
  (:require
    [solsort.util
     :refer
     [<ajax <seq<! js-seq normalize-css load-style! put!close!
      parse-json-or-nil log page-ready render dom->clj]]
    [reagent.core :as reagent :refer []]
    [re-frame.core :as re-frame
     :refer [register-sub subscribe register-handler dispatch dispatch-sync]]
    [clojure.string :as string :refer [replace split blank?]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]
    [solsort.mobibl.mock-data :refer [sample-db]]))

;; ## Handlers

;; When the application loads we set the data for use in the frontend by
;; with the :reset-db handler.  See #36
(register-handler :reset-db (fn [_ [_ db]] db))
(register-handler :open (fn [db [_ route]] (assoc db :route route)))

;; Initialise the database with sample data

(dispatch [:reset-db sample-db])

;; ## Subscriptions

(register-sub
  :route (fn [db] (reaction (get @db :route))))
(register-sub
  :current-library (fn [db] (reaction
                             (get-in @db [:library 
                                          (get-in @db [:current :library])]))))
(register-sub
  :current-work (fn [db] (reaction (get-in @db [:current :work]))))
(register-sub
  :current-query (fn [db] (reaction (get-in @db [:current :query]))))
(register-sub
  :work (fn [db [_ ting-id]] (reaction (get-in @db [:works ting-id] {}))))

;;
;; This work will serve as a default if we ever run into something not existing
;;
(def unknown-work {:title "Unknown Title"
                   :creator "Unknown Creator"
                   :id "Unknown-id"})

;;
;; Helper function to query the db for the full info about works
;;
(defn get-status-works [db prop]
  (let [status-obj (get-in db [:status prop])
        res (for [so status-obj]
              (merge so (get-in db [:works (:id so)] unknown-work)))]
    res))

(register-sub :reservations
              (fn [db _] (reaction (get-status-works @db :reservations))))
(register-sub :arrived
              (fn [db _] (reaction (get-status-works @db :arrived))))
(register-sub :borrowed
              (fn [db _] (reaction (get-status-works @db :borrowed))))
