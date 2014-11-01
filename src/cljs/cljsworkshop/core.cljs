(ns cljsworkshop.core
  (:require-macros [secretary.core :refer [defroute]]
                   [cljs.core.async.macros :refer [go]])
  (:require [goog.events :as events]
            [goog.dom :as dom]
            [secretary.core :as secretary]
            [cljs.core.async :refer [<! put! chan]])
  (:import goog.History
           goog.Uri
           goog.net.Jsonp))

(def app (dom/getElement "app"))
(def search-url "http://en.wikipedia.org/w/api.php?action=opensearch&format=json&search=")
(def home-html
  (str "<h1>Wikipedia Search:</h1>"
       "<section>"
       "  <input id=\"query\" placeholder=\"Type your search...\" />"
       "  <button id=\"searchbutton\">Search</button>"
       "  <ul id=\"results\"></ul>"
       "</section>"))

(defn set-html! [el content]
  (set! (.-innerHTML el) content))

(defn render-results [results]
  (let [results (js->clj results)]
    (reduce (fn [acc result]
              (str acc "<li>" result "</li>"))
            ""
            (second results))))

(defn listen [el type]
  (let [out (chan)]
    (events/listen el type (fn [e] (put! out e)))
    out))

(defn jsonp [uri]
  (let [out (chan)
        req (Jsonp. (Uri. uri))]
    (.send req nil (fn [res] (put! out res)))
    out))

(defroute home-path "/" []
  ;; Render initial html
  (set-html! app home-html)

  (let [clicks (listen (dom/getElement "searchbutton") "click")]
    (go (while true
          (<! clicks)
          (let [uri     (str search-url (.-value (dom/getElement "query")))
                results (<! (jsonp uri))]
            (set-html! (dom/getElement "results")
                       (render-results results)))))))

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


