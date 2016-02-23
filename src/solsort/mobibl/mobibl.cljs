;; [![Stories in Ready](https://badge.waffle.io/solsort/mobibl.png?label=ready&title=Ready)](https://waffle.io/solsort/mobibl)
;; # MoBibl
;;
;; The purpose of this project is to make an app for the public danish libraries.
;;
;; Feel free to use the issue tracker (https://github.com/solsort/mobibl/issues), for discussions, comments, suggest features etc.
;;
;; ## Status
;;
;; The project starts medio February 2016. Nothing is up and running yet.
;;
;; ## Roadmap
;; ### Version 0.1
;;
;; Functionality
;; - Search for library materials, order books
;; - News / calendar for library
;; - Locate library
;; - Patron status / renew books, etc.
;;
;; Backend
;; - search etc. must build on real services
;; - actual patron status, order books etc. might be mocked if we do not have access to those APIs yet
;;
;; Platforms
;; - HTML5 browser app
;; - Packagede Cordova app
;;
;; Wireframes:
;;
;; <img width=20% align=top src=doc/wireframes/search.jpg>
;; <img width=20% align=top src=doc/wireframes/work.jpg>
;; <img width=20% align=top src=doc/wireframes/library.jpg>
;; <img width=20% align=top src=doc/wireframes/patron-status.jpg>
;;
;; ### Later versions
;;
;; Functionality
;; - Exploration of related materials
;;
;; Platforms
;; - react-native apps (reusing all logic, but rewritten UI using native widgets on Android/iOS)
;;
;; ## Openness
;;
;; The project will be released under a **proper MIT open source license** when/if the Danish Public Library Sector decides, that they want to use the app, and pays for the development cost. Until then it is only released as Creative Commons BY-NC-ND, and copyrighted by solsort.com ApS. The cost calculated from actual number hours used on the project, plus expenses such as: transport to meetings with stakeholders, buying of graphics assets for the projects, etc. Contributors should keep track of how many hours we use on the project.
;;
;; The development happens in full openness on the project github repository, and release status, features etc. can be seen/discussed here.
;;
;; # Prelude of literate source code
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
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]
    [soda-ash.element :as soda]))

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
   :patron
   {:patron 
    {:reservations
     [{id: "123456-basis:12345678"
       
       }]}}
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
      {:name "Lydbog (online) (bind 3)" :availability :available}]}
    {:title "Harry Potter 5 - Harry Potter og Fønixordenen"
     :creator "Some Person"
     :cover-url "http://www.bogpriser.dk/Covers/260/9788702173260.jpg"
      :description "Harry Potter er desperat efter at komme tilbage til Hogwarts og finde ud af, hvorfor Ron og Hermione har været så hemmelighedsfulde hele sommeren. Men det femte år på Hogwarts bliver hverken let eller behageligt for Harry.  Ministeriet for Magi nægter at tro på Harrys og Dumbledores forsikringer om, at Voldemort er vendt tilbage.  Ministeren sender en repræsentant til skolen, som skal sørge for at udbrede Ministeriets holdning og lukke munden på Harry - med alle midler. Og med katastrofale følger..."
     }
    }
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

(defn search []
  [:div
   [:input {:value @(subscribe [:current-query])}]
   "..."])

(defn work [id]
  (let [work-id @(subscribe [:current-work])
        work @(subscribe [:work work-id]) ]
    [:div
     [:div "TODO: Work history here"]
     [:h1 (:title work)]
     [:div "af " (:creator work)]
     [:img {:src (:cover-url work)}]
     "..."]))

(defn library []
  [:div
   [:h1 @(subscribe [:current-library])]
   "..."])

(defn patron []
  (let [reservations-arrived (subscribe [:reservations-arrived])
        borrowed       (subscribe [:borrowed])
        reservations         (subscribe [:reservations])]
    (fn []
        [:div
         [:div {:class "menu"}
          [:button {:type "submit"} "Log Ud"]]
         [:h1 "Låner status"]
         [:div 
          [:h2 "Hjemkomne"]
          [:div ]]
         [:div
          [:h2 "Hjemlån"]
          [:div ]]
         [:div
          [:h2 "Bestillinger"]
          [:div ]]])))

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
