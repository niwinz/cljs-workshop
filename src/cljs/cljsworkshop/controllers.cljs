(ns cljsworkshop.controllers
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [clojure.string :as str]
            [goog.dom :as gdom]
            [cljs.core.async :refer [<! put! chan]]
            [cljsworkshop.utils :refer [set-html! xhr debounce throttle] :as utils]
            [om.core :as om :include-macros true]
            ;; [om.dom :as dom :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]))

(enable-console-print!)

;; State of the application
(defonce state
  (atom {:search ""}))

(defn- match-string
  [source searchstring]
  (let [source       (str (str/lower-case (get source "name"))
                          (str/lower-case (get source "snippet")))
        searchstring (str/lower-case searchstring)
        r            (.indexOf source searchstring)]
    (if (= r -1) false true)))

(defn- get-phones-data
  []
  (go
    (let [results (<! (xhr "/static/phones/phones.json"))]
      (js->clj results))))

(defn get-phone-detail
  [phoneid]
  (go
    (let [result (<! (xhr (str "/static/phones/" (name phoneid) ".json")))]
      (js->clj result))))

(defn phones-search
  "Home page (main) component."
  [app owner]
  (reify
    om/IDisplayName
    (display-name [_]
      "phone-search")

    om/IInitState
    (init-state [_]
      {:events (chan)
       :results []})

    om/IWillMount
    (will-mount [_]
      (go
        (let [events (-> (om/get-state owner :events) (throttle 1000))
              results (<! (get-phones-data))]

          ;; Set initial state
          (if (empty? (:search @app))
            (om/update-state! owner #(assoc % :results results))
            (let [filtered (filter #(match-string % val) results)]
              (om/update-state! owner #(assoc % :results filtered))))

          ;; Watch input events loop
          (loop []
            (let [val (<! events)]
              (if (empty? val)
                (om/update-state! owner #(assoc % :results results))
                (let [filtered (filter #(match-string % val) results)]
                  (om/update-state! owner #(assoc % :results filtered)))))
            (recur)))))

    om/IRenderState
    (render-state [_ {:keys [events results]}]
      (html
       [:section {:id "home"}
        [:h1 "Phone Searcher"]
        [:form
         [:section {:class "title"}
          [:input {:placeholder "Type your search..."
                   :defaultValue (:search app)
                   :on-key-up (fn [e]
                                (let [value (.-value (.-currentTarget e))]
                                  (put! events value)
                                  (om/transact! app #(assoc %1 :search value))))}]]
         [:section {:class "content"}
          [:section {:class"subtitle"}
           [:span "Search results for: "]
           [:strong (:search app)]]
          [:section {:class "results"}
           (if (empty? results)
             [:span "No results"]
             [:ul (for [item results]
                    (let [id (get item "id")
                          name (get item "name")]
                      [:li {:id id}
                       [:a {:href (str "/#/" id)} name]]))])]]]]))))


(defn phone-detail
  "Phone detail component."
  [app owner]
  (reify
    om/IDisplayName
    (display-name [_]
      "phone-detail")

    om/IInitState
    (init-state [_]
      {:data nil
       :phoneid nil})

    om/IWillMount
    (will-mount [_]
      (go
        (let [phoneid (om/get-state owner :phoneid)
              data    (<! (get-phone-detail phoneid))]
          (om/set-state! owner :data data))))

    om/IRenderState
    (render-state [_ {:keys [phoneid data] :as state}]
      (html
       (if (nil? data)
         [:div "loading..."]
         (let [imgsrc (str "/static/" (first (get data "images")))]
           [:section {:id "detail" :class "container"}
            [:h1 (get data "name")]
            [:section {:class "photos"}
             [:section {:class "main-photo"}
              [:img {:src imgsrc}]]
             [:section {:class "other-photos"}
              (for [img (rest (get data "images"))]
                (let [imgsrc (str "/static/" img)]
                  [:img {:src imgsrc :width "100"}]))]]]))))))


(defn home-controller
  []
  (let [app (gdom/getElement "app")]
    (om/root phones-search state {:target app})))

(defn phone-controller
  [phoneid]
  (let [app (gdom/getElement "app")
        phoneid (keyword phoneid)]
    (om/root phone-detail state {:target app :init-state {:phoneid phoneid}})))

(defn not-found-controller
  []
  (let [app (gdom/getElement "app")]
    (set-html! app "<h1>Not found<h1>")))
