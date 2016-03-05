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
    ;:library "710100"
    :library "810010"
    }

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

   ;; ## Known libraries
   ;;
   ;; Simple representation of the libraries that are interesting for the user
   ;; including position on a map, opening hours and contact information.
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
       :city "1172 København K"
       :country "Danmark"}
      :email "bibliotek@kff.kk.dk"
      :phone {:number "33663000"
              :time "man-fre 10-17"}
      :position
      [55.680887 12.573619]
      :hours
      [{:title "Åbningstider"
        :weekdays 
        [[8 22]
         [8 20]
         [8 20]
         [8 20]
         [8 19]
         [8 17]]}
       {:title "Betjening"
        :weekdays 
        [[12 17]
         [12 17]
         [12 17]
         [12 17]
         [12 17]
         [12 15]]}]}
    "810010"
    {:name "Det Kongelige Bibliotek, Diamanten"
     :type "Forskningsbibliotek"
     :address {
               :road "Søren Kierkegaards Plads 1"
               :city "1221 København K"
               :country "Danmark"}
     :email "kb@kb.dk"
     :phone {:number "33 47 47 47"
             :time "man - fre 9-16"}
     :position
     [55.67321579999999 12.5821264]
     ;;
     ;; FIXME How to represent many opening hours for departments of a library
     ;;
     ;; At the Diamanten library there are a lot of possible access times, so
     ;; many are included here to see if it is possible to represent them in a
     ;; manageable way on a mobile app.
     ;;
     ;; The user could also be presented for a way to selecting which department
     ;; of the library to view opening hours for.
     ;;
     :hours
     [{:title "Adgang til Diamanten"
       :weekdays
       [[8 22]
        [8 22]
        [8 22]
        [8 22]
        [8 22]
        [8 22]]}
      {:title "Informationen"
       :weekdays
       [[8 21]
        [8 21]
        [8 21]
        [8 21]
        [8 21]]}
      {:title "Helpdesk"
       :weekdays
       [[10 16]
        [10 16]
        [10 16]
        [10 16]
        [10 16]]}
      {:title "Læsesal Vest og E-Vest"
       :weekdays
       [[9 21]
        [9 21]
        [9 21]
        [9 21]
        [9 21]
        [10 17]]}
      {:title "Læsesal Nord og Øst"
       :weekdays
       [[8 21]
        [8 21]
        [8 21]
        [8 21]
        [8 21]
        [10 17]]}
      {:title "Center for Kort og Billeder"
       :weekdays
       [nil
        [10 16]
        [12 16]
        [10 16]]}
      {:title "Center for Manuskripter og Boghistorie"
       :weekdays
       [[10 17]
        [10 17]
        [12 19]
        [10 17]
        [10 17]]}
      {:title "Småtrykssamlingens interne læsesal"
       :weekdays
       [[10 15]
        [10 15]
        [10 15]
        [10 15]
        [10 15]]}
      {:title "Dansk Folkemindesamling"
       :weekdays
       [nil
        [10 15]
        [10 15]
        [10 15]
        [10 15]]}]}}})
