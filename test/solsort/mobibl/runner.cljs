(ns solsort.mobibl.runner
    (:require [cljs.test :as test]
              [doo.runner :refer-macros [doo-all-tests doo-tests]]
              [solsort.mobibl.core-test]))

(doo-tests 'solsort.mobibl.test-test)
