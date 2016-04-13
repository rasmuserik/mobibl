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
  :route (fn [db [_ page id]]
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
  :ui (fn [db [_ id value]]
          (assoc-in db [:ui id] value)))

(register-handler
  :latest-work
  (fn [db [_ id]]
    (let [work-history (get-in db [:work-history] [])
          work-history (into [id] (remove #(= % id) work-history))]
      (assoc db :work-history work-history))))
;; ## Subscriptions

(def default-work
  {:title "Unknown Title"
   :creator "Unknown Creator"})

(defn get-work [db id]
  (let [work (get-in db [:works id])]
    (when-not work (dispatch [:request-work id]))
    (merge default-work {:id id} work)))

(register-sub
  :work-history
  (fn [db _] (reaction (get @db :work-history []))))

(register-sub
  :ui
  (fn [db [_ path]] (reaction (get-in @db [:ui path]))))

(register-sub
  :search-history
  (fn [db _] (reaction
               ["filosofi"
                "ost"
                "Harry Potter"
                "hest"])))


(register-sub
  :search
  (fn [db [_ q page]]
    (reaction
      (let [results (get-in @db [:search q page])]
        (or results
           (do
            (dispatch [:request-search q page])
            []))))))

(def sample-lib
  {
   ;; ## Known libraries
   ;;
   ;; Simple representation of the libraries that are interesting for the user
   ;; including position on a map, opening hours and contact information.
   ;;
   ;; FIXME The above books in the works section are from different libraries,
   ;; ie. 775100 is Aarhus hovedbibliotek.
   ;;
   :library
   {"710100"
    {:name "Københavns Hovedbibliotek"
     :type "Folkebibliotek"
     :address
     {:road "Krystalgade 15"
      :city "1172 København K"
      :country "Danmark"}
     :email "bibliotek@kff.kk.dk"
     :phone {:number "33663000"
             :time "man-fre 10-17"}
     :position
     [55.680887 12.573619]
     :hours
     [{:title "Åbningstider"
       :weekdays
       [[8 22]
        [8 20]
        [8 20]
        [8 20]
        [8 19]
        [8 17]]}
      {:title "Betjening"
       :weekdays
       [[12 17]
        [12 17]
        [12 17]
        [12 17]
        [12 17]
        [12 15]]}]}
    "810010"
    {:name "Det Kongelige Bibliotek, Diamanten"
     :type "Forskningsbibliotek"
     :address {
               :road "Søren Kierkegaards Plads 1"
               :city "1221 København K"
               :country "Danmark"}
     :email "kb@kb.dk"
     :phone {:number "33 47 47 47"
             :time "man - fre 9-16"}
     :position
     [55.67321579999999 12.5821264]
     ;;
     ;; FIXME How to represent many opening hours for departments of a library
     ;;
     ;; At the Diamanten library there are a lot of possible access times, so
     ;; many are included here to see if it is possible to represent them in a
     ;; manageable way on a mobile app.
     ;;
     ;; The user could also be presented for a way to selecting which department
     ;; of the library to view opening hours for.
     ;;
     :hours
     [{:title "Adgang til Diamanten"
       :weekdays
       [[8 22]
        [8 22]
        [8 22]
        [8 22]
        [8 22]
        [8 22]]}
      {:title "Informationen"
       :weekdays
       [[8 21]
        [8 21]
        [8 21]
        [8 21]
        [8 21]]}
      {:title "Helpdesk"
       :weekdays
       [[10 16]
        [10 16]
        [10 16]
        [10 16]
        [10 16]]}
      {:title "Læsesal Vest og E-Vest"
       :weekdays
       [[9 21]
        [9 21]
        [9 21]
        [9 21]
        [9 21]
        [10 17]]}
      {:title "Læsesal Nord og Øst"
       :weekdays
       [[8 21]
        [8 21]
        [8 21]
        [8 21]
        [8 21]
        [10 17]]}
      {:title "Center for Kort og Billeder"
       :weekdays
       [nil
        [10 16]
        [12 16]
        [10 16]]}
      {:title "Center for Manuskripter og Boghistorie"
       :weekdays
       [[10 17]
        [10 17]
        [12 19]
        [10 17]
        [10 17]]}
      {:title "Småtrykssamlingens interne læsesal"
       :weekdays
       [[10 15]
        [10 15]
        [10 15]
        [10 15]
        [10 15]]}
      {:title "Dansk Folkemindesamling"
       :weekdays
       [nil
        [10 15]
        [10 15]
        [10 15]
        [10 15]]}]}}})

(register-sub
 :current-library (fn [db] (reaction
                            (get-in sample-lib [:library "710100"]))))

(register-sub :work (fn [db [_ id]] (reaction (get-work @db id))))
(register-sub :works (fn [db] (reaction (:works @db))))
(register-sub :route (fn [db] (reaction (get @db :route))))
(register-sub :db (fn [db] (reaction @db)))

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
