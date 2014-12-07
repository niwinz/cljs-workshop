(ns cljsworkshop.core
  (:require-macros [secretary.core :refer [defroute]])
  (:require [goog.events :as events]
            [goog.dom :as gdom]
            [secretary.core :as secretary]
            [cljsworkshop.controllers :as ctrl])
  (:import goog.History))

(defroute home-path "/" [] (ctrl/home-controller))
(defroute phone-detail-path #"/([\w\-]+)" [id] (ctrl/phone-controller id))
(defroute "*" [] (ctrl/not-found-controller))

(defn main
  []
  ;; Set secretary config for use the hashbang prefix
  (secretary/set-config! :prefix "#")

  ;; Attach event listener to history instance.
  (let [history (History.)]
    (events/listen history "navigate"
                   (fn [event]
                     (let [el (gdom/getElement "app")]
                       (.log js/console ":navigate:" el (.-children el)))
                     (secretary/dispatch! (.-token event))))
    (.setEnabled history true)))

(main)
