;; # mobibl.cljs
;;
(ns solsort.mobibl.mobibl
  (:require-macros
    [cljs.core.async.macros :refer [go go-loop alt!]]
    [reagent.ratom :as ratom :refer  [reaction]])
  (:require
    [solsort.util
     :refer
     [<ajax <seq<! js-seq normalize-css load-style! put!close! parse-json-or-nil log page-ready render
      dom->clj]]
    [reagent.core :as reagent :refer []]
    [re-frame.core :as re-frame :refer  [register-sub subscribe register-handler dispatch dispatch-sync]]
    [clojure.string :as string :refer [replace split blank?]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

;; # Utility functions
;;
;; To be extracted into utility library.

; generic code here

;; # DB
;;
;; As we are starting out implementing the views, we just have dummy data here so far.

(def sample-db
  {:route ["work" "870970-basis:28934297"]
   :current
   {:search-query "Murakami"
    :work "870970-basis:28934297"
    :library "Københavns Hovedbibliotek"}
   :works
   {"870970-basis:28934297"
    {:title "1Q84"
     :creator "Haruki Murakami"
     :cover-url "http://www.bogpriser.dk/Covers/202/9788779559202.jpg"
     :description "Aomame er en 30-årig smart pige, uddannet kampsportsinstruktør, men arbejder p.t. som lejemorder. Tengo er matematiklærer med forfatterdrømme. Han skal omskrive en sær 17-årig piges sære historie. Begge hovedfigurer oplever, at deres virkelighed forvrides let, hvad påvirker deres virkelighed?"
     :keywords ["Kultur" "Kærlighed" "Magisk Realisme" "Magt" "Parallelle Verdener" "Skrivekunst" "Japan" "1980-1989"]
     :location "Skønlitteratur"
     :language "Dansk"
     :editions
     [{:name "Bog (bind 1)" :availability :available}
      {:name "Bog (bind 2)" :availability :loaned}
      {:name "Bog (bind 3)" :availability :available}
      {:name "Lydbog (cd) (bind 1)" :availability :available}
      {:name "Lydbog (cd) (bind 2)" :availability :available}
      {:name "Lydbog (cd) (bind 3)" :availability :available}
      {:name "Lydbog (online) (bind 1)" :availability :available}
      {:name "Lydbog (online) (bind 2)" :availability :available}
      {:name "Lydbog (online) (bind 3)" :availability :available}]}}
   })
(dispatch [:reset-db sample-db])

;; # Handlers

(register-handler :reset-db (fn [_ [_ db]] db))
(register-handler :route (fn [db [_ route]] (assoc db :route route)))

;; # Subscriptions

(register-sub :route (fn [db] (reaction (get @db :route))))
(register-sub :current-library (fn [db] (reaction (get-in @db [:current :library]))))
(register-sub :current-work (fn [db] (reaction (get-in @db [:current :work]))))
(register-sub :current-query (fn [db] (reaction (get-in @db [:current :query]))))
(register-sub :work (fn [db [_ ting-id]] (reaction (get-in @db [:works ting-id] {}))))

;; # HTML5 view

;; ## Styling

;; ## Components

;; ### Search
;; <img width=20% align=top src=doc/wireframes/search.jpg>

(defn search []
  [:div
   [:input {:value @(subscribe [:current-query])}]
   "..."])

;; ### Work
;; <img width=20% align=top src=doc/wireframes/work.jpg>

(defn work [id]
  (let [work-id @(subscribe [:current-work])
        work @(subscribe [:work work-id]) ]
    [:div
     [:div "TODO: Work history here"]
     [:h1 (:title work)]
     [:div "af " (:creator work)]
     [:img {:src (:cover-url work)}]
     "..."]))

;; ### Library
;; <img width=20% align=top src=doc/wireframes/library.jpg>

(defn library []
  [:div
   [:h1 @(subscribe [:current-library])]
   "..."])

;; ### Status
;; <img width=20% align=top src=doc/wireframes/patron-status.jpg>

(defn patron []
  [:div
   [:h1 "Låner status"]
   "..."])

;; ### Main App entry point
(defn app []
  (case (first @(subscribe [:route]))
    "library" [library]
    "patron" [patron]
    "work" [work]
    "search" [search]
    [search]
    ))

;; ## Execute and events
(render [app])

