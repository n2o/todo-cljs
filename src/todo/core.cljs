(ns todo.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

;; (println "This text is printed from src/todo/core.cljs. Go ahead and edit it and see reloading in action.")

(def initial-state
  {:count 0
   :items [{:title "Foo" :done? false}
           {:title "Bar" :done? false}
           {:title "Baz" :done? false}]})

;; Views

(defui Counter
  static om/IQuery
  (query [this]
         [:count])
  Object
  (render [this]
          (let [{:keys [count]} (om/props this)]
            (dom/div nil
                     (dom/span nil (str "Counter: " count))
                     (dom/br nil)
                     (dom/button #js {:onClick (fn [e] (om/transact! this '[(increment)]))}
                                 "Click me")))))
(def counter (om/factory Counter))

(defui Item
  static om/IQuery
  (query [this]
         [:title])
  Object
  (render [this]
          (let [{:keys [title]} (om/props this)]
            (dom/div nil
                     (dom/span #js {:className "glyphicon glyphicon-ok"})
                     " "
                     (dom/span nil title)))))
(def item (om/factory Item))

(defui Todo
  Object
  (render [this]
          (let [{:keys [items]} (om/props this)]
            (dom/div #js {:className "row"}
                     (dom/div #js {:className "col-md-offset-3 col-md-6"}
                              (dom/div #js {:className "panel panel-default"}
                                       (dom/div #js {:className "panel-heading"}
                                                "Todo")
                                       (dom/div #js {:className "panel-body"}
                                                (apply dom/div nil
                                                       (map item items)))))))))
(def todo (om/factory Todo))

(defui Main
  Object
  (render [this]
          (dom/div nil
                   (counter (om/props this))
                   (todo (om/props this)))))


;; Reconciler action

(defmulti mutate om/dispatch)
(defmethod mutate 'increment
  [{:keys [state] :as env} key params]
  {:action (fn [] (swap! state update-in [:count] inc))})

(defmulti read om/dispatch)
(defmethod read :default
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(def reconciler
  (om/reconciler
   {:state initial-state
    :parser (om/parser {:read read :mutate mutate})}))
;; (om/from-history reconciler #uuid "59a52bf6-d45e-4bc6-abfa-c89018b97bba")
;; (om/transact! reconciler '[(increment)])

(om/add-root! reconciler
              Main (gdom/getElement "app"))

(defn on-js-reload []

)
