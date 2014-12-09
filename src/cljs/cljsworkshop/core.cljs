(ns cljsworkshop.core)

(defn set-html! [el content]
  (set! (.-innerHTML el) content))

(defn main
  []
  (let [content "Hello World from Clojure Script"
        element (aget (js/document.getElementsByTagName "main") 0)]
    (set-html! element content)))

(main)
