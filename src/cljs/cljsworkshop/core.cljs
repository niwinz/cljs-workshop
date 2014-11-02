(ns cljsworkshop.core
  (:require-macros [secretary.core :refer [defroute]])
  (:require [goog.events :as events]
            [secretary.core :as secretary]
            [cljsworkshop.controllers :as ctrl])
  (:import goog.History))

(defroute home-path "/" [] (ctrl/home-controller))
(defroute "*" [] (ctrl/not-found-controller))

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
