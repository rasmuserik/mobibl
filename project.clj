(defproject solsort.mobibl/mobibl "0.0.1-SNAPSHOT"
  :license "Creative Commons BY-NC-ND"

  :dependencies
  [[org.clojure/clojure "1.8.0"]
   [org.clojure/clojurescript "1.7.170"]
   [org.clojure/core.async "0.2.374"]
   [cljsjs/pouchdb "5.2.0-1"]
   [cljsjs/showdown "0.4.0-1"]
   [solsort/util "0.1.2-SNAPSHOT"]
   [reagent "0.5.1"]
   [re-frame "0.6.0"]]

  :plugins
  [[lein-cljsbuild "1.1.1"]
   [lein-ancient "0.6.8"]
   [lein-figwheel "0.5.0-2"]
   [lein-kibit "0.1.2"]
   [lein-doo "0.1.6"]]

  :source-paths ["src/"]

  :clean-targets ^{:protect false} 
  ["resources/public/out"
   "resources/public/index.js"
   "figwheel_server.log"
   "target/"]

  :profiles
  {:dev  {:dependencies  [[com.cemerick/piggieback "0.2.1"]
                          [org.clojure/tools.nrepl "0.2.10"]]
          :repl-options  {:nrepl-middleware
                          [cemerick.piggieback/wrap-cljs-repl]}}}

  :cljsbuild 
  {:builds 
   [{:id "dev"
     :source-paths ["src/"]
     :figwheel {:websocket-host ~(.getHostAddress (java.net.InetAddress/getLocalHost))
               ; :on-jsload "" 
                }
     :compiler {:main solsort.mobibl.mobibl
                :asset-path "out"
                :output-to "resources/public/index.js"
                :output-dir "resources/public/out"
                :source-map-timestamp true }}
    {:id "test"
     :source-paths ["src" "test"]
     :compiler {:output-to "resources/public/tests.js"
                :main solsort.mobibl.runner
                :optimizations :none}}
    {:id "dist"
     :source-paths ["src"]
     :compiler {:output-to "index.js"
                :main solsort.mobibl.mobibl
                :externs ["externs.js"]
                :optimizations :advanced
                :pretty-print false}}]}
  :figwheel {:nrepl-port 7888})
