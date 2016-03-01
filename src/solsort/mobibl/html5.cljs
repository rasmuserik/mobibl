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
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

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
       {:background "url(assets/background.jpg)"
        :background-color "#fbf8f4"}
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
    ;; ### Tabbar
    (load-style!
      {".tabbar"
       {:position :fixed
        :box-sizing :border-box
        :bottom 0
        :left 0
        :width "100%"
        :background "url(assets/background.jpg)"
        :background-color "#fbf8f4"
        :box-shadow "-1px 0px 5px rgba(0,0,0,1);"
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
    ;; ### Book
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
   [:img {:src (str "assets/" id "-icon.svg")
          :alt s}]])

(defn tabbar []
  [:div.tabbar
   [tabbar-button "search" "Søg"]
   [tabbar-button "work" "Materiale"]
   [tabbar-button "library" "Bibliotek"]
   [tabbar-button "status" "Status"]])

;; ### Search
;; <img width=20% align=top src=doc/wireframes/search.jpg>

(defn search [query]
  [:div
   [tabbar]
   [:input {:value query}]
   "..."])

;; ### Work
;; <img width=20% align=top src=doc/wireframes/work.jpg>

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


;; ### Library
;; <img width=20% align=top src=doc/wireframes/library.jpg>

(defn library [library]
  [:div
   [tabbar]
   [:h1 library]
   "..."])

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
  (let [[page id] @(subscribe [:route])]
    (case page
      "search" [search id]
      "work" [work id]
      "library" [library id]
      "status" [status]
      [search ""])))

;; ## Execute and events

(render [app])

;; ## Routing

(defn handle-hash []
  (dispatch [:open (string/split (.slice js/location.hash 1) "/")]))
(defn open [& args]
  (aset js/location "hash" (string/join "/" args)))
(js/window.addEventListener "hashchange" handle-hash)
(handle-hash)
