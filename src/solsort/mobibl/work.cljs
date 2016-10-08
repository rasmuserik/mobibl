(ns solsort.mobibl.work
  (:require [clojure.string :as string]
            [solsort.mobibl.work-types :refer [ns property-mappings types prop-types]]
            [solsort.util :refer [log]]))

(defonce get-work-fn (atom hash-map))
(defn get-work [id] (@get-work-fn id))
(defonce ahref-fn (atom hash-map))
(defn ahref [& args] (apply @ahref-fn args))
(defn styling []
  (let [unit 13
        width (* 4.5 unit)]
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
     :.work
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
      :background "linear-gradient(rgba(255,255,255,0), white)"}}))

;; ### work-tiny
(def work-tiny-height (* 13 5.5))
(defn get-cover [work]
  (first (or (get work "coverUrlFull")
             (get work "coverDataUrl" []))))
(defn work-tiny [pid]
  (let  [o (get-work pid)
         unit 13
         width (* 4.5 unit)]
    [:a (ahref {:page "work" :pid pid}
               {:style {:color "#111"}})
     [:div.center.tinywork
      [:img {:src (get-cover o)
             :width "100%" :height "100%"}]
      [:div.bold (:title o)]
      [:div.condensed (:creator o)]]]))

;; ### work-item
(defn work-item [pid]
  (let [o (get-work pid)
        keywords
        (map
         (fn [kw]
           [:a (ahref {:page "search" :q "" :facets kw}) kw])
         (:keywords o))]
    [:div.work
     [:img {:src (:cover-url o)}]
     [:div [:div.fadeout]
      [:div.bold.large (:title o)]
      [:div.italic.large (:creator o)]
      [:div (:description o)]
      (into
       [:div]
       (interpose ", " (map (fn [s] [:span.condensed.inline-block s])
                            (:keywords o))))]]))

(defn get-properties [work type]
  (let [ks (get prop-types type)]
    (apply
     concat
     (for [k ks] (for [v (get work k [])] [k v])))))
(defn people-list [names type]
  (interpose
   " & "
   (for [name (distinct (map second names))]
     [:a (ahref {:page "search" :q "" :facets [{:type "creator" :term name}]}
                {:property type
                 :typeof "Person"})
      [:span {:property "name"} name]])))
(defn r-if [p v] (if p v ""))
(defn work-view [work-id]
  (log 'a)
  (let
   [work (get-work work-id)
    schema-type (get types (.toLowerCase (first (log (get work "type" [""])))) "missing-type")
    language (:language work)
    subjects (get-properties work :subject)
    location (:location work)
    creators (get-properties work :creator)
    contributers (get-properties work :contributor)]
    (log 'b work schema-type
         language subjects location creators contributers)
    [:div.ui.container
     {:vocab "http://schema.org/"
      :prefix (string/join " " (map (fn [[k v]] (str k ": " v)) ns))
      :resource (str "http://rdf.solsort.com/ting/" work-id)
      :typeof (str schema-type " dcterms:BibliographicResource")}
     [:p]
     [:h1.center {:property "name"} (first (get work "title" []))]
     (r-if (not (empty? creators)) 
           (into [:p.center "af "] (people-list creators "creator")))
     [:p.center ; cover
      (r-if (get-cover work)
        [:img
         {:src (get-cover work)
          :property "image"
          :style
          {:max-height (* 0.5 (- js/document.body.clientHeight 50))
           :max-width (* 0.8 (- js/document.body.clientWidth 20))}}])]
     [:p.center [:a.ui.primary.button (ahref {:page "order" :pid work-id}) "Reservér"]]
     [:a {:property "mainEntityOfPage" :href (str "https://bibliotek.dk/da/work/" (first (get work "pid")))}]
     [:p {:property "description"} (first (get work "abstract"))]
     (r-if (not (empty? contributers))
           (into [:p "Bidrag af: "] (people-list contributers "contributor")))
     (r-if subjects
           (into [:p {:style {:line-height "2rem"}}]
                 (interpose
                  " "
                  (for [[_ word] subjects]
                    [:span {:property "keywords"}
                     [:a.ui.label
                      (ahref
                       {:page "search" :q word :facets []})
                      word]]))))
     (r-if (get work "language")
           [:p [:em "Sprog: "]
            [:span {:property "inLanguage"
                    :content (get work "languageISO6392")}
             (first (get work "language"))]])
     (if location [:p [:em "Opstilling: "] location] "")
     (if (< 1 (count (get work "collection")))
       [:div
        [:p.bold "Udgaver:"]
        (into [:div]
              (map (fn [id] [:span
                             {
                              ;:property "sameAs"
                              ;:resource (str "http://rdf.solsort.com/ting/" id)
                              }
                             (work-tiny id)]) (get work "collection")))]
       "")
     (if-not (empty? (get work "tingRelated"))
       [:div
        [:p.bold "Relaterede:"]
        [:div.ui.grid
         (into 
          [:div.stackable.four.column.doubling.row]
          (map
           (fn [id]
             [:div.column
              {;:property "related"
               ;:resource (str "http://rdf.solsort.com/ting/" id)
               }
              [:a.small
               (ahref {:page "work" :pid id}
                      {:style
                       {:display :inline-block
                        :height "6em"}})
               (work-item id)]])
           (take 12 (remove #{(:pid work)} (get work "tingRelated")))))]]
       "")
     [:div
      [:h3 "Data"]]
     [:table
      (into
       [:tbody]
       (for [[k v] (sort (filter #(string? (first %)) work))]
         [:tr
          [:th {:style {:font-size "70%"
                        :text-align :right
                        :vertical-align :top
                        :padding-right 5}} k]
          (into [:td {:style {:padding-bottom 5}}]
                (interpose ", "
                           (map (fn [s] [:span {:property (get property-mappings k "misc:unhandled")} (str s)]) v)))]))]]))
