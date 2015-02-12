(ns cljsworkshop.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljsjs.react :as react]
            [clojure.string :as str]
            [goog.dom :as gdom]
            [goog.events :as events]
            [cljs.core.async :refer [<! put! chan]]
            [om.core :as om :include-macros true]
            [hodgepodge.core :refer [local-storage]]
            [sablono.core :as html :refer-macros [html]]
            [cljsworkshop.moment :as moment]))

(enable-console-print!)

(defonce tasklist-state (atom {:entries []}))
(defonce undo-state (atom {:entries [@tasklist-state]}))

(add-watch tasklist-state :history
  (fn [_ _ _ n]
    (println "Event:" n)
    (let [entries (:entries @undo-state)]
      (when-not (= (last entries) n)
        (swap! undo-state
               (fn [state]
                 (update-in state [:entries] conj n)))))))

;; Get the persisted state, and if it exists
;; restore it on tasklist and undo states.
(when-let [state (:taskliststate local-storage)]
  (reset! tasklist-state state)
  (reset! undo-state {:entries [state]}))

;; Watch tasklist-state changes and
;; persists them in local storege.
(add-watch tasklist-state :persistece
  (fn [_ _ _ n]
    (println "Event:" n)
    (assoc! local-storage :taskliststate n)))

(defn taskitem
  [task owner]
  (reify
    om/IDisplayName
    (display-name [_]
      "taskitem")

    om/IRender
    (render [_]
      (let [subject (:subject task)
            completed? (:completed task)]
        (html
         [:li {:on-click (fn [_] (om/transact! task :completed #(not %)))}
          (if completed?
           [:span {:style {:text-decoration "line-through"}} subject]
           [:span subject])])))))


(defn form-submit
  [app counter event]
  (.preventDefault event)
  (let [input (-> (.-target event)
                  (.querySelector "[name=subject]"))
        task  {:subject (.-value input)
               :id (swap! counter inc)
               :completed false}]

    ;; Set the input to empty value
    (set! (.-value input) "")

    ;; Append the previously defined task
    ;; to the task list entries on global
    ;; state atom.
    (om/transact! app :entries #(conj % task))))

(defn tasklist
  [app owner]
  (reify
    om/IDisplayName
    (display-name [_]
      "tasklist")

    om/IInitState
    (init-state [_]
      {:counter (atom 1)})

    om/IRenderState
    (render-state [_ {:keys [counter]}]
      (let [entries (:entries app)]
        (html
         [:section {:style {:margin-top "20px"
                            :padding "5px"
                            :border "1px solid #ddd"}}
          [:section.title
           [:strong "Task list:"]]
          [:section.input
           [:form {:on-submit #(form-submit app counter %)}
            [:input {:type "text"
                     :name "subject"
                     :placeholder "Write your task name..."}]
            [:input {:type "submit"
                     :defaultValue "Foo"}]]]
          [:section.list {:style {:margin-top "10px"}}
           (if (empty? entries)
             [:span "No items on the task list..."]
             [:ul (for [item entries]
                    (om/build taskitem item {:key :created-at}))])]])))))

(defn do-undo
  [app]
  (when (> (count (:entries @app)) 1)
    ;; remove the last spapshot from the undo list.
    (om/transact! app :entries pop)

    ;; Restore the last snapshot into tasklist
    ;; application state
    (reset! tasklist-state (last (:entries @undo-state)))))


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
                  :on-click (fn [_] (do-undo app))}]]]))))

(let [undoel (gdom/getElement "undo")
      tasklistel (gdom/getElement "tasklist")]
  (om/root undo undo-state {:target undoel})
  (om/root tasklist tasklist-state {:target tasklistel}))
