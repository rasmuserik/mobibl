;; # Sample Application Database (`mock_data.cljs`)
(ns solsort.mobibl.mock-data)
;;
;; As we are starting out implementing the views, we just have dummy data here
;; so far.  The precise format and content of this data has not yet been
;; determined.
;;
(def sample-db
  {
   ;
   ; :route ["work" "870970-basis:28934297"]
   ;
   :route ["status"]

   ;; ## User loan status
   ;;
   ;; Info under `:status` refers with ids to complete descriptions in the
   ;; `:works` section
   ;;
   :status
   {:reservations
    [{:id "775100-katalog:28934572" :until "2016-03-12"}]
    :arrived
    [{:id "775100-katalog:50643662" :until "2016-03-01"}
     {:id "775100-katalog:10260744" :until "2016-03-02"}]
    :borrowed
    [{:id "870970-basis:28934297" :until "2016-03-24"}
     {:id "123456-basis:12345678" :until "1901-12-12"}]}
   :current
   {:search-query "Murakami"
    :work "870970-basis:28934297"
    :library "710100"}

   ;; ## Creative works
   ;;
   ;; This contains the metadata of the creative works,
   ;; caching the webservices.
   ;;
   :works
   {"870970-basis:28934297"
    {:title "1Q84"
     :creator "Haruki Murakami"
     :cover-url "http://www.bogpriser.dk/Covers/202/9788779559202.jpg"
     :description
     "Aomame er en 30-årig smart pige, uddannet kampsportsinstruktør, men
      arbejder p.t. som lejemorder. Tengo er matematiklærer med
      forfatterdrømme.  Han skal omskrive en sær 17-årig piges sære historie.
      Begge hovedfigurer oplever, at deres virkelighed forvrides let, hvad
      påvirker deres virkelighed?"
     :keywords ["Kultur" "Kærlighed" "Magisk Realisme" "Magt"
                "Parallelle Verdener" "Skrivekunst" "Japan" "1980-1989"]
     :location "Skønlitteratur"
     :language "Dansk"
     :editions
     [{:name "Bog (bind 1)" :availability :available}
      {:name "Bog (bind 2)" :availability :loaned}
      {:name "Bog (bind 3)" :availability :available}
      {:name "Lydbog (cd) (bind 1)" :availability :available}
      {:name "Lydbog (cd) (bind 2)" :availability :available}
      {:name "Lydbog (cd) (bind 3)" :availability :available}
      {:name "Lydbog (online) (bind 1)" :availability :available}
      {:name "Lydbog (online) (bind 2)" :availability :available}
      {:name "Lydbog (online) (bind 3)" :availability :available}]}
    "775100-katalog:50643662"
    {:title "Matematik i virkeligheden"
     :creator "Allan Baktoft"
     :cover-url "http://www.bogpriser.dk/Images/placeholder-cover.png"
     :description "Regn den ud"
     :keywords ["Matematik" "Regne"]
     :location "Faglitteratur"
     :languages ["Dansk"]
     :editions
     [{:name "Bog (bind 3)" :availability :available}
      {:name "Lydbog (cd) (bind 1)" :availability :available}
      {:name "Lydbog (cd) (bind 2)" :availability :available}
      {:name "Lydbog (cd) (bind 3)" :availability :available}
      {:name "Lydbog (online) (bind 1)" :availability :available}]}
    "775100-katalog:28934572"
    {:title "A momentary lapse of reason"
     :creator "The Pink Floyd"
     :cover-url "https://en.wikipedia.org/wiki/File:MLoRLP01.jpg"
     :description "Musik"
     :keywords ["Rock"]
     :location "Musik"
     :languages ["Engelsk"]
     :editions
     [{:name "Lydbog (cd) (bind 1)" :availability :available}
      {:name "Lydbog (cd) (bind 2)" :availability :loaned}
      {:name "Lydbog (cd) (bind 3)" :availability :loaned}]}
    "775100-katalog:10260744"
    {:title "Ummagumma"
     :creator "The Pink Floyd"
     :cover-url (str "https://en.wikipedia.org/wiki/"
                     "File:PinkFloyd-album-ummagummastudio-300.jpg")
     :description "Musik"
     :keywords ["Rock"]
     :location "Musik"
     :languages ["English"]
     :editions
     [{:name "Lydbog (cd) (bind 1)" :availability :loaned}
      {:name "Lydbog (cd) (bind 2)" :availability :available}
      {:name "Lydbog (cd) (bind 3)" :availability :available}]}}

   ;; Simplified representation of libraries identified by library id.
   ;;
   ;; The `:hours` section may need to be expanded as some libraries have
   ;; different sections with differing opening hours. The "Sorte Diamant" for
   ;; example, has 9 different sections with differing hours, see
   ;;
   ;;    http://www.kb.dk/da/kb/aabningstider/index.html#diamant
   ;;
   ;; FIXME The above books in the works section are from different libraries,
   ;; ie. 775100 is Aarhus hovedbibliotek.
   ;;
    :library
    {"710100"
    {:name "Københavns Hovedbibliotek"
      :type "Folkebibliotek"
      :address
      {:road "Krystalgade 15"
      :post "1172"
      :city "København K"
      :country "Danmark"}
      :email "bibliotek@kff.kk.dk"
      :phone {:number "33663000"
              :time "man-fre 10-17"}
      :position
      [55.680887 12.573619]
      :hours
      {:open
      [[8 22]
        [8 20]
        [8 20]
        [8 20]
        [8 19]
        [8 17]]
      :service
      [[12 17]
        [12 17]
        [12 17]
        [12 17]
        [12 17]
        [12 15]]}}
    "810010"
    {:name "Det Kongelige Bibliotek, Diamanten"
      :type "Forskningsbibliotek"
      :address {
                :road "Søren Kierkegaards Plads 1"
                :post "1221"
                :city "København K"
                :country "Danmark"}
      :email "kb@kb.dk"
      :phone {:number "33 47 47 47"
              :time "man - fre 9-16"}
      :position
      [55.67321579999999 12.5821264]
      :hours
      {:open
      [[8 22]
        [8 22]
        [8 22]
        [8 22]
        [8 22]
        [8 22]]
      :service
      [[10 16]
        [10 16]
        [10 16]
        [10 16]
        [10 16]]}}}})
