[![Stories in Ready](https://badge.waffle.io/solsort/mobibl.png?label=ready&title=Ready)](https://waffle.io/solsort/mobibl)

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
        [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

## Handlers

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
## Subscriptions

    (def default-work
      {:title "Unknown Title"
       :creator "Unknown Creator"})

    (defn get-work [db id]
      (let [work (get-in db [:works id])]
        (when-not work (dispatch [:request-work id]))
        (merge default-work {:id id} work)))

    (register-sub
      :search
      (fn [db [_ q page]]
        (reaction
          (let [results (get-in @db [:search q page])]
            (or results
               (do
                (dispatch [:request-search q page])
                []))))))

    (register-sub :work (fn [db [_ id]] (reaction (get-work @db id))))
    (register-sub :works (fn [db] (reaction (:works @db))))
    (register-sub :route (fn [db] (reaction (get @db :route))))
    (register-sub :db (fn [db] (reaction @db)))


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

## Data initialisation

TODO: also run on network reconnect, and after a while

    (dispatch [:request-status])

TODO: sync database to disk, and restore on load
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
    (def highlight "#326bc5")
    ;(def background fade from "#eaeaea" to "#ffffff")
    (def dark "#262626")
    (def light "#e5e5e5")
    (def medium "#d8d8d8")
    (defn styling []

We are designing for mobile-portrait-mode,
which can be enforced in the packaged cordova-app.

Using a unit-size of 1/24 of the mobile portrait width,
means that a clickable object should preferibly
be 4 units high/wide, though 3 units is also ok.

It also allows for 1/2, 1/3, 1/4, and 1/6 division of the screen,
and 5/8 vs 3/8 which approximately the golden ratio.

      (let [unit (/ (js/Math.min js/document.body.clientHeight
                                 js/document.body.clientWidth)
                    24)]
        (load-style!
          {:body
           {:background "url(assets/background.jpg)"
            :background-color "#fbf8f4"
            :font-family "\"Open Sans\", sans-serif"
            :font-weight "300"}
           ".condensed"
           {:font-family "\"Open Sans Condensed\""}
           ".ssbutton"
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
          {".tabbar-spacer"
           {:display :inline-block
            :height (* 4 unit)
            }
           ".tabbar"
           {:position :fixed
            :box-sizing :border-box
            :bottom 0
            :text-align :center
            :left 0
            :width "100%"
            :background "url(assets/background.jpg)"
            :background-color "#fbf8f4"
            :box-shadow "-1px 0px 5px rgba(0,0,0,1);"
            :z-index "100"
            }
           ".tabbar a"
           {:display :inline-block
            :box-sizing :border-box
            :width (* 6 unit)
            :text-align :center}
           ".tabbar img"
           {:padding (* 0.5 unit)
            :height (* 4 unit)
            :width (* 4 unit)}
           "body"
           {:padding-bottom (* 4 unit)}
           }
          "tabbar-styling")
### Book
        (load-style!
          {".work"
           {:margin-left unit
            :margin-right unit }
           ".work-cover-img"
           {:float :right
            :max-width "62%"
            :max-height (- js/window.innerHeight (* 4 unit)) }
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
       [:img {:src (str "assets/" id "-icon.svg")
              :alt s}]])

    (defn tabbar []
      [:div
       [:div.tabbar-spacer " "]
       [:div.tabbar
       [tabbar-button "search" "Søg"]
       [tabbar-button "work" "Materiale"]
       [tabbar-button "library" "Bibliotek"]
       [tabbar-button "status" "Status"]]])

### Search
<img width=20% align=top src=doc/wireframes/search.jpg>

    (defn work-line [pid]
      (let [o @(subscribe [:work pid])
            keywords
            (interpose
              " "
              (map
                (fn [kw] [:a.condensed.button {:href (str "#search/" kw)} kw])
                (:keywords o)
                ))]
        [:a
         {:key pid
          :href (str "#work/" pid)}
         [:div.row.callout
          [:div.large-1.medium-2.small-3.columns
           [:img {:src (:cover-url o)}]
           ]
          [:div.large-11.medium-10.small-9.columns
           [:h4 (:title o)]
           [:div.expanded.hollow.button (:creator o)]
           (into [:div] keywords)]]]))

    (defn search [query]
      (let
        [results @(subscribe [:search query 0])
         results (map work-line results)
         search-form
         [:div.row
          [:div.small-12.columns
           [:div.input-group
            [:input.input-group-field
             {:type :text
              :value query
              :on-change
              #(dispatch-sync [:route "search" (-> % .-target .-value)])}]
            [:a.input-group-button.button "søg"]]]]
         ]
        (log 'search-results query results)
        [:div
         [:p]
         (merge [:div search-form]
          results)
         [tabbar]
         ]))

### Work
<img width=20% align=top src=doc/wireframes/work.jpg>

    (defn work [work-id]
      (let [work @(subscribe [:work work-id])
            language (:language work)
            keywords (:keywords work)
            location (:location work)
            creator (:creator work)]
        [:div.work
         [:div "TODO: Work history here"]
         [:h1.text-center (:title work)]
         [:div.text-center "af " [:a {:href (str "#search/" creator)} creator]]
         [:img.work-cover-img.float-right {:src (:cover-url work)}]
         [:div [:a.button "Bestil"]]
         (if-not keywords ""
           (into [:p #_[:em "Emne: "]]
                 (interpose
                   " "
                   (for [word keywords]
                     [:a.hollow.condensed.button {:href
                                       (str "#search/" word)} word]))))
         [:div.work-desc (:description work)]
         (if language [:p [:em "Sprog: "] language] "")
         (if location [:p [:em "Opstilling: "] location] "")
         [tabbar]
         ]))


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
      (dispatch (into [:route] (string/split (.slice js/location.hash 1) "/"))))
    (js/window.addEventListener "hashchange" handle-hash)
    (handle-hash)
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
          (doall
            (for [o mockdata]
              (dispatch [:work (o "_id") (convert-bibentry o)]))))))

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
