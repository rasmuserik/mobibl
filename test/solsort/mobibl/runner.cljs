(ns solsort.mobibl.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [solsort.mobibl.test]))

(doo-tests 'solsort.mobibl.test)
