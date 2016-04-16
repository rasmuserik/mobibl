(ns solsort.mobibl.leaflet
  (:require
    [cljsjs.leaflet]
    [reagent.core :as reagent]
    [re-frame.core :as re-frame :refer [subscribe dispatch dispatch-sync]]
    [solsort.util :refer [log]]))

(defonce default-marker-icons
  {:default
   (js/L.icon
     (clj->js
       {:iconUrl "./assets/marker-icon.png"
        :iconRetinaUrl "./assets/marker-icon-2x.png"
        :iconSize  [25 41]
        :iconAnchor  [12 38]
        :popupAnchor  [-3 -76]
        :shadowUrl "./assets/marker-shadow.png"
        :shadowRetinaUrl "./assets/marker-shadow.png"
        :shadowSize  [25 45]
        :shadowAnchor  [6 42]}))})

(defn- leaflet-inner
  [{:keys [id pos zoom markers tile-url attribution handler class gc
           marker-icons]
    :or {pos [51.505 -0.09]
         marker-icons default-marker-icons
         scale 13
         markers []
         tile-url "http://{s}.tile.osm.org/{z}/{x}/{y}.png"
         attribution "&copy; OpenStreetMap"}
    :as o}]
  (let [leaflet (atom nil)]
    (reagent/create-class
      {:display-name id
       :reagent-render (fn [] [:div {:id id :class class} "hello" id])
       :component-did-mount
       (fn []
         (reset! leaflet (js/L.map id))
         (.setView @leaflet (clj->js pos) zoom)
         (.addTo (js/L.tileLayer tile-url #js {:attribution attribution})
                 @leaflet)
         (aset js/window "leaflet" @leaflet )
         (-> @leaflet .-attributionControl (.setPrefix "Leaflet"))
         (.on @leaflet "moveend"
              #(let [pos (-> % .-target .getCenter)
                     zoom (-> % .-target .getZoom)]
                 (dispatch-sync
                   [:ui id
                    (-> o
                        (assoc :pos [(.-lat pos) (.-lng pos)])
                        (assoc :zoom zoom))])))
         (doall
           (for [m markers]
             (let [marker
                   (js/L.marker
                     (clj->js (:pos m))
                     #js{:icon (marker-icons(or (:type m) :default))})
                   ]
               (when (:click m) (.on marker "click" (:click m)))
               (.addTo marker @leaflet)))))
       :component-did-update (fn [component])
       :component-will-unmount
       (fn [] (when gc (dispatch [:ui-remove id])))})))

(defn ^:export leaflet [& {:keys [id gc pos pos0 zoom zoom0] :as args}]
  (let [newid (or id (str "leaflet" (.slice  (str  (js/Math.random)) 2)))
        orig @(subscribe [:ui newid])
        o {:id newid
           :gc (if id gc true)
           :pos (or pos (:pos orig) pos0 [55.67 12.57])
           :zoom (or zoom (:zoom orig) zoom0 10)} ]
    (dispatch-sync [:ui (:id o) (into args o)])
    (fn [& {:as args}]
      [leaflet-inner (merge @(subscribe [:ui newid])) args])))
