(ns todo.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

;; (println "This text is printed from src/todo/core.cljs. Go ahead and edit it and see reloading in action.")

(defonce initial-state {:count 0})

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

(defui MainTemplate
  Object
  (render [this]
          (dom/div nil
                   (dom/strong nil "Foo")
                   (counter (om/props this)))))


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

(defonce reconciler
  (om/reconciler
   {:state initial-state
    :parser (om/parser {:read read :mutate mutate})}))
;; (om/from-history reconciler #uuid "59a52bf6-d45e-4bc6-abfa-c89018b97bba")
;; (om/transact! reconciler '[(increment)])

(om/add-root! reconciler
              MainTemplate (gdom/getElement "app"))

(defn on-js-reload []

)
