(ns cljsworkshop.core
  (:require-macros [secretary.core :refer [defroute]])
  (:require [goog.events :as events]
            [goog.dom :as dom]
            [secretary.core :as secretary])
  (:import goog.History))


(def app (dom/getElement "app"))

(defn set-html! [el content]
  (set! (.-innerHTML el) content))

(defroute home-path "/" []
  (set-html! app "<h1>Hello World from home page.</h1>"))

(defroute some-path "/:param" [param]
  (let [message (str "<h1>Parameter in url: <small>" param "</small>!</h1>")]
    (set-html! app message)))

(defroute "*" []
  (set-html! app "<h1>Not Found</h1>"))

(defn main
  []
  ;; Set secretary config for use the hashbang prefix
  (secretary/set-config! :prefix "#")

  ;; Attach event listener to history instance.
  (let [history (History.)]
    (events/listen history "navigate"
                   (fn [event]
                     (secretary/dispatch! (.-token event))))
    (.setEnabled history true)))

(main)


