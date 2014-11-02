(ns cljsworkshop.utils
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.events :as events]
            [goog.dom :as dom]
            [cljs.core.async :refer [<! put! chan timeout]])
  (:import goog.History
           goog.Uri
           goog.net.XhrIo
           goog.net.Jsonp))

(defn set-html! [el content]
  "Set html to dom element."
  (set! (.-innerHTML el) content))

(defn listen [el type]
  "Function for attach a listener to
  dom events and returns a core.async channel."
  (let [out (chan)]
    (events/listen el type (fn [e] (put! out e)))
    out))

(defn jsonp [uri]
  "Function for send jsonp requests, returns
  a core.async channel."
  (let [out (chan)
        req (Jsonp. (Uri. uri))]
    (.send req nil (fn [res] (put! out res)))
    out))

(defn xhr [uri]
  (let [out (chan)
        uri (Uri. uri)
        req (XhrIo.)]
    (events/listen req "success"
                   (fn [res]
                     (let [response (.getResponseJson (.-target res))]
                       (put! out response))))
    (.send req uri)
    ;; (fn [res] (put! out res)))
    out))

(defn throttle
  ([source msecs]
     (throttle (chan) source msecs))
  ([c source msecs]
     (go
       (loop [state ::init last nil cs [source]]
         (let [[_ sync] cs]
          (let [[v sc] (alts! cs)]
            (condp = sc
              source (condp = state
                       ::init (do (>! c v)
                                  (recur ::throttling last
                                         (conj cs (timeout msecs))))
                       ::throttling (recur state v cs))
              sync (if last
                     (do (>! c last)
                         (recur state nil
                                (conj (pop cs) (timeout msecs))))
                     (recur ::init last (pop cs))))))))
     c))

(defn debounce
  ([source msecs]
     (debounce (chan) source msecs))
  ([c source msecs]
     (go
       (loop [state ::init cs [source]]
         (let [[_ threshold] cs]
           (let [[v sc] (alts! cs)]
             (condp = sc
               source (condp = state
                        ::init
                        (do (>! c v)
                            (recur ::debouncing
                                   (conj cs (timeout msecs))))
                        ::debouncing
                        (recur state
                               (conj (pop cs) (timeout msecs))))
               threshold (recur ::init (pop cs)))))))
     c))
