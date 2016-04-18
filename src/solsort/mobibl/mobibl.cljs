;; # Core application logic and database (`mobibl.cljs`)
;;
;; This file contain the platform independent logic of the app.
;;
;; ## Documentation of data structure
;;
;; The app-db is the core of the application
;; and should have the following structure:
;;
;; - `:ui` everything related to the ui
;; - `:route` TODO everything about routing and history
;; - `:backend` everything specific to certain backends (currently `requested`)
;; - `:data` TODO application specific data:
;;     - `:libraries` information about the libraries
;;     - `:search :results` results of search queries
;;     - `:facets` TODO facets for searches
;;     - `:user` user status (currently `:status`)
;;     - `:works` information about specific works
;;
;; ### Data backend
;;
;; A data backend should supply the following handlers
;;
;; - `:request-work work-id`
;; - `:request-facets query`
;; - `:request-search query`
;; - `:request-suggest query`
;; - `:request-library library-id`
;; - `:request-status`
;; - ... login, order
;;     - `:order` ...
;;     - `:login` ...
;;     - `:request-work-details` ...
;;
;; And it should automatically load the list of libraries.
;;
;; ## General dependencies and setup

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
(declare get-work default-work sample-lib)

;; DEBUG: uncomment this to print db on reload

;(register-sub :db (fn [db] (reaction @db)))
;(log 'db @(subscribe [:db]))

;; ## Routing and history
;;
;;
(register-sub :route (fn [db] (reaction (get-in @db [:route :path] [:search ""]))))

(register-sub :work-history (fn [db _] (reaction (get @db :work-history []))))
(register-sub
  :search-history
  (fn [db _] (reaction
               [["filosofi" [[:creator "plato"] [:creator "socrates"]]]
                ["ost" []]
                ["Harry Potter" [[:type "dvd"] [:type "bog"]]]
                ["hest" [[:year "2001"]]]])))

(register-handler
  :route (fn [db [_ page id prevPageScroll]]
           (let [[prevPage prevId _] (get-in db [:route :path])
                 [id scroll] (if id
                               [id 0]
                               (get-in db [:current page]))]
             (dispatch [:scroll scroll])
             (-> db
                 (assoc-in [:current prevPage] [prevId prevPageScroll])
                 (assoc-in [:current page] [id scroll])
                 (assoc-in [:route :path] [page id])))))

(register-handler
  :latest-work
  (fn [db [_ id]]
    (let [work-history (get-in db [:work-history] [])
          work-history (into [id] (remove #(= % id) work-history))]
      (assoc db :work-history work-history))))
;; ## Work
;;
(register-sub :work (fn [db [_ id]] (reaction (get-work @db id))))
(register-sub :works (fn [db] (reaction (:works @db))))

(register-handler
  :work (fn [db [_ id content]]
          (assoc-in db [:works id]
                    (merge (get-in db [:work id] {})
                           content))))

(defn get-work [db id]
  (let [work (get-in db [:works id])]
    (when-not work (dispatch [:request-work id]))
    (merge default-work {:id id} work)))
(def default-work {:title "Unknown Title" :creator "Unknown Creator"})


;; ## Search

(register-sub
  :search
  (fn [db [_ q page]]
    (reaction (let [results (get-in @db [:search q page])]
                (or results
                    (do (dispatch [:request-search q page]) []))))))

;; ## UI

(register-handler
  :ui (fn [db [_ id value]]
        (assoc-in db [:ui id] value)))

(register-sub :ui (fn [db [_ path]] (reaction (get-in @db [:ui path]))))
(register-handler
  :add-facet (fn [db [_ facet]]
               (assoc-in db [:ui :facets]
                         (cons facet (get-in db [:ui :facets] [])))))
(register-handler
  :remove-facet (fn [db [_ facet]]
                  (assoc-in db [:ui :facets]
                            (remove #{facet} (get-in db [:ui :facets] [])))))
(register-sub :facets (fn [db [_ path]] (reaction (get-in @db [:ui :facets]))))

;; TODO this is HTML5-specific, so should probably be moved into html5.cljs
(register-handler
  :scroll (fn [db [_ scroll]]
            (js/setTimeout
              #(set! js/document.body.scrollTop (or scroll 0)) 100)
            db))

;; ## Libraries

(register-sub
  :current-library (fn [db] (reaction
                              (get-in @db [:libraries "710100"]))))
(register-sub
  :libraries (fn [db] (reaction (get-in @db [:libraries :all] []))))
(register-sub
  :library (fn [db [_ id]] (reaction (get-in @db [:libraries id] {}))))

(register-handler
  :library
  (fn [db [_ library]] (assoc-in db [:libraries (:id library)] library)))

(register-handler
  :libraries
  (fn [db [_ libraries]] (assoc-in db [:libraries :all] libraries)))

;; ### Sample library

(dispatch-sync [:library
   ;; Simple representation of the libraries that are interesting for the user
   ;; including position on a map, opening hours and contact information.
   ;;
   ;; FIXME The above books in the works section are from different libraries,
   ;; ie. 775100 is Aarhus hovedbibliotek.
   ;;
    {:id "710100"
     :name "Københavns Hovedbibliotek"
     :address
     {:road "Krystalgade 15"
      :city "1172 København K"
      :country "Danmark"}
     :email "bibliotek@kff.kk.dk"
     :phone {:number "33663000"
             :time "man-fre 10-17"}
     :position [55.680887 12.573619]
     ;; FIXME How to represent many opening hours for departments of a library?
     ;; - these are delivered as a &lt;pre&gt;-formatted data
     :hours
     [{:title "Åbningstider"
       :weekdays [[8 22] [8 20] [8 20] [8 20] [8 19] [8 17]]}
      {:title "Betjening"
       :weekdays [[12 17] [12 17] [12 17] [12 17] [12 17] [12 15]]}]}
    ])

;; ## User status

(defn get-status-works
  "Helper function to query the db for the full info about works"
  [db prop]
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

;; TODO: also run on network reconnect, and after a while
(dispatch [:request-status])
