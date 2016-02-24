(ns solsort.mobibl.mock-data)
;; # DB
;;
;; As we are starting out implementing the views, we just have dummy data here
;; so far.  The precise format and content of this data has not yet been
;; determined.
;;
(def sample-db
  {
   ; :route ["work" "870970-basis:28934297"]
   :route ["patron" "870970-basis:28934297"]
   :patron 
   {:reservations
    [{:id "775100-katalog:28934572" :title "A momentary lapse of reason"}]
    :reservations-arrived
    [{:id "775100-katalog:50643662" :title "Matematik i virkeligheden"}
     {:id "775100-katalog:10260744" :title "Ummagumma"} ]
    :borrowed
    [{:id "870970-basis:28934297" :title "1Q84"}]}
   :current
   {:search-query "Murakami"
    :work "870970-basis:28934297"
    :library "Københavns Hovedbibliotek"}
   :works
   {"870970-basis:28934297"
    {:title "1Q84"
     :creator "Haruki Murakami"
     :cover-url "http://www.bogpriser.dk/Covers/202/9788779559202.jpg"
     :description "Aomame er en 30-årig smart pige, uddannet kampsportsinstruktør, men arbejder p.t. som lejemorder. Tengo er matematiklærer med forfatterdrømme. Han skal omskrive en sær 17-årig piges sære historie. Begge hovedfigurer oplever, at deres virkelighed forvrides let, hvad påvirker deres virkelighed?"
     :keywords ["Kultur" "Kærlighed" "Magisk Realisme" "Magt" "Parallelle Verdener" "Skrivekunst" "Japan" "1980-1989"]
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
     :cover-url "https://en.wikipedia.org/wiki/File:PinkFloyd-album-ummagummastudio-300.jpg"
     :description "Musik"
     :keywords ["Rock"]
     :location "Musik"
     :languages ["English"]
     :editions
     [{:name "Lydbog (cd) (bind 1)" :availability :loaned}
      {:name "Lydbog (cd) (bind 2)" :availability :available}
      {:name "Lydbog (cd) (bind 3)" :availability :available}]}}})
