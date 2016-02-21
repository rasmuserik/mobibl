(ns solsort.mobibl.test
    (:require [cljs.test :refer-macros [deftest is]]
              [solsort.mobibl.mobibl :as mobibl]))

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
