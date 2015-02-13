(ns cljsworkshop.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljsjs.react :as react]
            [clojure.string :as str]
            [goog.dom :as gdom]
            [goog.events :as events]
            [cljs.core.async :refer [<! put! chan]]
            [om.core :as om]
            [om.dom :as dom]))

(enable-console-print!)

(defonce state {:message "Hello world from global state."})

(defn mycomponent
  [app owner]
  (reify
    om/IDisplayName
    (display-name [_]
      "my-component")

    om/IInitState
    (init-state [_]
      {:message "Hello world from local state"})

    om/IRenderState
    (render-state [_ {:keys [message]}]
      (dom/section
        (dom/div nil message)
        (dom/div nil (:message app))))))

(let [el (gdom/getElement "app")]
  (om/root mycomponent state {:target el}))
