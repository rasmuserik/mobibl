(ns solsort.mobibl.test
    (:require [cljs.test :as test :refer-macros [deftest is]]
              [solsort.mobibl.mobibl :as mobibl]))

;; Report test status
;; https://github.com/clojure/clojurescript/wiki/Testing#detecting-test-completion--success
(defmethod test/report  [:cljs.test/default :end-run-tests]  [m]
  (if  (cljs.test/successful? m)
    (println "Success!")
    (println "FAIL")))

(deftest test-test
  (is (= 2 2)))

;; (deftest async-test
  ;; (async done
         ;; (let [a 1]
           ;; (js/setTimeout (fn []
                              ;; (is (= 1 a))
                              ;; (done))
                          ;; 100)
           ;; (is (= 1 a)))))
