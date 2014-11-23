(ns cljsworkshop.controllers
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [goog.dom :as gdom]
            [cljs.core.async :refer [<! put! chan]]
            [cljsworkshop.utils :refer [set-html! xhr debounce throttle] :as utils]
            [om.core :as om :include-macros true]
            ;; [om.dom :as dom :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]))

(enable-console-print!)

;; State of the application
(def state (atom {:search ""}))

(defn- search
  [source searchstring]
  (let [source       (str (.toLowerCase (get source "name"))
                          (.toLowerCase (get source "snippet")))
        searchstring (.toLowerCase searchstring)
        r            (.indexOf source searchstring)]
    (if (= r -1) false true)))

;; Main component
(defn phones-search
  [app owner]
  (reify
    om/IDisplayName
    (display-name [this] "phone-search")

    om/IInitState
    (init-state [_]
      {:events (chan)})

    om/IWillMount
    (will-mount [_]
      (go
        (let [events  (-> (om/get-state owner :events)
                          (throttle 1000))
              results (<! (utils/xhr "/static/phones/phones.json"))
              results (js->clj results)]

          ;; Set initial state
          (om/transact! app #(assoc % :results results))

          ;; Watch input events
          (loop []
            (let [val (<! events)]
              (if (empty? val)
                (om/transact! app #(assoc % :results results))
                (let [filtered (filter #(search % val) results)]
                  (om/transact! app #(assoc % :results filtered)))))
            (recur)))))

    om/IRenderState
    (render-state [_ {:keys [events]}]
      (html
       [:section
        [:h1 "Phone Searcher"]
        [:form
         [:section {:class "title"}
          [:input {:placeholder "Type your search..."
                   :on-key-up (fn [e]
                                (let [value (.-value (.-currentTarget e))]
                                  (put! events value)
                                  (om/transact! app #(assoc %1 :search value))))}]]
         [:section {:class "content"}
          [:section {:class"subtitle"}
           [:span "Search results for: "]
           [:strong (:search app)]]
          [:section {:class "results"}
           (if (empty? (:results app))
             [:span "No results"]
             [:ul (for [item (:results app)]
                    (let [id (get item "id")
                          name (get item "name")]
                      [:li {:id id} name]))])]]]]))))

(defn home-controller
  []
  (let [app (gdom/getElement "app")]
    (om/root phones-search state {:target app})))

(defn not-found-controller
  []
  (let [app (gdom/getElement "app")]
    (set-html! app "<h1>Not found<h1>")))
