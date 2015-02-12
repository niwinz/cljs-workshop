(defproject cljsworkshop "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "BSD (2-Clause)"
            :url "http://opensource.org/licenses/BSD-2-Clause"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]

                 ;; Backend dependencies
                 [com.cemerick/piggieback "0.1.5"]
                 [weasel "0.6.0-SNAPSHOT"]
                 [compojure "1.3.1"]

                 [hodgepodge "0.1.3"]
                 [sablono "0.3.1"]
                 [org.omcljs/om "0.8.8"]
                 [prismatic/om-tools "0.3.6"]
                 [secretary "1.2.1"]
                 [org.clojure/clojurescript "0.0-2816"]

                 [ring/ring-core "1.3.2" :exclusions [javax.servlet/servlet-api]]
                 [ring/ring-servlet "1.3.2" :exclusions [javax.servlet/servlet-api]]
                 [ring/ring-defaults "0.1.2" :exclusions [javax.servlet/servlet-api]]

                 [cc.qbits/jet "0.5.4"]]

  :source-paths ["src/clj", "src/cljs"]
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :main cljsworkshop.core
  :plugins [[lein-cljsbuild "1.0.4"]]
  :cljsbuild {:builds
              [{:id "devel"
                :source-paths ["src/cljs"]
                :compiler {:output-to "resources/public/js/app.js"
                           :output-dir "resources/public/js/out-devel"
                           :source-map true
                           :foreign-libs [{:file "js/moment.js"
                                           :file-min "js/moment.min.js"
                                           :provides ["cljsworkshop.moment"]}]
                           :externs ["js/moment.min.js"]
                           :optimizations :none
                           :cache-analysis true
                           :asset-path "/static/js/out"
                           :main cljsworkshop.core
                           :pretty-print true}}
               {:id "prod"
                :source-paths ["src/cljs"]
                :compiler {:output-to "resources/public/js/app.js"
                           :output-dir "resources/public/js/out-prod"
                           :source-map "resources/public/js/app.js.map"
                           :foreign-libs [{:file "js/moment.js"
                                           :file-min "js/moment.min.js"
                                           :provides ["cljsworkshop.moment"]}]
                           :externs ["js/moment.min.js"]
                           :closure-warnings {:externs-validation :off}
                           :optimizations :advanced
                           :cache-analysis true
                           :asset-path "/static/js/out"
                           :main cljsworkshop.core
                           :pretty-print false}}]})

