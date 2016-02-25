[![Stories in Ready](https://badge.waffle.io/solsort/mobibl.png?label=ready&title=Ready)](https://waffle.io/solsort/mobibl)

# MoBibl

The purpose of this project is to make an app for the public danish libraries.

Feel free to use the issue tracker (https://github.com/solsort/mobibl/issues), for discussions, comments, suggest features etc.

## Status

The work on the project has started. No public demo is available yet.
# Contributing

We use [github issues](https://github.com/solsort/mobibl/issues) to keep track of the project. 

Feel free to create new issues, or start working on existing issues.

All kinds of contributions are welcome, including:

- testing the app and reporting bugs
- design and graphics
- improvement of user experience
- writing documentation
- ideas for features, use cases, etc.
- coding

Labels are used to make it easier to find tasks within different areas.
If you cannot find a task that fits you, feel free to make one, or create an issue about a task missing within the area you would like to contribute in.

## Workflow

We use the following labels to keep track of issue status.

- `open` means that it should be ready to work on, and anybody are welcome to start looking into it. If it is not clear what the first task of the issue is, make a quick comment and ask "how do I start?" or something similar.
- `in progress` means that somebody is working on it at the moment.
- `ready` are pull requests, and means that the feature is implemented, and ready to be merged into the main project. Best practice is to let somebody else merge it, thereby sharing knowledge and having an extra pair of eyes looking at it.

A board of current issues can be seen on [waffle.io](https://waffle.io/solsort/mobibl). 

Techincal tips: create a feature branch to work on the feature with `git checkout -b 42-some-issue-description` where 42 is the issue number. This automatically assigns the issue to you, and move it to `in progress`. If the commit fixing the issue includes the text `fixes #42` this will auto-close the issue when the pull-request is merged.


## Coding

The first version is a HTML5-app using React. Later on we might add native UIs using React-native, while keeping the logic.

We use [re-frame](https://github.com/Day8/re-frame) to structure the app, and it is written in ClojureScript.

Recommended readings:

- https://github.com/Day8/re-frame/blob/master/README.md
- http://reagent-project.github.io/
- https://github.com/Day8/re-frame/wiki/Creating%20Reagent%20Components
- ... feel free to add more references here

----

If you are making major contributions to the project, please keep track of the hours/effort you use (in case we apply for or get some funding for the project).
# License

Current version is released under [Creative Commons BY-NC-ND](https://creativecommons.org/licenses/by-nc-nd/3.0/), and copyrighted by solsort.com ApS.

The project will be released under a **proper MIT open source license** if the Danish Public Library Sector, or someone else, decides, that they want to use the app, and pays for the development cost. 
# mobibl.cljs

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
### Tab bar - menu in bottom of the screen

    (defn tabbar-button [id s]
       [:a {:href (str "#" id)} 
        [:img {:src (str "assets/" id "-icon.png")
               :alt s}]]
      )
    (defn tabbar []
      [:div.tabbar
       [tabbar-button "search" "Søg"]
       [tabbar-button "work" "Materiale"]
       [tabbar-button "library" "Bibliotek"]
       [tabbar-button "status" "Status"]])

### Search
<img width=20% align=top src=doc/wireframes/search.jpg>

    (defn search []
      [:div
       [tabbar]
       [:input {:value @(subscribe [:current-query])}]
       "..."])

### Work
<img width=20% align=top src=doc/wireframes/work.jpg>

    (defn work [id]
      (let [work-id @(subscribe [:current-work])
            work @(subscribe [:work work-id]) ]
        [:div
         [tabbar]
         [:div "TODO: Work history here"]
         [:h1 (:title work)]
         [:div "af " (:creator work)]
         [:img {:src (:cover-url work)}]
         "..."]))


### Library
<img width=20% align=top src=doc/wireframes/library.jpg>

    (defn library []
      [:div
       [tabbar]
       [:h1 @(subscribe [:current-library])]
       "..."])

### Status
<img width=20% align=top src=doc/wireframes/patron-status.jpg>

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

### Main App entry point
    (defn app []
      (case (first @(subscribe [:route]))
        "search" [search]
        "work" [work]
        "library" [library]
        "status" [patron]
        [search]))

## Execute and events
    (render [app])
