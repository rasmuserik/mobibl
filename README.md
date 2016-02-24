[![Stories in Ready](https://badge.waffle.io/solsort/mobibl.png?label=ready&title=Ready)](https://waffle.io/solsort/mobibl)
# MoBibl

The purpose of this project is to make an app for the public danish libraries.

Feel free to use the issue tracker (https://github.com/solsort/mobibl/issues), for discussions, comments, suggest features etc.

## Status

The project starts medio February 2016. Nothing is up and running yet.

## Roadmap
### Version 0.1

Functionality
- Search for library materials, order books
- News / calendar for library
- Locate library
- Patron status / renew books, etc.

Backend
- search etc. must build on real services
- actual patron status, order books etc. might be mocked if we do not have access to those APIs yet

Platforms
- HTML5 browser app
- Packagede Cordova app

Wireframes:

<img width=20% align=top src=doc/wireframes/search.jpg>
<img width=20% align=top src=doc/wireframes/work.jpg>
<img width=20% align=top src=doc/wireframes/library.jpg>
<img width=20% align=top src=doc/wireframes/patron-status.jpg>

### Later versions

Functionality
- Exploration of related materials

Platforms
- react-native apps (reusing all logic, but rewritten UI using native widgets on Android/iOS)

## Openness

The project will be released under a **proper MIT open source license** when/if the Danish Public Library Sector decides, that they want to use the app, and pays for the development cost. Until then it is only released as Creative Commons BY-NC-ND, and copyrighted by solsort.com ApS. The cost calculated from actual number hours used on the project, plus expenses such as: transport to meetings with stakeholders, buying of graphics assets for the projects, etc. Contributors should keep track of how many hours we use on the project.

The development happens in full openness on the project github repository, and release status, features etc. can be seen/discussed here.

# Prelude of literate source code

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
        [solsort.mobibl.mock-data :refer [sample-db]]))

# Handlers

When the application loads we set the data for use in the frontend by
with the :reset-db handler.  See #36
    (register-handler :reset-db (fn [_ [_ db]] db))
    (register-handler :route (fn [db [_ route]] (assoc db :route route)))

Initialise the database with sample data

    (dispatch [:reset-db sample-db])

# Subscriptions

    (register-sub :route (fn [db] (reaction (get @db :route))))
    (register-sub :current-library (fn [db] (reaction (get-in @db [:current :library]))))
    (register-sub :current-work (fn [db] (reaction (get-in @db [:current :work]))))
    (register-sub :current-query (fn [db] (reaction (get-in @db [:current :query]))))
    (register-sub :work (fn [db [_ ting-id]] (reaction (get-in @db [:works ting-id] {}))))
    (register-sub :reservations
                  (fn [db [_ ids]] (reaction (get-in @db [:patron :reservations]))))
    (register-sub :reservations-arrived
                  (fn [db [_ ids]] (reaction (get-in @db [:patron :reservations-arrived]))))
    (register-sub :borrowed
                  (fn [db [_ ids]] (reaction (get-in @db [:patron :borrowed]))))

# HTML5 view

## Styling

## Components

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
            borrowed             (subscribe [:borrowed])
            reservations         (subscribe [:reservations])]
        (fn []
            [:div
             [:h1 "Låner status"]
             [:div {:class "menu"}
              [:button {:type "submit"} "Log Ud"]]
             [:div
              [:h2 "Hjemkomne"]
              (into
               [:ul]
               (for [ra @reservations-arrived]
                    [:li
                     [:a {:href (str "#/arrived/" (:id ra))} (:title ra)]
                     [:ul
                      [:li (str "Afhentes inden " (:until ra))]
                      [:li "Opstilling " [:a {:href (str "#/location/" (:location ra))} (:location ra)]]

**TODO** Add unique creator ID

                      [:li [:a {:href (str "#/creator/" "TODO-creator-id")} (:creator ra)]]]]))]
             [:div
              [:h2 "Hjemlån"]
              [:div
               [:a {:href (str "#/borrowed/renew/all")} "Forny Alle"]]
              (into
               [:ul]
               (for [b @borrowed]
                    [:li
                     [:a {:href (str "#/borrowed/item/" (:id b))}

**TODO** It would be nice with thumbnails

                      [:img {:src "http://www.bogpriser.dk/Images/placeholder-cover.png"
                             :width "32" :height "32" :alt "TODO :cover-mini-url"}]
                      [:span { :style {:margin-left "1em"}} (:title b)]]
                     [:ul
                      [:li (str "Afleveres senest " (:due-date b))]
                      [:li [:a {:href (str "#/borrowed/renew/" (:id b))} "Forny"]]]]))]
             [:div
              [:h2 "Bestillinger"]
              (into
               [:ul]
               (for [r @reservations]
                    [:li
                     [:a {:href (str "#/reservation/" (:id r))} (:title r)]
                     [:ul
                      [:li [:a {:href (str "#/creator/" (:id r))} (:creator r)]]
                      [:li [:a {:href (str "#/reservation/remove/" (:id r))} "Slet"]]]]))]])))

    (defn app []
      (case (first @(subscribe [:route]))
        "library" [library]
        "patron" [patron]
        "work" [work]
        "search" [search]
        [search]))

## Execute and events
    (render [app])
