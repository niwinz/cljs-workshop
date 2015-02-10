(ns cljsworkshop.core
  (:require [qbits.jet.server :refer [run-jetty]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
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
  (let [app (wrap-defaults app site-defaults)]
    (run-jetty {:ring-handler app :port 5050})))

