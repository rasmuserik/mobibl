[![Stories in Ready](https://badge.waffle.io/solsort/mobibl.png?label=ready&title=Ready)](https://waffle.io/solsort/mobibl)
[![Build Status](https://travis-ci.org/solsort/mobibl.svg?branch=master)](https://travis-ci.org/solsort/mobibl)

# MoBibl

The purpose of this project is to make an app for the public danish libraries.

Feel free to use the issue tracker (https://github.com/solsort/mobibl/issues), for discussions, comments, suggest features etc.

## Status

The work on the project has started. No public demo is available yet.
# Roadmap

- very first prototype, v0.1.0
    - views
        - search-page - list of books with cover, open book on click
        - book - metadata, bestil link, link til bibliotek hvor hjemme
        - library - map with single library + dummy text
        - user list of arrived, loans, orders
    - on top of dummy data
    - dummy-user-data no order-flow
    - no suggest
    - no facets
    - no recommendations

## Notes: overview of functionality of ddb-cms, med henblik på hvilke dele der giver mening i app...
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
# Core application logic and database (`mobibl.cljs`)

This file contain the platform independent logic of the app.

## Documentation of data structure

The app-db is the core of the application
and should have the following structure:

- `:ui` everything related to the ui
- `:route` TODO everything about routing and history
    - `:path` current route as a array
    - `:history` history of routes, with duplicate removed
- `:backend` everything specific to certain backends (currently `requested`)
- `:data` TODO application specific data:
    - `:libraries` information about the libraries
    - `:search :results` results of search queries
    - `:facets` TODO facets for searches
    - `:user` user status (currently `:status`)
    - `:works` information about specific works

### Data backend

A data backend should supply the following handlers

- `:request-work work-id`
- `:request-facets query`
- `:request-search query`
- `:request-suggest query`
- `:request-library library-id`
- `:request-status`
- ... login, order
    - `:order` ...
    - `:login` ...
    - `:request-work-details` ...

And it should automatically load the list of libraries.

## General dependencies and setup

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

## DEBUG: uncomment this to print db on reload

    (register-sub :db (fn [db] (reaction @db)))
    (log 'db @(subscribe [:db]))

## Routing and history


    (register-sub
      :route
      (fn [db] (reaction (get-in @db [:route :path] [:search ""]))))

    (register-sub
      :history
      (fn [db [_ page]] (reaction (get-in @db [:route :history page] []))))

    (register-handler
      :route
      (fn [db [_ page & params]]
        (let [page (or page :search)
              params (or params (first (get-in db [:route :history page])))
              route (into [page] params)]
          (-> db
              (assoc-in [:route :history page]
                        (into [params]
                              (remove #{params}
                                      (get-in db [:route :history page]))))
              (assoc-in [:route :path] route)))))

## Work

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

## Search

    (register-sub
      :search
      (fn [db [_ q page]]
        (reaction (let [results (get-in @db [:search q page])]
                    (or results
                        (do (dispatch [:request-search q page]) []))))))
    (register-handler
      :facets
      (fn [db [_ query facets]] (assoc-in db [:facets query] facets)))
    (register-sub
      :facets
      (fn [db [_ query]] (reaction (get-in @db [:facets query] []))))

## UI

    (register-handler
      :ui (fn [db [_ id value]] (assoc-in db [:ui id] value)))

    (register-sub :ui (fn [db [_ path]] (reaction (get-in @db [:ui path]))))
    (register-handler
      :add-facet (fn [db [_ facet]]
                   (assoc-in db [:ui :facets]
                             (cons facet (get-in db [:ui :facets] [])))))
    (register-handler
      :remove-facet (fn [db [_ facet]]
                      (assoc-in db [:ui :facets]
                                (remove #{facet} (get-in db [:ui :facets] [])))))

## Libraries

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

### Sample library

    (dispatch-sync
      [:library
Simple representation of the libraries that are interesting for the user
including position on a map, opening hours and contact information.

FIXME The above books in the works section are from different libraries,
ie. 775100 is Aarhus hovedbibliotek.

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
FIXME How to represent many opening hours for departments of a library?
- these are delivered as a &lt;pre&gt;-formatted data
        :hours
        [{:title "Åbningstider"
          :weekdays [[8 22] [8 20] [8 20] [8 20] [8 19] [8 17]]}
         {:title "Betjening"
          :weekdays [[12 17] [12 17] [12 17] [12 17] [12 17] [12 15]]}]}
       ])

## User status

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

TODO: also run on network reconnect, and after a while
    (dispatch [:request-status])
# HTML5 view (html5.cljs)

    (ns solsort.mobibl.html5
      (:require-macros
        [cljs.core.async.macros :refer [go go-loop alt!]]
        [reagent.ratom :as ratom :refer  [reaction]])
      (:require
        [cljs.reader]
        [solsort.util
         :refer
         [<ajax <seq<! js-seq load-style! put!close!
          parse-json-or-nil log page-ready render dom->clj]]
        [reagent.core :as reagent :refer []]
        [re-frame.core :as re-frame
         :refer [register-sub subscribe register-handler dispatch dispatch-sync]]
        [clojure.string :as string :refer [replace split blank?]]
        [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]
        [solsort.mobibl.leaflet :refer [leaflet]]
        [cljsjs.hammer]))

    (declare route-link)

## Styling

    (def highlight "#326bc5")
    ;(def background fade from "#eaeaea" to "#ffffff")
    (def dark "#262626")
    (def light "#e5e5e5")
    (def medium "#d8d8d8")
    (defn styling []
### general styling
We are designing for mobile-portrait-mode,
which can be enforced in the packaged cordova-app.

Using a unit-size of 1/24 of the mobile portrait width,
means that a clickable object should preferibly
be 4 units high/wide, though 3 units is also ok.

It also allows for 1/2, 1/3, 1/4, and 1/6 division of the screen,
and 5/8 vs 3/8 which approximately the golden ratio.

      (let [unit (/ (js/Math.min js/document.body.clientHeight
                                 js/document.body.clientWidth)
                    24)
            unit-height (/ js/document.body.clientHeight 24)]
        (load-style!
          {:body
           {
            ; :background "url(assets/background.jpg)"
            ; :background-color "#fbf8f4"
            :font-family "\"Open Sans\", sans-serif"
            :font-weight "300"}
           ".bold" {:font-weight "bold !important"}
           ".center" {:text-align :center}
           ".inline-block" {:display :inline-block}
           ".italic" {:font-style "italic !important"}
           ".large" {:font-size "120% !important"}
           ".small" {:font-size "80% !important"}
           ".regular " {:font-weight "300 !important"}
           ".condensed" {:font-family "\"Open Sans Condensed\" !important"}
           }
          "general styling")
### Tabbar
        (load-style!
          {".tabbar-spacer"
           {:display :inline-block
            :height 50
            }
           ".tabbar"
           {:position :fixed
            :box-sizing :border-box
            :bottom 0
            :text-align :center
            :left 0
            :width "100%"
            ;:background "url(assets/background.jpg)"
            ;:background-color "#fbf8f4"
            :background-color "#ffffff"
            :box-shadow "-1px 0px 5px rgba(0,0,0,1);"
            :z-index "100"
            }
           ".tabbar a"
           {:display :inline-block
            :box-sizing :border-box
            :width (* 0.25 (- js/document.body.clientWidth 100))
            :text-align :center}
           ".tabbar img"
           {:padding 3
            :height 44
            :width 44}
           }
          "tabbar-styling")
### Library view

FIXME Not so nice to have the style for bib-map defined here

        (load-style!
          {".map"
           {:height (js/Math.min js/document.body.clientWidth
                                 (* 0.6 js/document.body.clientHeight))}
           ".contact"
           {:padding "0em 0em 10em 0em"
            ".contact div span"
            {:margin "0em 1em 0em 0em"
             :border "1px solid blue"}}}
          "library-styling")))

### tinywork

    (let [unit 13
          width (* 4.5 unit)]
      (load-style!
        {:.tinywork
         {:display :inline-block
          :white-space :normal
          :font-size (* 0.8 unit)
          :line-height (str unit "px")
          :position :relative
          :width width
          :height (* 5.5 unit)
          :text-shadow
          (str "1px 0px 1px white,"
               "0px 0px 1px white,"
               "1px 1px 1px white,"
               "0px 1px 1px white")}
         ".tinywork > .bold"
         {:display :inline-block
          :position :absolute
          :top 0
          :left 0
          :width width
          :height (* 4 unit)
          :background "rgba(255,255,255,0.4)"
          :padding-bottom (* .25 unit)
          :overflow :hidden}
         ".tinywork > .condensed"
         {:display :inline-block
          :position :absolute
          :text-align :left
          :bottom 0
          :left 0
          :width width
          :font-size (* 1 unit)
          :white-space :nowrap
          :padding (* .25 unit)
          :height (* 1.5 unit)
          :background "rgba(255,255,255,0.4)"
          :overflow :hidden}
         }
        "tinywork-styling"))

### work

    (load-style!
      {:.work
       {:position :relative
        :overflow "hidden"
        :height "100%"
        :width "100%"}
       ".work > img"
       {:max-width "33%"
        :max-height "100%"
        :box-sizing :border-box
        :vertical-align :top}
       ".work > div"
       {:display :inline-block
        :box-sizing :border-box
        :width "66%"
        :height "100%"
        :vertical-align :top
        :padding-left ".3em"
        :overflow :hidden}
       ".work .fadeout"
       {:display :block
        :position :absolute
        :bottom "0px"
        :height "33%"
        :width "100%"
        :background "linear-gradient(rgba(255,255,255,0), white)" }}
      "work-styling")

### Actually apply styling

    ; re-layout on rotation etc.
    (js/window.addEventListener "resize" styling)
    ; re-layout when everything has loaded, to account for
    ; possible change of width due to appearing scrollbar
    (js/window.addEventListener "load" #(js/setTimeout styling 0))
    ; re-layout on load, and on figwheel reload
    (styling)

## Components
### Component for remembering scroll position per route

    (defn restore-scroll [route]
      (set! js/document.body.scrollTop (get @(subscribe [:ui :scroll]) route 0)))

    (defonce handle-scroll
      (fn [] (dispatch [:ui :scroll
                        (assoc (or @(subscribe [:ui :scroll]) {})
                               @(subscribe [:route])
                               js/document.body.scrollTop)])))
    (js/window.removeEventListener "scroll" handle-scroll)
    (js/window.addEventListener "scroll" handle-scroll)
### Tab bar - menu in bottom of the screen

    (defn tabbar-button [id s]
      [:a {:href (route-link id)}
       [:img {:src (str "assets/" (name id) "-icon.svg")
              :alt s}]])


    (defn tabbar []
      [:div
       [:div.tabbar-spacer " "]
       [:div.tabbar
        [tabbar-button :search "Søg"]
        [tabbar-button :work "Materiale"]
        [tabbar-button :library "Bibliotek"]
        [tabbar-button :status "Status"]]])

### work-tiny

    (def work-tiny-height (* 13 5.5))
    (defn work-tiny [pid]
      (let  [o @(subscribe [:work pid])
             unit 13
             width (* 4.5 unit)]
        [:a {:href (route-link :work pid) :style {:color "#111"}}
         [:div.center.tinywork
          [:img {:src (:cover-url o) :width "100%" :height "100%" } ]
          [:div.bold (:title o)]
          [:div.condensed (:creator o)]]]))

### work-item
    (defn work-item [pid]
      (let [o @(subscribe [:work pid])
            keywords
            (map
              (fn [kw] [:a {:href (route-link :search "" [:subject kw]) } kw])
              (:keywords o)
              )]
        [:div.work
         [:img {:src (:cover-url o) }]
         [:div [:div.fadeout]
          [:div.bold.large (:title o)]
          [:div.italic.large (:creator o)]
          (into
            [:div]
            (interpose ", " (map (fn [s] [:span.condensed.inline-block s])
                                 (:keywords o))))
          [:div (:description o)]]]))

### Facet view
    (defn facet-color [s]
      (case s
        :audience "red"
        :audienceCategory "yellow"
        :literaryForm "yellow"
        :category "yellow"
        :subject "orange"
        :dk5 "orange"
        :creator "olive"
        :period "green"
        :date "green"
        :type "teal"
        :fictionSubject "blue"
        :musicSubject "blue"
        :nonFictionSubject "blue"
        :form "violet"
        :genre "purple"
        :genreCategory "purple"
        :geographic "brown"
        :level "gray"
        "" ))

### Search
<img width=20% align=top src=doc/wireframes/search.jpg>

    (defn facet [search-str active-facets]
      (fn [[col s cnt :as facet]]
        [:a.ui.small.basic.button.condensed.bold
         {:on-click
          (fn []
            (let [facet-history
                  (or @(subscribe [:ui :facet-history]) [])]
              (dispatch [:ui :facet-history
                         (remove #{[col s]} facet-history)])
              (dispatch (into [:route :search search-str [col s]]
                              active-facets))))
          :key (hash [col s])
          :class (facet-color col)} s
         [:span.small.regular " " cnt ""]]))

    (defn search [query]
      (let
        [results @(subscribe [:search query 0])
         results
         (map
           (fn [pid]
             [:a.column {:key pid :href (route-link :work pid)}
              [:div
               {:style {:height "9rem"
                        :color :black
                        :margin-bottom "1rem"
                        :box-shadow "2px 2px 5px 0px rgba(0,0,0,0.1)"}}
               [work-item pid]]])
           results)
         show-history @(subscribe [:ui :show-history])
         search-history @(subscribe [:history :search])
         suggest (when show-history search-history)
         search-str (or (first (filter string? query)) "")
         active-facets (remove string? query)
         facet-history (or @(subscribe [:ui :facet-history]) [])
         facets @(subscribe [:facets :sample])]
        (log 'search query search-str active-facets)
        [:div
         [:div.ui.container
          [:h1 "Mobiblby biblioteker"]
          [:div
           [:div.ui.search.fluid.input.action.left.icon
            [:i.search.icon]
            [:input
             {:placeholder "Indtast søgning"
              :type :text
              :value search-str
              :on-change #(dispatch-sync (into [:route :search
                                                (-> % .-target .-value)]
                                               active-facets))
              }]
            [:button.ui.icon.button
             {:class (if-not search-history "disabled"
                       (if show-history "active" ""))
              :on-click #(dispatch [:ui :show-history (not show-history)])}
             [:i.caret.down.icon]]
            (when suggest
              (into [:div.results.transition.visible
                     {:style {:display "block !important"}}]
                    (for [[s facets] suggest]
                      (into
                        [:a.result
                         {:href (route-link :search s)
                          :on-click #(dispatch-sync [:ui :show-history false])}
                         s " "]
                        (for [[col f] facets]
                          [:div.ui.small.label
                           {:class (facet-color col)}
                           (str f)]
                          )
                        ))))]]]

Facet view
         [:div
          {:style {:white-space :nowrap
                   :overflow-y :hidden
                   :overflow-x :auto
                   :margin-top "1rem"
                   :margin-bottom "1rem"
                   :margin-left "0.5%"
                   :width "99.5%"
                   :display :inline-block
                   }}
          (merge
            [:div]
            (map (fn [[col s :as facet]]
                   [:a.ui.small.button.condensed.bold
                    {:on-click
                     (fn []
                       (dispatch [:ui :facet-history
                                  (conj facet-history facet)])
                       (dispatch (log (into [:route :search search-str]
                                            (remove #{[col s]} active-facets)))))
                     :key (hash [col s])
                     :class (facet-color col)} s])
                 active-facets)
            (map (facet search-str active-facets) facet-history))
          (merge
            [:div]
            (map (facet search-str active-facets) facets))]
         [:div.ui.container
          [:div.ui.grid
           (merge [:div.stackable.doubling.four.column.row]
                  results)]]
         [tabbar]]))

### Work
<img width=20% align=top src=doc/wireframes/work.jpg>

    (defn work [work-id]
      (let [work @(subscribe [:work work-id])
            language (:language work)
            keywords (:keywords work)
            location (:location work)
            creator (:creator work)
            work-history (map first @(subscribe [:history :work]))]
        [:div
         [:div
          {:style
           {:background-color "#777"
            :overflow-x :auto
            :overflow-y :hidden}}
          (into [:div {:style {:white-space :nowrap :height work-tiny-height}}]
                (map work-tiny work-history))]
         [:div.ui.container
          [:p]
          [:h1.center (:title work)]
          [:p.center "af "
           [:a {:href (route-link :search "" [:creator creator])} creator]]
          [:p.center
           [:img
            {:src (:cover-url work)
             :style
             {:max-height (* 0.5 (- js/document.body.clientHeight 50))
              :max-width (* 0.8 (- js/document.body.clientWidth 20))}}]]
          [:p.center [:a.ui.primary.button "Bestil"]]
          [:p (:description work)]
          (if-not keywords ""
            (into [:p {:style {:line-height "2rem"}}]
                  (interpose
                    " "
                    (for [word keywords]
                      [:a.ui.label
                       {:href (route-link :search "" [:subject word])}
                       word]))))
          (if language [:p [:em "Sprog: "] language] "")
          (if location [:p [:em "Opstilling: "] location] "")
          [:p.bold "Relaterede:"]
          [:div.ui.grid
           (into
             [:div.stackable.four.column.doubling.row]
             (map
               (fn [id]
                 [:div.column
                  [:a.small
                   {:href (route-link :work id)
                    :style
                    {:display :inline-block
                     :height "6em"}}
                   (work-item id)]])
               (take 12 (rest (:related work)))))]
          [tabbar]]]))

### Library
<img width=20% align=top src=doc/wireframes/library.jpg>

    (def daynames ["Man" "Tir" "Ons" "Tor" "Fre" "Lør" "Søn"])

    (defn library [id]
      (let [current-library @(subscribe [:library id])]

        (let [address (:address current-library)
              hours   (:hours current-library)
              phone   (:phone current-library)]
          [:div
           [leaflet
            :class "map"
            :id "leafletdiv"
            :pos0 (:position current-library)
            :zoom 13
            :markers
            (map (fn [[pos id]] {:pos pos
                                 :click #(dispatch-sync [:route :library id])})
                 @(subscribe [:libraries]))]
           [:div.ui.container [:h1 (:name current-library)]]
           [:div.ui.container
            [:div.address
             [:h2 "Adresse"]
             [:div (:road address)]
             [:div (:city address)]
             [:div (:country address)]]
            [:div.open
             [:h2 "Åbningstider"]
             [:div
              "(blob of html describing opening hours)"]]
            [:div.contact
             [:h2 "Kontakt"]
             [:div
              [:span "Email: "]
              [:span (:email current-library)]]
             [:div
              [:span "Telefon: "]
              [:span (:number phone)]
              " "
              [:span (:time phone)]]]]
           [tabbar]
           ])))

### Status
<img width=20% align=top src=doc/wireframes/patron-status.jpg>
    (defn loan-entry [id & content]
      [:div
       {:style {:margin-bottom "1rem"}}
       (into [:span
              {:style
               {:display :inline-block
                :vertical-align :top
                :width "30%" }}]
             content)
       [:a
        {:href (route-link :work id)
         :style
         {:display :inline-block
          :font-size "70%"
          :vertical-align :top
          :width "70%"
          :height "4rem" } }
        [work-item id]]
       ]

      )

    (defn status []
      (let [arrived (subscribe [:arrived])
            borrowed             (subscribe [:borrowed])
            reservations         (subscribe [:reservations])]
        (fn []
          [:div.ui.container
           [:div.right.floated.ui.primary.button "Log ud"]
           [:h1 "Låner status"]
           [:p
            [:h2 "Hjemkomne"]
            (into
              [:div]
              (for
                [ra @arrived]
                (loan-entry
                  (:id ra)
                  [:div (:until ra)]
                  ; TODO location to fetch
                  )))]
           [:p
            [:h2.ui.left.header
             [:div.content
              {:style
               {:width "30%"
                :min-width "8rem"}} "Hjemlån"]
             [:div.ui.button "Forny alle"]]
            (into
              [:div]
              (for [b @borrowed]
                (loan-entry
                  (:id b)
                  [:div (:until b)]
                  [:div.ui.small.button "Forny"]
                  )
                ))]
           [:p
            [:h2 "Bestillinger"]
            (into
              [:div]
              (for [r @reservations]
                (loan-entry
                  (:id r)
                  [:div.ui.small.button "Slet"]
                  )
                ))
            ]
           [tabbar]
           ])))


### Main App entry point
    (defn app []
      (let [prev-route (atom)]
        (fn []
          (let [[page & params  :as route] @(subscribe [:route])
                first-run (not= @prev-route route)
                ]
            (when first-run
              (reset! prev-route route)
              (js/setTimeout #(restore-scroll route) 0))
            [:div
             (case page
               :search [search params first-run]
               :work [work (first params)]
               :library [library (or (first params) "710100")]
               :status [status]
               [search ""])]))))

## Swipe gestures

    (defonce routes #js [:search :work :library :status])

    (defn change-route [delta]
      (let [n (+ delta (.indexOf routes (first @(subscribe [:route]))))
            n (max 0 (min (dec (.-length routes)) n))]
        (dispatch [:route (aget routes n)])))
    (defn addSwipeGestures []
      (let [hammer (js/Hammer.Manager. js/document.body)
            swipe  (js/Hammer.Swipe.)]
        (.add hammer swipe)
        (.on hammer "swipeleft" #(change-route 1))
        (.on hammer "swiperight" #(change-route -1))))
    (defonce register-swipe (addSwipeGestures))


## Routing

    (defn route-link [& route] (str "#" (prn-str route)))

    (defonce handle-hash
      (fn []
        (if (empty? js/location.hash)
          (dispatch [:route])
          (dispatch (into [:route]
                          (cljs.reader/read-string
                            (.slice js/location.hash 1)))))))

    (js/window.removeEventListener "hashchange" handle-hash)
    (js/window.addEventListener "hashchange" handle-hash)
    (handle-hash)

    (defonce sync-hash
      (ratom/run!
        (let [[page param :as route] @(subscribe [:route])]
          (js/history.pushState nil nil (apply route-link route)))))
## Execute and events

    (render [:div [app]])
# BibApp Data Source (bibapp_datasource.cljs)

    (ns solsort.mobibl.bibapp-datasource
      (:require-macros
        [cljs.core.async.macros :refer [go go-loop alt!]]
        [reagent.ratom :as ratom :refer  [reaction run!]])
      (:require
        [solsort.util
         :refer
         [<ajax <seq<! js-seq normalize-css load-style! put!close!
          parse-json-or-nil log page-ready render dom->clj]]
        [clojure.walk :refer [keywordize-keys]]
        [reagent.core :as reagent :refer []]
        [clojure.data]
        [re-frame.core :as re-frame
         :refer [register-sub subscribe register-handler dispatch dispatch-sync]]
        [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

## Convert data from datasource to internal format


    (defn convert-bibentry [o]
      (let [isbn (str (first (o "isbn")))]
        {:title (first (o "title"))
         :creator (first (o "creator"))
         :cover-url (str
                      "http://www.bogpriser.dk/Covers/"
                      (.slice isbn -3) "/" isbn ".jpg")
         :keywords (get o "subject" [])
         :related (map first (o "related"))
         :description (first (o "description"))
         :location nil
         :language (first (o "language"))
         }))

## Mock data
### Dummy ids of books etc. with metadata

    (def dummy-ids
      ["870970-basis:29820031" "870970-basis:45231402" "870970-basis:29146004"
       "870970-basis:28794630" "870970-basis:28904061" "870970-basis:45574881"
       "870970-basis:51604288" "870970-basis:44351641" "870970-basis:45470075"
       "870970-basis:27697917" "870970-basis:22324284" "870970-basis:28452551"
       "810010-katalog:008471560" "870970-basis:44741830" "870970-basis:28534698"
       "870970-basis:45583457" "870970-basis:45386864" "870970-basis:45421716"
       "870970-basis:28052472" "870970-basis:45493016" "870970-basis:44291738"
       "870970-basis:23060132" "810010-katalog:007071351" "870970-basis:45554813"
       "870970-basis:45237648" "870970-basis:28407513" "870970-basis:44950723"
       "830380-katalog:93161505" "870970-basis:27006434" "870970-basis:45618765"
       "870970-basis:26666074" "870970-basis:44695634" "870970-basis:27455344"
       "870970-basis:50914968" "870970-basis:45170306" "870970-basis:45233758"
       "870970-basis:29706328" "870970-basis:51582772" "870970-basis:45199088"
       "870970-basis:27880436" "870970-basis:29991537" "870970-basis:44313235"
       "870970-basis:23116642" "870970-basis:45233332" "870970-basis:44547759"
       "870970-basis:44910888" "870970-basis:51313380" "870970-basis:44887509"
       "870970-basis:26829798" "870970-basis:45005801" "870970-basis:25893018"
       "870970-basis:44364999" "870970-basis:44331225" "870970-basis:50625656"
       "870970-basis:45534952" "870970-basis:44591413" "870970-basis:44592045"
       "870970-basis:28522517" "870970-basis:29100160" "870970-basis:26396417"
       "870970-basis:50565858" "870970-basis:28930240" "870970-basis:28108990"
       "870970-basis:27195105" "870970-basis:28372531" "870970-basis:44831562"
       "870970-basis:50520846" "870970-basis:45182266" "870970-basis:29158746"
       "870970-basis:43917579" "870970-basis:45217345" "870970-basis:45263762"
       "870970-basis:50909794" "810010-katalog:007144163" "870970-basis:26952425"
       "870970-basis:27873251" "870970-basis:45350568" "870970-basis:44850001"
       "870970-basis:44520028" "870970-basis:44150484" "870970-basis:27561527"
       "870970-basis:27867138" "870970-basis:28539290" "870970-basis:45153843"
       "870970-basis:29287341" "870970-basis:26681316" "870970-basis:45281434"
       "870970-basis:28715730" "870970-basis:45300439" "870970-basis:45575969"
       "870970-basis:27374859" "820010-katalog:3096314" "870970-basis:26509904"
       "870970-basis:44406365" "870970-basis:44623234" "870970-basis:44973650"
       "870970-basis:44537052" "870970-basis:51283708" "870970-basis:45377458"
       "870970-basis:28009011" "870970-basis:45076261" "870970-basis:27165435"
       "870970-basis:24232123" "870970-basis:45164683" "870970-basis:44529807"])

### Load mock bibliographic data if available
    (go
      (let [mockdata (<! (<ajax "mockdata.json"))]
        (when mockdata
          (doall (for [o mockdata]
                   (dispatch [:work (o "_id") (convert-bibentry o)]))))))
    (go
      (let [facets (<! (<ajax "assets/sample-facets.json"))]
        (dispatch [:facets :sample
                   (sort-by #(- (nth % 2))
                            (map (fn [[type facet count :as f]]
                                   [(keyword type) facet count]) facets))])))
## Library data
    (go
      (let [libraries (<! (<ajax "assets/libraries.json"))
            libgeo (map
                     (fn [lib]
                       [#js[((lib "geo") "lat") ((lib "geo") "lng")] (lib "id")])
                     libraries)]

        (dispatch [:libraries libgeo])
        (doall
          (map #(dispatch
                  [:library
                   {:id (% "id")
                    :name (% "name")
                    :address
                    {:road(% "street")
                     :postcode (% "postcode")
                     :city (% "city")}
                    :position [((% "geo") "lat")  ((% "geo") "lng")]}])
               libraries))))

## Get work metadata from database

Use data from http://solsort.com/db/bib

    (register-handler
      :request-work
      (fn [db [_ id]]
        (when-not (get-in db [:requested id] false)
          (go
            (dispatch
              [:work id
               (convert-bibentry
                 (<! (<ajax (str "https://solsort.com/db/bib/" id))))])))
        (assoc-in db  [:requested id] true)))

    (register-handler
      :request-search
      (fn [db [_ query page]]
        (if (get-in db [:search query page])
          db
          (assoc-in db [:search query page]
                    (take (js/Math.round (* (js/Math.random)
                                            (js/Math.random)
                                            100))
                          (shuffle dummy-ids))))))

## Dummy data for status

    (defn reservation [id]
      {:id id
       :until "2016-03-12" })
    (defn arrived [id]
      {:id id
       :until "2016-03-12" })
    (defn borrowed [id]
      {:id id
       :until "2016-03-25" })

    (register-handler
      :request-status
      (fn [db _]
        (assoc db :status
               {:reservations
                (map reservation (take 5 (shuffle dummy-ids)))
                :arrived
                (map arrived (take 5 (shuffle dummy-ids)))
                :borrowed
                (map borrowed (take 5 (shuffle dummy-ids)))

                })))
