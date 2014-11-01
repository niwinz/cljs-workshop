(ns cljsworkshop.core
  (:require [ring.adapter.jetty9 :refer [run-jetty]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.response :refer [render]]
            [clojure.java.io :as io]))

;; Simple function that works as controller
;; It should return a proper response. In our
;; case it returns a content of static index.html.
(defn home
  [req]
  (render (io/resource "index.html") req))

;; Routes definition
(defroutes app
  (GET "/" [] home)
  (route/resources "/static")
  (route/not-found "<h1>Page not found</h1>"))

;; Application entry point
(defn -main
  [& args]
  (run-jetty app {:port 5050}))



