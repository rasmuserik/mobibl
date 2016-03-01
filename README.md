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
# Main entry point (main.cljs)

The application consist of three parts.

    (ns solsort.mobibl.main
      (:require

The application database subscriptions and handlers.

        [solsort.mobibl.mobibl]

The UI/views.

        [solsort.mobibl.html5]

The data source / connection to the server.

        [solsort.mobibl.bibapp-datasource]))
# Sample Application Database (`mock_data.cljs`)
    (ns solsort.mobibl.mock-data)

As we are starting out implementing the views, we just have dummy data here
so far.  The precise format and content of this data has not yet been
determined.

    (def sample-db
      {:route ["search"]

## User loan status

Info under `:status` refers with ids to complete descriptions in the
`:works` section

       :status
       {:reservations
        [{:id "775100-katalog:28934572" :until "2016-03-12"}]
        :arrived
        [{:id "775100-katalog:50643662" :until "2016-03-01"}
         {:id "775100-katalog:10260744" :until "2016-03-02"}]
        :borrowed
        [{:id "870970-basis:28934297" :until "2016-03-24"}
         {:id "123456-basis:12345678" :until "1901-12-12"}]}

## Current items on pages

       :current
       {"status" "Murakami"
        "work" "870970-basis:28934297"}

## Search history

       :searches
       [{:query "test"
         :results ["870970-basis:28934297"
                   "775100-katalog:50643662"
                   "775100-katalog:28934572"
                   "775100-katalog:10260744"]}]

## Creative works

This contains the metadata of the creative works,
caching the webservices.

       :works
       {"870970-basis:28934297"
        {:title "1Q84"
         :creator "Haruki Murakami"
         :cover-url "http://www.bogpriser.dk/Covers/202/9788779559202.jpg"
         :description
         "Aomame er en 30-årig smart pige, uddannet kampsportsinstruktør, men
         arbejder p.t. som lejemorder. Tengo er matematiklærer med
         forfatterdrømme.  Han skal omskrive en sær 17-årig piges sære historie.
         Begge hovedfigurer oplever, at deres virkelighed forvrides let, hvad
         påvirker deres virkelighed?"
         :keywords ["Kultur" "Kærlighed" "Magisk Realisme" "Magt"
                    "Parallelle Verdener" "Skrivekunst" "Japan" "1980-1989"]
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
        "775100-katalog:50643662"
        {:title "Matematik i virkeligheden"
         :creator "Allan Baktoft"
         :cover-url ""
         :description "Regn den ud"
         :keywords ["Matematik" "Regne"]
         :location "Faglitteratur"
         :languages ["Dansk"]
         :editions
         [{:name "Bog (bind 3)" :availability :available}
          {:name "Lydbog (cd) (bind 1)" :availability :available}
          {:name "Lydbog (cd) (bind 2)" :availability :available}
          {:name "Lydbog (cd) (bind 3)" :availability :available}
          {:name "Lydbog (online) (bind 1)" :availability :available}]}
        "775100-katalog:28934572"
        {:title "A momentary lapse of reason"
         :creator "The Pink Floyd"
         :cover-url "https://en.wikipedia.org/wiki/File:MLoRLP01.jpg"
         :description "Musik"
         :keywords ["Rock"]
         :location "Musik"
         :languages ["Engelsk"]
         :editions
         [{:name "Lydbog (cd) (bind 1)" :availability :available}
          {:name "Lydbog (cd) (bind 2)" :availability :loaned}
          {:name "Lydbog (cd) (bind 3)" :availability :loaned}]}
        "775100-katalog:10260744"
        {:title "Ummagumma"
         :creator "The Pink Floyd"
         :cover-url
         (str "https://upload.wikimedia.org/wikipedia/en/1/16/"
              "PinkFloyd-album-ummagummastudio-300.jpg")
         :description "Musik"
         :keywords ["Rock"]
         :location "Musik"
         :languages ["English"]
         :editions
         [{:name "Lydbog (cd) (bind 1)" :availability :loaned}
          {:name "Lydbog (cd) (bind 2)" :availability :available}
          {:name "Lydbog (cd) (bind 3)" :availability :available}]}}})
# Actual application logic (`mobibl.cljs`)

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

## Handlers

When the application loads we set the data for use in the frontend by
with the :reset-db handler.  See #36
    (register-handler :reset-db (fn [_ [_ db]] db))
    (register-handler
      :open (fn [db [_ [page id]]]
              (let [id (or id (get-in db [:current page]))]
                (log [page id])
                (-> db
                    (assoc-in [:current page] id)
                    (assoc :route [page id])))))

    (register-handler
      :request-work
      (fn [db [_ id]]
        (assoc-in db [:works id :status :requested] true)))

Initialise the database with sample data
    (dispatch [:reset-db sample-db])

## Subscriptions


    (def default-work
      {:title "Unknown Title"
       :creator "Unknown Creator"})

    (defn get-work [db id]
      (let [work (get-in db [:works id])]
      (when-not work (dispatch [:request-work id]))
      (merge default-work {:id id} work)))

    (register-sub :work (fn [db [_ id]] (reaction (get-work @db id))))
    (register-sub :works (fn [db _] (reaction (:works @db))))
    (register-sub :route (fn [db] (reaction (get @db :route))))


Helper function to query the db for the full info about works

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
# HTML5 view (html5.cljs)

    (ns solsort.mobibl.html5
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


## Styling


    (load-style! normalize-css "style-reset")
    (defn styling []

We are designing for mobile-portrait-mode,
which can be enforced in the packaged cordova-app.

Using a unit-size of 1/24 of the mobile portrait width,
means that a clickable object should preferibly
be 4 units high/wide, though 3 units is also ok.

It also allows for 1/2, 1/3, 1/4, and 1/6 division of the screen,
and 5/8 vs 3/8 which approximately the golden ratio.

      (let [unit (/ js/document.body.clientWidth 24)]
        (load-style!
          {:body
           {:background "#ffffff"}
           "div,a,span,b,i,img,button"
           {:box-sizing :border-box}
           ".button"
           {:display :inline-block
            :min-height (* 2.5 unit)
            :border-radius (* 0.5 unit)
            :border (str (* 0.15 unit) "px solid black")
            :padding-top (* 0.5 unit)
            :padding-left (* 0.3 unit)
            :padding-right (* 0.3 unit)
            :text-align :center
            :vertical-align :middle}}
          "general styling")
### Tabbar
        (load-style!
          {".tabbar"
           {:position :fixed
            :box-sizing :border-box
            :bottom 0
            :left 0
            :width "100%"
            :background-color :white
            :border-top "1px solid black"
            }
           ".tabbar a"
           {:display :inline-block
            :box-sizing :border-box
            :width (* 6 unit)
            :text-align :center}
           ".tabbar img"
           {:height (* 4 unit)
            :width (* 4 unit)}
           "body"
           {:padding-bottom (* 4 unit)}
           }
          "tabbar-styling")
### Book
        (load-style!
          {".work"
           {:margin-left unit
            :margin-right unit
            }
           ".work .title"
           {:text-align :center
            :font-size "200%"
            :margin-top unit }
           ".work .author"
           {:text-align :center
            :margin-bottom unit}
           ".work-keyword"
           {:display :inline-block
            :vertical-align :middle
            :clear :none
            :padding-top (* 0.5 unit)
            :min-height (* 2 unit)
            ;:outline "1px solid black"
            :width (* unit 7.3)
            }
           ".work-img"
           {:float :right
            :margin-left 0
            :margin-right 0
            :width (* unit 14)}}
          "work-style"
          )
        ))
### Actually apply styling

    ; re-layout on rotation etc.
    (js/window.addEventListener "resize" styling)
    ; re-layout when everything has loaded, to account for
    ; possible change of width due to appearing scrollbar
    (js/window.addEventListener "load" #(js/setTimeout styling 0))
    ; re-layout on load, and on figwheel reload
    (styling)

## Components
### Tab bar - menu in bottom of the screen

    (defn tabbar-button [id s]
      [:a {:href (str "#" id)}
       [:img {:src (str "assets/" id "-icon.png")
              :alt s}]])

    (defn tabbar []
      [:div.tabbar
       [tabbar-button "search" "Søg"]
       [tabbar-button "work" "Materiale"]
       [tabbar-button "library" "Bibliotek"]
       [tabbar-button "status" "Status"]])

### Search
<img width=20% align=top src=doc/wireframes/search.jpg>

    (defn search [query]
      [:div
       [tabbar]
       [:input {:value query}]
       "..."])

### Work
<img width=20% align=top src=doc/wireframes/work.jpg>

    (defn work [work-id]
      (let [work @(subscribe [:work work-id])
            language (:language work)
            keywords (:keywords work)
            location (:location work)
            creator (:creator work)]
        [:div.work
         [tabbar]
         [:div "TODO: Work history here"]
         [:div.title (:title work)]
         [:div.author "af " [:a {:href (str "#search/" creator)} creator]]
         [:img {:class "work-img"
                :src (:cover-url work)}]
         [:div [:a.button "Bestil"]]
         (if-not keywords ""
           (into [:p #_[:em "Emne: "]]
                 (interpose
                   " "
                   (for [word keywords]
                     [:a.work-keyword {:href
                          (str "#search/" word)} word]))))
         [:div.work-desc (:description work)]
         (if language [:p [:em "Sprog: "] language] "")
         (if location [:p [:em "Opstilling: "] location] "")]))


### Library
<img width=20% align=top src=doc/wireframes/library.jpg>

    (defn library [library]
      [:div
       [tabbar]
       [:h1 library]
       "..."])

### Status
<img width=20% align=top src=doc/wireframes/patron-status.jpg>

    (defn status []
      (let [arrived (subscribe [:arrived])
            borrowed             (subscribe [:borrowed])
            reservations         (subscribe [:reservations])]
        (fn []
          [:div
           [tabbar]
           [:h1 "Låner status"]
           [:div {:class "menu"}
            [:button {:type "submit"} "Log Ud"]]
           [:div
            [:h2 "Hjemkomne"]
            (into
              [:ul]
              (for
                [ra @arrived]
                [:li
                 [:a {:href (str "#work/" (:id ra))} (:title ra)]
                 [:ul
                  [:li (str "Afhentes inden " (:until ra))]
                  [:li "Opstilling "
                   [:a {:href (str "#/location/" (:location ra))} (:location ra)]]

**TODO** Add unique creator ID

                  [:li
                   [:a
                    {:href (str "#/creator/" "TODO-creator-id")} (:creator ra)]]
                  ]]))]
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
                  [:li (str "Afleveres senest " (:until b))]
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
                  [:li [:a {:href (str "#/reservation/remove/" (:id r))} "Slet"]]
                  ]]))]])))

### Main App entry point
    (defn app []
      (let [[page id] @(subscribe [:route])]
        (case page
          "search" [search id]
          "work" [work id]
          "library" [library id]
          "status" [status]
          [search ""])))

## Execute and events

    (render [app])

## Routing

    (defn handle-hash []
      (dispatch [:open (string/split (.slice js/location.hash 1) "/")]))
    (defn open [& args]
      (aset js/location "hash" (string/join "/" args)))
    (js/window.addEventListener "hashchange" handle-hash)
    (handle-hash)
# BibApp Data Source (bibapp_datasource.cljs)

    (ns solsort.mobibl.bibapp-datasource
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
        [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))
