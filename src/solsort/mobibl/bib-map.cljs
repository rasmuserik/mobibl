(ns solsort.mobibl.bib-map
    (:require [cljsjs.leaflet]
              [reagent.core :as reagent :refer [create-class props dom-node]]
              [re-frame.core :as re-frame :refer [subscribe dispatch]]
              [solsort.mobibl.secret :refer [mapbox]]
              [solsort.util :refer [log]]))
 
;; Default mapbox url

(def mapbox-url
  (str
   "https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png"
   "?access_token={accessToken}"))


;; Mapbox config is merged with local secret data to avaoid exposing the mapbox
;; API key

(def mapbox-conf
  (clj->js
   (merge
    mapbox
    {:attribution
     (str "Map data &copy; <a href=\"http://openstreetmap.org\">OpenStreetMap"
          "</a> contributors, <a href=\"http://creativecommons.org/"
          "licenses/by-sa/2.0/\">CC-BY-SA</a>, Imagery © "
          "<a href=\"http://mapbox.com\">Mapbox</a>")
     :maxZoom 18
     :zoom 13})))


;; Set a custom marker for the map.  Parameters are not completely correct.

(def mapbox-marker
  (.icon js/L 
         (clj->js
          {:iconUrl
           "http://cdn.leafletjs.com/leaflet/v0.7.7/images/marker-icon.png"
           :iconRetinaUrl
           "http://cdn.leafletjs.com/leaflet/v0.7.7/images/marker-icon-2x.png"
           :iconSize [25 41]
           :iconAnchor [12 38]
           :popupAnchor [-3 -76]
           :shadowUrl
           "http://cdn.leafletjs.com/leaflet/v0.7.7/images/marker-shadow.png"
           :shadowRetinaUrl
           "http://cdn.leafletjs.com/leaflet/v0.7.7/images/marker-shadow.png"
           :shadowSize [25 45]
           :shadowAnchor [6 42]})))

(defn- bib-map-comp [id pos]
  (let [bib-map (atom nil)
        update (fn [id pos]
                   (.addTo 
                    (.marker js/L
                             (clj->js pos)
                             (clj->js {:icon mapbox-marker}))
                    @bib-map)
                   (.setView @bib-map (clj->js pos) 14))]
    (create-class
     {:display-name (str "bib-map-" id)
      :reagent-render (fn []
                          [:div {:id id
                                 :on-wheel #(log "scroll")
                                 :style {:height "200px"
                                         :width "300px"}}])
      :component-did-mount (fn []
                               (let [map-handle (.map js/L id)]
                                 (reset! bib-map map-handle)
                                 (.addTo
                                  (.tileLayer js/L mapbox-url mapbox-conf)
                                  map-handle)
                                 (update id pos)))
      :component-did-update #(do
                               (log "Updated" %1)
                               (update id pos))})))

(defn ^:export bib-map [& {:keys [id pos]}]
  (fn []
      [bib-map-comp id pos]))
