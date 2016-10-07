(ns solsort.mobibl.work
  (:require [clojure.string :as string]
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
             (get work "coverDataUrl" [])))
  )
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

(defonce reverse-types
  {"Book bib:AudioBook" ["lydbog (bånd)" "lydbog (net)" "spolelydbånd" "lydbog" "lydbog (cd)" "lydbog (cd-mp3)"]
   "PublicationIssue bib:ComicIssue" ["tegneserie"]
   "Book bib:GraphicNovel" ["graphic novel"]
   "VideoGame" ["pc-spil" "playstation 3" "playstation 2" "xbox 360" "wii" "nintendo ds" "playstation 4" "wii u" "playstation" "xbox one" "pc-spil (net)" "xbox" "gameboy advance" "psp" "playstation vita" "html5-spil" "gameboy"]
   "SoftwareApplication" ["cd-rom" "dvd-rom" "diskette"] 
   "PublicationIssue" ["periodikum" "periodikum (net)" "tidsskrift (net)"]
   "Map" ["kort" "e-kort"] 
   "CreativeWorkSeries" ["avis" "avis (net)" "serie" "tidsskrift"]
   "Movie" ["dvd" "video" "blu-ray" "betamax" "film (net)" "biograffilm" "film"]
   "MusicComposition" ["node" "sang" "e-node"]
   "MusicAlbum" ["cd (musik)" "grammofonplade" "bånd" "musik (net)"]
   "CreativeWork" ["cd"  "cd-rom (mp3)" "netdokument" "sammensat materiale" "diverse" "grafisk blad" "legetøj" "mikroform" "udstilling" "spil" "dias" "teateropførelse" "plakat" "puslespil" "originalkunst" "genstand" "lydspor" "akvarel" "laborativt materiale" "transparent" "elektronisk materiale" "planche" "billedkort" "fotoreproduktion" "emnekasse" "foto" "dcc-bånd" "materiale til indlæringsapparat" "billedbånd" "ordkort" "tegning" "måleapparat" "flonellografmateriale" "teaterdukke" "cd-i" "postkort" "arkitekturtegning" "kunstreproduktion" "maleri" "flipover-materiale" "mini disc" "øvelsesmodel" "fastplade" "billedtæppe" "foto-cd" "teknisk tegning" "udstillingsmontage" "symbolkort"]
   "Article" ["avisartikel" "artikel" "tidsskriftsartikel"]
   "Book" ["årbog" "bog" "ebog" "billedbog" "punktskrift" "bog stor skrift"]})
(defonce types
  (into {}
        (apply concat
               (map (fn [[k vs]] (map (fn [v] [v k]) vs)) reverse-types))))
(def prop-types
  {:uncategorised
   ["date" "extent" "format" "abstract" "description" "descriptionSeries"
    "accessType" "activity" "alternative" "audienceAge" "audience"
    "audienceMedieraad" "audiencePegi" "collection" "collectionDetails"
    "version" "multiVolumeType" "publisher" "referencesISBN" "rights"
    "shelfMusicshelf" "acSource" "source" "typeBibDKType" "type" "workType"]
   :ignore ["creatorSort"]
   :cover-url
   ["coverUrlFull"]
   :language
   ["language" "dcLanguage" "languageISO6392" "languageSpoken" "languageSubtitles"]
   :contributor
   ["contributorAct" "contributorAft" "contributorAnm" "contributorAnt" "contributorArc"
    "contributorArr" "contributorArt" "contributorAud" "contributorAui" "contributorAus"
    "contributorAut" "contributorCcp" "contributorChr" "contributorClb" "contributorCli"
    "contributorCll" "contributorCmm" "contributorCmp" "contributorCnd" "contributorCng"
    "contributorCom" "contributorCre" "contributorCtb" "contributorCtg" "contributorCur"
    "contributorCwt" "contributorDkani" "contributorDkbea" "contributorDkdes"
    "contributorDkfig" "contributorDkfvl" "contributorDkind" "contributorDkmdt"
    "contributorDkmed" "contributorDkmon" "contributorDkops" "contributorDkref"
    "contributorDkste" "contributorDktek" "contributorDktil" "contributorDkved"
    "contributorDrm" "contributorDte" "contributorEdt" "contributorFmo" "contributorHnr"
    "contributor" "contributorIll" "contributorInv" "contributorIsb" "contributorItr"
    "contributorIve" "contributorIvr" "contributorLbt" "contributorLtg" "contributorLyr"
    "contributorMus" "contributorNrt" "contributorOrm" "contributorOth" "contributorPbl"
    "contributorPht" "contributorPrd" "contributorPrf" "contributorPro"
    "contributorPROVIDERID" "contributorPrt" "contributorRce" "contributorRes"
    "contributorRev" "contributorScl" "contributorSds" "contributorSng" "contributorSpk"
    "contributorStl" "contributorTrl" "contributorWdc"]
   :creator
   ["creatorActPeriod" "creatorAnt" "creatorArr" "creatorArt" "creatorAus" "creatorAut"
    "creatorCcp" "creatorCmp" "creatorCom" "creatorCre" "creatorDkbea" "creatorDkbra"
    "creatorDkbrm" "creatorDkdes" "creatorDkmed" "creatorDkmon" "creatorDkops"
    "creatorDkved" "creatorDrm" "creatorDrt" "creatorEdt" "creator" "dcCreator"
    "creatorIll" "creatorIve" "creatorIvr" "creatorLyr" "creatorMus" "creatorOth"
    "creatorPht" "creatorPROVIDERID" "creatorScl" "creatorSng" 
    "creatorTrl" "creatorWdc"]
   :id
   ["fedoraPid" "pid" "acIdentifier"
    "identifier" "identifierISBN" "identifierISMN" "identifierISRC" "identifierISSN"
    "identifierPROVIDERID" "identifierPROVIDERMID" "identifierUPC" "identifierURI"]
   :relation
   ["continuedIn" "continues" "discussedIn" "discusses"
    "hasAdaptation" "hasAnalysis" "hasCreatorDescription" "hasDescriptionFromPublisher"
    "hasManuscript" "hasOnlineAccess" "hasPart" "hasPartTrack" "hasReview" "hasSoundtrack"
    "isAdaptationOf" "isAnalysisOf" "isDescriptionFromPublisherOf" "isManuscriptOf"
    "isPartOfAlbumId" "isPartOfAlbumTitle" "isPartOf" "isPartOfISBN" "isPartOfISSN"
    "isPartOfManifestation" "isReplacedBy" "isReviewOf" "isSoundtrackOfGame"
    "isSoundtrackOfMovie"
    "partOf" "replaces"]
   :subject
   ["spatialCoordinates" "spatialDBCF" "spatialDBCM" "spatialDBCS" "spatial"
    "temporalDBCM" "temporalDBCP" "temporal"
    "subjectDBCF" "subjectDBCM" "subjectDBCN" "subjectDBCO" "subjectDBCS" "subjectDK5"
    "subjectDK5Text" "subjectGenre" "subject" "subjectLCSH" "subjectSort"]
   :title
   ["titleFull" "dcTitleFull" "title" "dcTitle" "titleSeries"]})
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
  (let
   [work (get-work work-id)
    schema-type (get types (.toLowerCase (first (log (get work "type" [""])))) "missing-type")
    language (:language work)
    subjects (get-properties work :subject)
    location (:location work)
    creators (get-properties work :creator)
    contributers (get-properties work :contributor)
    ]
    [:div.ui.container
     {:vocab "http://schema.org/"
      :prefix "bib: http://bib.schema.org/ dc: http://purl.org/dc/elements/1.1/
dkdcplus: http://biblstandard.dk/abm/namespace/dkdcplus/
"
      :resource (str "http://rdf.solsort.com/ting/" work-id)
      :typeof schema-type}
     [:p]
     [:h1.center {:property "name"} (:title work)]
     (r-if (not (empty? creators)) 
       (into [:p.center "af "] (people-list creators "creator")))
     [:p.center ; cover
      (if (string/starts-with? (:cover-url work) "assets/")
        ""
        [:img
         {:src (get-cover work)
          :property "image"
          :style
          {:max-height (* 0.5 (- js/document.body.clientHeight 50))
           :max-width (* 0.8 (- js/document.body.clientWidth 20))}}])]
     [:p.center [:a.ui.primary.button (ahref {:page "order" :pid work-id}) "Reservér"]]
     [:a {:property "mainEntityOfPage" :href (str "https://bibliotek.dk/da/work/" (first (get work "pid")))}]
     [:p (:description work)]
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
     (if language [:p [:em "Sprog: "] language] "")
     (if location [:p [:em "Opstilling: "] location] "")
     (if (< 1 (count (get work "collection")))
       [:div
        [:p.bold "Udgaver:"]
        (into [:div]
              (map work-tiny (get work "collection")))]
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
                           (map (fn [s] [:span (str s)]) v)))]))]]))
