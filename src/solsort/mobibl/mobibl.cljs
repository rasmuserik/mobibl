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
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

;; ## Handlers

(register-handler
  :open (fn [db [_ [page id]]]
          (let [id (or id (get-in db [:current page]))]
            (-> db
                (assoc-in [:current page] id)
                (assoc :route [page id])))))
(register-handler
  :work (fn [db [_ id content]]
          (assoc-in db [:works id]
                    (merge (get-in db [:work id] {})
                           content))))

(register-handler
  :search-query
  (fn [db [_ q]] (assoc-in db [:forms :search-query] q)))

;; ## Subscriptions

(def default-work
  {:title "Unknown Title"
   :creator "Unknown Creator"})

(defn get-work [db id]
  (let [work (get-in db [:works id])]
    (when-not work (dispatch [:request-work id]))
    (merge default-work {:id id} work)))

(register-sub :work (fn [db [_ id]] (reaction (get-work @db id))))
(register-sub :works (fn [db] (reaction (:works @db))))
(register-sub :route (fn [db] (reaction (get @db :route))))
(register-sub :db (fn [db] (reaction @db)))

(register-sub
  :search-query
  (fn [db [_ id]] (reaction (get-in @db [:forms :search-query]))))

;;
;; Helper function to query the db for the full info about works
;;
(defn get-status-works [db prop]
  (let [status-obj (get-in db [:status prop])
        res (for [so status-obj]
              (merge so (get-work db (:id so))))]
    res))

(register-sub :reservations
              (fn [db _] (reaction (get-status-works @db :reservations))))
(register-sub :arrived
              (fn [db _] (reaction (get-status-works @db :arrived))))
(register-sub :borrowed
              (fn [db _] (reaction (get-status-works @db :borrowed))))

;; ## Data initialisation
;;
;; TODO: also run on network reconnect, and after a while
;;
(dispatch [:request-status])


;; TODO: sync database to disk, and restore on load
