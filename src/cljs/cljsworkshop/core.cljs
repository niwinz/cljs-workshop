(ns cljsworkshop.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljsjs.react :as react]
            [clojure.string :as str]
            [goog.dom :as gdom]
            [goog.events :as events]
            [cljs.core.async :refer [<! put! chan]]
            [hodgepodge.core :refer [local-storage]]
            [om.core :as om]
            [om-tools.dom :as dom]))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Task Item Component
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- render-taskitem
  [task owner]
  (let [subject (:subject task)
        completed? (:completed task)]
    (dom/li {:on-click (fn [_] (om/transact! task :completed #(not %)))}
      (if completed?
        (dom/span {:style {:text-decoration "line-through"}} subject)
        (dom/span subject)))))

(defn taskitem
  [state owner]
  (reify
    om/IDisplayName
    (display-name [_]
      "taskitem")

    om/IRender
    (render [_]
      (render-taskitem state owner))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Task List Component
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- submit
  "On tasklist form submit callback."
  [state counter event]
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
    (om/transact! state :entries #(conj % task))))

(defn- render-tasklist
  [state owner {:keys [counter] :as local}]
  (dom/section {:style {:margin-top "20px"
                        :padding "5px"
                        :border "1px solid #ddd"}}
    (dom/section {:class "title"}
      (dom/strong "Task list:"))
    (dom/section {:class "input"}
      (dom/form {:on-submit #(submit state counter %)}
        (dom/input {:type "text"
                    :name "subject"
                    :placeholder "Write your task name..."})
        (dom/input {:type "submit"
                    :default-value "Foo"}))
      (dom/section {:class "list" :style {:margin-top "10px"}}
        (if-let [entries (seq (:entries state))]
          (apply dom/ul (for [item entries]
                          (om/build taskitem item {:key :id})))
          (dom/span "No items on the task list..."))))))

(defn tasklist
  [state owner]
  (reify
    om/IDisplayName
    (display-name [_]
      "tasklist")

    om/IInitState
    (init-state [_]
      {:counter (atom 1)})

    om/IRenderState
    (render-state [_ local]
      (render-tasklist state owner local))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Undo Component
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn do-undo
  [state]
  (when (> (count (:entries @state)) 1)
    ;; remove the last spapshot from the undo list.
    (om/transact! state :entries pop)

    ;; Restore the last snapshot into tasklist
    ;; application state
    (reset! tasklist-state (last (:entries @undo-state)))))


(defn undo
  [state owner]
  (reify
    om/IRender
    (render [_]
      (dom/section {:class "undo"
                    :style {:padding "5px"
                            :border "1px solid #ddd"}}
        (dom/section {:class "buttons"}
          (dom/input {:type "button"
                      :default-value "Undo"
                      :on-click (fn[_] (do-undo state))}))))))

(let [undoel (gdom/getElement "undo")
      tasklistel (gdom/getElement "tasklist")]
  (om/root undo undo-state {:target undoel})
  (om/root tasklist tasklist-state {:target tasklistel}))
