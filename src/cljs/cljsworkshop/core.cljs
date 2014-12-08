(ns cljsworkshop.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [clojure.string :as str]
            [goog.dom :as gdom]
            [goog.events :as events]
            [cljs.core.async :refer [<! put! chan]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]))

(enable-console-print!)

(defonce tasklist-state (atom {:entries []}))
(defonce undo-state (atom {:entries [@tasklist-state]
                           :index nil}))

(add-watch tasklist-state :history
  (fn [_ _ _ n]
    (println "Event:" n)
    (let [entries (:entries @undo-state)]
      (when-not (= (last entries) n)
        (swap! undo-state
               (fn [state]
                 (update-in state [:entries] conj n)))))))

(defn tasklist
  [app owner]
  (reify
    om/IDisplayName
    (display-name [_]
      "tasklist")

    om/IRender
    (render [_]
      (let [entries (:entries app)]
        (html
         [:section {:style {:margin-top "20px"
                            :padding "5px"
                            :border "1px solid #ddd"}}
          [:section.title
           [:strong "Task list:"]]
          [:section.input
           [:form {:on-submit (fn [e]
                                (.preventDefault e)
                                (let [input (-> (.-target e)
                                                (.querySelector "[name=subject]"))
                                      task  {:subject (.-value input)
                                             :state :todo}]
                                  (set! (.-value input) "")
                                  (om/transact! app :entries #(conj % task))))}
            [:input {:type "text"
                     :name "subject"
                     :placeholder "Write your task name..."}]
            [:input {:type "submit"
                     :defaultValue "Foo"}]]]
          [:section.list {:style {:margin-top "10px"}}
           (if (empty? entries)
             [:span "No items on the task list..."]
             [:ul
              (for [item entries]
                (condp = (:state item)
                  :done [:li {:style {:text-decoration "line-through"}}
                         (:subject item)]
                  :todo [:li (:subject item)]))])]])))))


(defn undo
  [app owner]
  (reify
    om/IRender
    (render [_]
      (html
       [:section.undo {:style {:padding "5px"
                               :border "1px solid #ddd"}}
        [:section.buttons
         [:input {:type "button" :default-value "Undo"
                  :on-click (fn [event]
                              (when (> (count (:entries @app)) 1)
                                (om/transact! app :entries pop)
                                (reset! tasklist-state (last (:entries @undo-state)))))}]]]))))

(let [undoel (gdom/getElement "undo")
      tasklistel (gdom/getElement "tasklist")]
  (om/root undo undo-state {:target undoel})
  (om/root tasklist tasklist-state {:target tasklistel}))
