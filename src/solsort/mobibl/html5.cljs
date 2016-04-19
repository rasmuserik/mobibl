;; # HTML5 view (html5.cljs)
;;
(ns solsort.mobibl.html5
  (:require-macros
    [cljs.core.async.macros :refer [go go-loop alt!]]
    [reagent.ratom :as ratom :refer  [reaction]])
  (:require
    [cljs.reader]
    [solsort.util
     :refer
     [<ajax <seq<! js-seq normalize-css load-style! put!close!
      parse-json-or-nil log page-ready render dom->clj]]
    [reagent.core :as reagent :refer []]
    [re-frame.core :as re-frame
     :refer [register-sub subscribe register-handler dispatch dispatch-sync]]
    [clojure.string :as string :refer [replace split blank?]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]
    [solsort.mobibl.leaflet :refer [leaflet]]
    [cljsjs.hammer]
    [goog.string :refer [unescapeEntities]]))

(declare route-link)

;; ## Styling
;;
(load-style! normalize-css "style-reset")
(def highlight "#326bc5")
;(def background fade from "#eaeaea" to "#ffffff")
(def dark "#262626")
(def light "#e5e5e5")
(def medium "#d8d8d8")
(defn styling []
  ;; ### general styling
  ;; We are designing for mobile-portrait-mode,
  ;; which can be enforced in the packaged cordova-app.
  ;;
  ;; Using a unit-size of 1/24 of the mobile portrait width,
  ;; means that a clickable object should preferibly
  ;; be 4 units high/wide, though 3 units is also ok.
  ;;
  ;; It also allows for 1/2, 1/3, 1/4, and 1/6 division of the screen,
  ;; and 5/8 vs 3/8 which approximately the golden ratio.
  ;;
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
    ;; ### Tabbar
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
    ;; ### Library view
    ;;
    ;; FIXME Not so nice to have the style for bib-map defined here
    ;;
    (load-style!
      {".map"
       {:height (js/Math.min js/document.body.clientWidth
                             (* 0.6 js/document.body.clientHeight))}
       "table.openhours th"
       {:text-align "left"
        :padding "0em 0.8em 0em 0em"}
       "table.openhours tbody td"
       {:text-align "center"}
       ".contact"
       {:padding "0em 0em 10em 0em"
        ".contact div span"
        {:margin "0em 1em 0em 0em"
         :border "1px solid blue"}}}
      "library-styling")))

;; ### tinywork

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

;; ### work

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

;; ### Actually apply styling
;;
; re-layout on rotation etc.
(js/window.addEventListener "resize" styling)
; re-layout when everything has loaded, to account for
; possible change of width due to appearing scrollbar
(js/window.addEventListener "load" #(js/setTimeout styling 0))
; re-layout on load, and on figwheel reload
(styling)

;; ## Components
;; ### Component for remembering scroll position per route

(defn restore-scroll [route]
  (set! js/document.body.scrollTop (get @(subscribe [:ui :scroll]) route 0)))

(defonce handle-scroll
  (fn [] (dispatch [:ui :scroll
                    (assoc (or @(subscribe [:ui :scroll]) {})
                           @(subscribe [:route])
                           js/document.body.scrollTop)])))
(js/window.removeEventListener "scroll" handle-scroll)
(js/window.addEventListener "scroll" handle-scroll)
;; ### Tab bar - menu in bottom of the screen

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

;; ### work-tiny

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

;; ### work-item
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

;; ### Facet view
(defn facet-color [s]
  (case s
    :creator "orange"
    :type "olive"
    :language "teal"
    :subject "violet"
    :year "pink"
    "" ))

;; ### Search
;; <img width=20% align=top src=doc/wireframes/search.jpg>

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

         ;; Facet view
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
            (map (facet search-str active-facets) facet-history) 
            )
          (merge
            [:div]
            (map (facet search-str active-facets) facets))]
         [:div.ui.container
          [:div.ui.grid
          (merge [:div.stackable.doubling.four.column.row]
                 results)]]
         [tabbar]]))

;; ### Work
;; <img width=20% align=top src=doc/wireframes/work.jpg>

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

;; ### Library
;; <img width=20% align=top src=doc/wireframes/library.jpg>

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
         [:table.openhours
          [:thead
           (into
             [:tr [:th]]
             (for [title (map :title hours)]
               [:th title]))]
          (into [:tbody]
                (for [day (range 7)]
                  (into [:tr
                         [:th (get daynames day)]]
                        (for [area (map :weekdays hours)
                              :let [time (get area day)]]
                          (into [:td]
                                [(if (nil? time)
                                   "Lukket"
                                   (let [t0 (get time 0)
                                         t1 (get time 1)]
                                     (str (if (< t0 10)
                                            (unescapeEntities "&nbsp;")
                                            "")
                                          t0 " - " t1)))])))))]]
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

;; ### Status
;; <img width=20% align=top src=doc/wireframes/patron-status.jpg>
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


;; ### Main App entry point
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

;; ## Swipe gestures

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


;; ## Routing

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
;; ## Execute and events

(render [:div [app]])
