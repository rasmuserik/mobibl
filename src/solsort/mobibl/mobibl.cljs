(ns solsort.mobibl.mobibl
  (:require-macros  [cljs.core.async.macros :refer  [go go-loop alt!]])
  (:require
   [solsort.util 
    :refer  
    [<ajax <seq<! js-seq normalize-css load-style! put!close! parse-json-or-nil log page-ready render
     dom->clj]]
   [reagent.core :as reagent :refer  []]
   [clojure.string :as string :refer [replace split blank?]]
   [cljs.core.async :refer  [>! <! chan put! take! timeout close! pipe]]))

(render [:h1 "hello"])

