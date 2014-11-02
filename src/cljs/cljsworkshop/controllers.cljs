(ns cljsworkshop.controllers
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as dom]
            [cljs.core.async :refer [<! put! chan]]
            [cljsworkshop.utils :refer [set-html! xhr debounce throttle]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]])
  (:import goog.Uri
           goog.net.XhrIo
           goog.net.Jsonp))

;; State of the application
(def state
  (atom {:search ""}))

(defn github-search-api
  [term]
  (let [uri "https://api.github.com/search/repositories?sort=stars&order=desc&q="]
    (str uri term)))

(defn github-results
  [app, owner]
  (reify
    om/IDisplayName
    (display-name [this] "github-results")

    ;; om/IInitState
    ;; (init-state [_]
    ;;   {:results []})

    om/IWillMount
    (will-mount [_]
      (let [query (-> (om/get-state owner :query)
                      (throttle 1000))]
        (go
          (loop []
            (let [term    (<! query)
                  results (<! (xhr (github-search-api term)))
                  res     (-> results js->clj :data :items)]
              (.log js/console results)
              (om/set-state! owner :results res))
            (recur)))))

    om/IRenderState
    (render-state [_ {:keys [results] :or {results []}}]
      (html [:section.content
             [:section
              [:div "Search result for: "
               [:strong (:search app)]]]
             [:section
              (if (empty? [])
                [:span "No results"]
                [:ul
                 (for [item (:results app)]
                   [:li {:id (:id item)} (:name item)])])]]))))

;; Main component
(defn github-search
  [app owner]

  (reify
    om/IDisplayName
    (display-name [this] "github-results")

    om/IInitState
    (init-state [_]
      {:query (chan)})

    om/IRenderState
    (render-state [_ {:keys [query]}]
      (html [:section
             [:h1 "Github Searcher"]
             [:form
              [:section.title
               [:input {:placeholder "Type your search"
                        :on-change (fn [e]
                                     (let [value (.-value (.-currentTarget e))]
                                       (put! query value)))}]]
              (om/build github-results app {:init-state {:query query}})]]))))


(defn home-controller
  []
  (let [app (dom/getElement "app")]
    (om/root github-search state {:target app})))

(defn not-found-controller
  []
  (let [app (dom/getElement "app")]
    (set-html! app "<h1>Not found<h1>")))
