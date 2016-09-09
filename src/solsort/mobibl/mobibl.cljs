(ns solsort.mobibl.mobibl
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop alt!]]
   [reagent.ratom :as ratom :refer  [reaction]])
  (:require
   [cljs.reader]
   [solsort.toolbox.appdb :refer [db db! db-async!]]
   [solsort.toolbox.query-route :as route]
   [solsort.mobibl.work :refer [work-tiny work-item work]]
   [solsort.toolbox.ui :refer [input]]
   [solsort.mobibl.data :refer [get-work get-search get-suggest get-facets]]
   [solsort.util
    :refer
    [<ajax <seq<! js-seq load-style! put!close!
     parse-json-or-nil log page-ready render dom->clj]]
   [reagent.core :as reagent :refer []]
   [re-frame.core :as re-frame
    :refer [register-sub subscribe register-handler dispatch dispatch-sync]]
   [clojure.string :as string :refer [replace split blank?]]
   [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]
   [solsort.toolbox.leaflet :refer [openstreetmap]]
   [cljsjs.hammer]))

;; ## Styling
;;
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
      {; :background "url(assets/background.jpg)"
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
      ".condensed" {:font-family "\"Open Sans Condensed\" !important"}}
     "general styling")
    ;; ### Tabbar
    (load-style!
     {".tabbar-spacer"
      {:display :inline-block
       :height 50}
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
       :z-index "100"}
      ".tabbar a"
      {:display :inline-block
       :box-sizing :border-box
       :width (* 0.25 (- js/document.body.clientWidth 100))
       :text-align :center}
      ".tabbar img"
      {:padding 3
       :height 44
       :width 44}}
     "tabbar-styling")
    ;; ### Library view
    ;;
    ;; FIXME Not so nice to have the style for bib-map defined here
    ;;
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

;; (defn restore-scroll [route]
;;   (set! js/document.body.scrollTop (get (db [:ui :scroll]) route 0)))

;; (defonce handle-scroll
;;   (fn [] (db-async! [:ui :scroll]
;;                     (assoc (or (db [:ui :scroll]) {})
;;                            @(subscribe [:route])
;;                            js/document.body.scrollTop))))
;; (js/window.removeEventListener "scroll" handle-scroll)
;; (js/window.addEventListener "scroll" handle-scroll)

;; ### Tab bar - menu in bottom of the screen

(defn tabbar-button [id s]
  [:a (route/ahref {:page (name id)})
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

;; ### Facet view
(defn facet-color [s]
  (case s
    "audience" "red"
    "audienceCategory" "yellow"
    "literaryForm" "yellow"
    "category" "yellow"
    "subject" "orange"
    "dk5" "orange"
    "creator" "green"
    "period" "green"
    "date" "green"
    "type" "teal"
    "title" "violet"
    "fictionSubject" "blue"
    "musicSubject" "blue"
    "nonFictionSubject" "blue"
    "form" "violet"
    "genre" "purple"
    "genreCategory" "purple"
    "geographic" "brown"
    "level" "gray"
    "language" "olive"
    ""))

;; ### Search
;; <img width=20% align=top src=doc/wireframes/search.jpg>

(defn add-facet [o]
  (let [o (select-keys o [:type :term])]
    (db! [:history :facets]
         (conj (remove #{o} (db [:history :facets] [])) o))
    (db! [:route :facets] (conj (db [:route :facets]) o))))
(defn selected-facet? [o]
  (some #(= % (select-keys o [:type :term])) (db [:route :facets] [])))

(defn active-facet [o]
  [:a.ui.small.button.condensed.bold
   {:on-click #(db! [:route :facets] (remove #{o} (db [:route :facets] [])))
    :key (hash o)
    :class (facet-color (:type o))}
   (:term o)])

(defn facet [o]
  (if (contains? (db [:route :facets])
                 (select-keys o [:type :term]))
    ""
    [:a.ui.small.basic.button.condensed.bold
     {:on-click #(add-facet o)
      :key (hash o)
      :class (facet-color (:type o))}
     (:term o)
     (if (:frequency o)
       [:span.small.regular " " (str (:frequency o)) ""]
       "")]))

(defn suggestion-list [s]
  (map facet (remove selected-facet? (remove nil? s))))
(defn cql-cleanup [s]
  (string/replace
   (string/trim s)
   #"[\"&]"
   ""))
(defn search-query "transform the search-route into cql" []
  (let [q (map #(str "\"" % "\"")
               (string/split
                (cql-cleanup (db [:route :q] ""))
                #" +"))
        q (if (= "\"\"" (first q)) [] q)
        facets (db [:route :facets] [])
        facets (map #(assoc % :cql
                            (str (if (= "title" (:type %))
                                   "ti"
                                   (str "facet." (name (:type %))))
                                 "=\""
                                 (cql-cleanup (:term %))
                                 "\""))
                    facets)
        facets (vals (group-by :type facets))
        facets (map
                #(str "("
                      (string/join " or " (map :cql %))
                      ")")
                facets)
        cql (string/join " and " (concat q facets))
        cql (if (and (empty? q) (= 1 (count facets)))
              (.slice cql 1 -1)
              cql)]
    cql))

(defn facets-div [l]
  (log 'facet-div l)
  (into [:div {:style {:height "2.3em"}}] l))
(defn suggestions []
  (let [s (get-suggest (db [:route :q] ""))]
    [:div
      [facets-div
       (concat (map active-facet (db [:route :facets] []))
               (suggestion-list (db [:history :facets])))]
      [facets-div (suggestion-list
                   (distinct (interleave (:title s) (:creator s) (:subject s))))]
      [facets-div (suggestion-list (keep-indexed #(if (even? %1) %2 nil) (distinct (get-facets (search-query)))))]
      [facets-div (suggestion-list (keep-indexed #(if (odd? %1) %2 nil) (distinct (get-facets (search-query)))))]]))
(defn search [query]
  (let
   [result-pids (get-search (search-query) 0)
    results
    (map
     (fn [pid]
       [:a.column (route/ahref {:page "work" :pid pid}
                               {:key pid})
        [:div
         {:style {:height "9rem"
                  :color :black
                  :margin-bottom "1rem"
                  :box-shadow "2px 2px 5px 0px rgba(0,0,0,0.1)"}}
         [work-item pid]]])
     result-pids)
    show-history (db [:ui :show-history])
    search-history [] ; TODO
    suggest (when show-history search-history)
    search-str (or (first (filter string? query)) "")
    active-facets (remove string? query)
    facet-history (or (db [:ui :facet-history]) [])
    facets [] ;@(subscribe [:facets :sample])
]
    [:div
     [:div.ui.container
      [:h1 "Mobibl"]
      [:p {:style {:color :red}} "Under udvikling, ikke fuldt funktionel endnu."]
      [:div
       [:div.ui.search.fluid.input.left.icon
        [:i.search.icon]
        [input {:db [:route :q]
         :placeholder "Indtast søgning"}]
        (when suggest
          (into [:div.results.transition.visible
                 {:style {:display "block !important"}}]
                (for [[s facets] suggest]
                  (into
                   [:a.result
                    {:href (route/url {:page "search" :q s})
                     :on-click #(db! [:ui :show-history] false)}
                    s " "]
                   (for [[col f] facets]
                     [:div.ui.small.label
                      {:class (facet-color col)}
                      (str f)])))))]]]

     ;; Facet view
     [:div
      {:style {:white-space :nowrap
               :overflow-y :hidden
               :overflow-x :auto
               :margin-top "0.2rem"
               :margin-bottom "1rem"
               :margin-left "0.5%"
               :width "99.5%"
               :display :inline-block}}
      [suggestions]
      #_(merge
         [:div]
         (map (fn [[col s :as facet]]
                [:a.ui.small.button.condensed.bold
                 {:on-click
                  (fn []
                    (db-async! [:ui :facet-history]
                               (conj facet-history facet))
                    (db! [:route :facets] (remove #{[col s]} (db [:route :facets]))))
                  :key (hash [col s])
                  :class (facet-color col)} s])
              active-facets)
         (map (facet search-str active-facets) facet-history))
      #_(merge
         [:div]
         (map (facet search-str active-facets) facets))]
     [:p]
     [:div.ui.container
      [:div.ui.grid
       (merge [:div.stackable.doubling.four.column.row]
              results)]]
     ]))

;; ### Library
;; <img width=20% align=top src=doc/wireframes/library.jpg>

(def daynames ["Man" "Tir" "Ons" "Tor" "Fre" "Lør" "Søn"])

(defn geo->pos [geo] [(get geo "latitude") (get geo "longitude")])
(defn bib->pos [bib] (geo->pos (get bib "geolocation")))
(defn library [id]
  (let [bib (db [:libraries (db [:route :id] "715100")])]
    (let [address (:address bib)
          hours   (:hours bib)
          phone   (:phone bib)]
      [:div
       (if (or (not bib)
               (empty? (db [:libraries])))
         [:div]
         (into
          [openstreetmap
           {:class "map"
            :db ["leafletdiv"]
            :pos0 (bib->pos bib)
            :zoom0 12}]
            (doall
             (map (fn [[id bib]]
                    [:marker  {:pos (bib->pos bib)
                               :click #(route/open {:page "library" :id id})}])
                  (db [:libraries])))))
       [:div.ui.container
        [:h1 (first (get bib "branchShortName"))]]
       [:div.ui.container
        [:div.address
         [:h2 "Adresse"]
         [:div (first (get bib "branchName"))]
         [:div (get bib "postalAddress")]
         [:div (get bib "postalCode") " " (get bib "city")]
         [:div (:country address)]]
        [:div.open
         [:h2 "Åbningstider"]
         [:pre (first (get bib "openingHours"))]]
        [:div.contact
         [:h2 "Kontakt"]
         [:div
          [:span "Email: "]
          [:span (get bib "branchEmail")]]
         [:div
          [:span "Telefon: "]
          [:span (get bib "branchPhone")]
          " "
          [:span (:time phone)]]]]
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
            :width "30%"}}]
         content)
   [:a
    (route/ahref {:page "work" :pid id}
                 {:style
                  {:display :inline-block
                   :font-size "70%"
                   :vertical-align :top
                   :width "70%"
                   :height "4rem"}})
    [work-item id]]])

(defn status []
  [:div.ui.container
   [:h1 "Lånerstatus"]
   [:p {:style {:color :red}} "Ikke implementeret endnu."]
   ])

(defn old-status []
  (let [arrived [] ;(subscribe [:arrived])
        borrowed   [] ;          (subscribe [:borrowed])
        reservations  [];       (subscribe [:reservations])
]
    (fn []
      [:div.ui.container
       [:div.right.floated.ui.primary.button "Log ud"]
       [:h1 "Lånerstatus"]
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
            [:div.ui.small.button "Forny"])))]
       [:p
        [:h2 "Bestillinger"]
        (into
         [:div]
         (for [r @reservations]
           (loan-entry
            (:id r)
            [:div.ui.small.button "Slet"])))]])))

;; ### Main App entry point
(defn app []
  (let [prev-route (atom)]
    (fn []
      (let [page (db [:route :page] "search")]
        (db! [:route] (into (db [:history page] {}) (db [:route])))
        (db! [:history page] (db [:route]))
        [:div
         (case (db [:route :page] "search")
           "search" [search (apply concat (db [:route :q] "") (db [:route :facets] [])) false]
           "work" [work (db [:route :pid])]
           "library" [library (db [:route :id] "710100")]
           "status" [status]
           [search ""])
         [tabbar]]))))

;; ## Swipe gestures

;; (defonce routes #js [:search :work :library :status])

;; (defn change-route [delta]
;;   (let [n (+ delta (.indexOf routes (first @(subscribe [:route]))))
;;         n (max 0 (min (dec (.-length routes)) n))]
;;     (dispatch [:route (aget routes n)])))
;; (defn addSwipeGestures []
;;   (let [hammer (js/Hammer.Manager. js/document.body)
;;         swipe  (js/Hammer.Swipe.)]
;;     (.add hammer swipe)
;;     (.on hammer "swipeleft" #(change-route 1))
;;     (.on hammer "swiperight" #(change-route -1))))
;; (defonce register-swipe (addSwipeGestures))


#_(defonce handle-hash
    (fn []
      (if (empty? js/location.hash)
        (dispatch [:route])
        (dispatch (into [:route]
                        (cljs.reader/read-string
                         (.slice js/location.hash 1)))))))

;(js/window.removeEventListener "hashchange" handle-hash)
;(js/window.addEventListener "hashchange" handle-hash)
;(handle-hash)

;; ## Execute and events

;(render [:div [app]])
(defn main []
  [:div
   ;[:h1 "hello"]
   ;[input [:route :input]
   [app]])
(render [:div [main]])
(db)
