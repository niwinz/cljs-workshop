(ns cljsworkshop.core
  (:require-macros [secretary.core :refer [defroute]])
  (:require [goog.events :as events]
            [goog.dom :as dom]
            [secretary.core :as secretary])
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

(defn do-jsonp
  [uri callback]
  (let [req (Jsonp. (Uri. uri))]
    (.send req nil callback)))

(defroute home-path "/" []
  (set-html! app home-html)
  (let [on-response     (fn [results]
                          (let [html (render-results results)]
                            (set-html! (dom/getElement "results") html)))

        on-search-click (fn [e]
                          (let [userquery (.-value (dom/getElement "query"))
                                searchuri (str search-url userquery)]
                            (do-jsonp searchuri on-response)))]

    (events/listen (dom/getElement "searchbutton") "click" on-search-click)))

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


