(defproject cljsworkshop "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "BSD (2-Clause)"
            :url "http://opensource.org/licenses/BSD-2-Clause"}
  :dependencies [[org.clojure/clojure "1.6.0"]

                 ;; Backend dependencies
                 [compojure "1.2.1"]
                 [ring/ring-core "1.3.1" :exclusions [javax.servlet/servlet-api]]
                 [ring/ring-servlet "1.3.1" :exclusions [javax.servlet/servlet-api]]
                 [ring/ring-defaults "0.1.2"]

                 [javax.servlet/javax.servlet-api "3.1.0"]
                 [info.sunng/ring-jetty9-adapter "0.7.2"]

                 ;; Frontend dependencies
                 [org.clojure/clojurescript "0.0-2371"]]

  :source-paths ["src/clj"]

  :main cljsworkshop.core
  :plugins [[lein-cljsbuild "1.0.3"]]
  :cljsbuild {:builds
              [{:id "app"
                :source-paths ["src/cljs"]
                :compiler {:output-to "resources/public/js/app.js"
                           :optimizations :whitespace
                           :pretty-print true}}]})
