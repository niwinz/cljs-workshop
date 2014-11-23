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
(defonce state
  (atom {:search ""
         :results []
         :detailcache {}
         :details {}
         }))

(defn- match-string
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
              results (<! (xhr "/static/phones/phones.json"))
              results (js->clj results)]

          ;; Set initial state
          (om/transact! app #(assoc % :results results))

          ;; Watch input events
          (loop []
            (let [val (<! events)]
              (if (empty? val)
                (om/transact! app #(assoc % :results results))
                (let [filtered (filter #(match-string % val) results)]
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
                      [:li {:id id}
                       [:a {:href (str "/#/" id)} name]]))])]]]]))))


(defn phone-detail
  [app owner]
  (reify
    om/IDisplayName
    (display-name [_]
      "phone-detail")

    om/IInitState
    (init-state [_]
      {:data nil})

    om/IWillMount
    (will-mount [_]
      (go
        (let [phoneid (om/get-state owner :phoneid)
              data    (get-in @app [:detailcache phoneid] nil)]
          (if (nil? data)
            (let [data (<! (xhr (str "/static/phones/" (name phoneid) ".json")))
                  data (js->clj data)]
              ;; (.log js/console "data loaded" (clj->js data))
              (om/transact! app #(assoc-in % [:detailcache phoneid] data))
              (om/set-state! owner :data data))
            (om/set-state! owner :data data)))))

    om/IRenderState
    (render-state [_ {:keys [phoneid data] :as state}]
      (.log js/console "render state" (clj->js state))
      (if (nil? data)
        (html [:div "loading..."])
        (let [imgsrc (str "/static/" (first (get data "images")))]
          (html
           [:section {:class "container"}
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
                                  ;; :instrument
                                  ;; (fn [f cursor m]
                                  ;;   (.log js/console "instrument" (clj->js cursor))
                                  ;;   (.log js/console "instrument" (clj->js m))
                                  ;;   ::om/pass)})))

(defn phone-controller
  [phoneid]
  (let [app (gdom/getElement "app")
        phoneid (keyword phoneid)]
    (om/root phone-detail state {:target app :init-state {:phoneid phoneid}})))

(defn not-found-controller
  []
  (let [app (gdom/getElement "app")]
    (set-html! app "<h1>Not found<h1>")))
