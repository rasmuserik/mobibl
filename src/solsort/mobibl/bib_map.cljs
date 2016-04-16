(ns solsort.mobibl.bib-map
  (:require [cljsjs.leaflet]
            [reagent.core :as reagent :refer [create-class props dom-node]]
            [re-frame.core :as re-frame :refer [subscribe dispatch]]
            [solsort.util :refer [log]]))

;; Default tile url
;;
;; According to MapQuest: Free access to open tiles will go away at some point,
;; See http://wiki.openstreetmap.org/wiki/MapQuest#MapQuest-hosted_map_tiles
;;
(def tile-url "http://otile1.mqcdn.com/tiles/1.0.0/map/{z}/{x}/{y}.jpg")

;; See http://hello.mapquest.com/attributions
(def map-conf
  (clj->js
    {:attribution
     (str "&copy; OpenStreetMap, Mapbox")
     :maxZoom 18
     :zoom 13}))

;; Set a custom marker for the map.  Parameters are not completely correct.
(def map-marker
  (.icon
    js/L
    (clj->js
      {:iconUrl "./assets/marker-icon.png"
       :iconRetinaUrl "./assets/marker-icon-2x.png"
       :iconSize [25 41]
       :iconAnchor [12 38]
       :popupAnchor [-3 -76]
       :shadowUrl "./assets/marker-shadow.png"
       :shadowRetinaUrl "./assets/marker-shadow.png"
       :shadowSize [25 45]
       :shadowAnchor [6 42]})))

(defn- bib-map-comp [id pos]
  (let [bib-map (atom nil)
        update (fn [id pos]
                 (.addTo
                   (.marker js/L
                            (clj->js pos)
                            (clj->js {:icon map-marker}))
                   @bib-map)
                 (.setView @bib-map (clj->js pos) 14))]
    (create-class
      {:display-name (str "bib-map-" id)
       :reagent-render (fn []
                         [:div {:id id}])
       :component-did-mount (fn []
                              (let [map-handle (.map js/L id)]
                                (reset! bib-map map-handle)
                                (.addTo
                                  (.tileLayer js/L tile-url map-conf)
                                  map-handle)
                                (update id pos)))
       :component-did-update #(do
                                (log "Updated" %1)
                                (update id pos))})))

(defn ^:export bib-map [& {:keys [id pos]}]
  (fn []
    [bib-map-comp id pos]))
