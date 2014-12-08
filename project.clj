(defproject cljsworkshop "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "BSD (2-Clause)"
            :url "http://opensource.org/licenses/BSD-2-Clause"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]

                 ;; Backend dependencies
                 [compojure "1.2.1"]
                 [ring/ring-core "1.3.1" :exclusions [javax.servlet/servlet-api]]
                 [ring/ring-servlet "1.3.1" :exclusions [javax.servlet/servlet-api]]
                 [ring/ring-defaults "0.1.2"]

                 [javax.servlet/javax.servlet-api "3.1.0"]
                 [info.sunng/ring-jetty9-adapter "0.7.2"]
                 [com.cemerick/piggieback "0.1.3"]
                 [weasel "0.4.2"]

                 ;; Frontend dependencies
                 [org.clojure/clojurescript "0.0-2411"]
                 [secretary "1.2.1"]
                 [sablono "0.2.22"]
                 [om "0.8.0-alpha2"]
                 [hodgepodge "0.1.0"]]

  :source-paths ["src/clj"]
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :main cljsworkshop.core
  :plugins [[lein-cljsbuild "1.0.3"]]
  :cljsbuild {:builds
              [{:id "app"
                :source-paths ["src/cljs"]
                :compiler {:optimizations :whitespace
                           :pretty-print true
                           :preamble ["react/react.min.js"]
                           :output-to "resources/public/js/app.js"
                           ;; :output-dir "resources/public/js/"
                           ;; :source-map "resources/public/js/app.js.map"
                           }}]})
