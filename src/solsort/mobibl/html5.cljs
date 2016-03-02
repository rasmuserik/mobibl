;; # HTML5 view (html5.cljs)
;;
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
    [solsort.mobibl.mobibl]
    [re-frame.core :as re-frame
     :refer [register-sub subscribe register-handler dispatch dispatch-sync]]
    [clojure.string :as string :refer [replace split blank?]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]
    [solsort.mobibl.bib-map :refer [bib-map]]))


;; ## Styling
;;

(load-style! normalize-css "style-reset")
(defn styling []
  ;;
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
  (let [unit (/ js/document.body.clientWidth 24)]
    (load-style!
      {:body
       {:background "#fff8f8"
        }
       "div,a,span,b,i,img"
       {:box-sizing :border-box}}
      "general styling"
      )
    ;; ### Styling for the
    (load-style!
      {".tabbar"
       {:position :fixed
        :box-sizing :border-box
        :bottom 0
        :left 0
        :width "100%"
        :background-color :white
        :border-top "1px solid black"
        :z-index 1000
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
      "topbar-styling")
    ))

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
;; ### Tab bar - menu in bottom of the screen

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

;; ### Search
;; <img width=20% align=top src=doc/wireframes/search.jpg>

(defn search []
  [:div
   [tabbar]
   [:input {:value @(subscribe [:current-query])}]
   "..."])

;; ### Work
;; <img width=20% align=top src=doc/wireframes/work.jpg>

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


;; ### Library
;; <img width=20% align=top src=doc/wireframes/library.jpg>

(defn library []
  (let [current-library (subscribe [:current-library])]
    (fn []
        (log "Cur Lib" @current-library)
        [:div
         {:style {:z-index 1000}}
         [tabbar]
         [:h1 (:name @current-library)]
         [bib-map
          :id "bib-map"
          :pos (:position @current-library)]])))

;; ### Status
;; <img width=20% align=top src=doc/wireframes/patron-status.jpg>

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
              ;;
              ;; **TODO** Add unique creator ID
              ;;
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
              ;;
              ;; **TODO** It would be nice with thumbnails
              ;;
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

;; ### Main App entry point
(defn app []
  (case (first @(subscribe [:route]))
    "search" [search]
    "work" [work]
    "library" [library]
    "status" [status]
    [search]))

;; ## Execute and events

(render [app])

;; ## Routing

(defn handle-hash []
  (dispatch [:open (string/split (.slice js/location.hash 1) "/")]))
(defn open [& args]
  (aset js/location "hash" (string/join "/" args)))
(js/window.addEventListener "hashchange" handle-hash)
(handle-hash)
