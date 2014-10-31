(ns cljsworkshop.core
  (:require [ring.adapter.jetty9 :refer [run-jetty]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.response :refer [render]]
            [clojure.java.io :as io]))

(defn home
  [req]
  (render (io/resource "index.html") req))

(defroutes app
  (GET "/" [] home)
  (route/resources "/static")
  (route/not-found "<h1>Page not found</h1>"))

(defn -main
  [& args]
  (-> app
      (wrap-defaults api-defaults)
      (run-jetty {:port 5050})))



