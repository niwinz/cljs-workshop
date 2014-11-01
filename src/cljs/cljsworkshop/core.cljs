(ns cljsworkshop.core
  (:require [goog.events :as events]
            [goog.dom :as dom]))

(defn main
  []
  (let [counter  (atom 0)
        button  (dom/getElement "button")
        display (dom/getElement "clicksnumber")]

    ;; Set initial value
    (set! (.-innerHTML display) @counter)

    ;; Assign event listener
    (events/listen button "click"
                   (fn [event]
                     ;; Increment the value
                     (swap! counter inc)
                     ;; Set new value in display element
                     (set! (.-innerHTML display) @counter)))))

(main)
