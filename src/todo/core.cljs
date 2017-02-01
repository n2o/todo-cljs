(ns todo.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(def init-data
  {:count 0
   :todo/items [{:title "Liste irgendwie darstellen" :done? true}
                {:title "Abgeschlossene Items durchstreichen" :done? false}
                {:title "Abgeschlossene Items ausgrauen" :done? false}
                {:title "Per Klick Items abhaken" :done? true}
                {:title "Formular für neue Items" :done? false}
                {:title "Nach Kategorien sortieren" :done? false}
                {:title "Fertig werden, weil alle nach Hause wollen" :done? false}
                {:title "cljs / om f*** ups" :done? false}]
   :todo/filter :all})

;; -----------------------------------------------------------------------------
;; Parsing

(defn get-items [state key]
  (let [st @state]
    (into [] (map #(get-in st %)) (get st key))))

(defmulti read om/dispatch)
(defmethod read :todo/items
  [{:keys [state]} key params]
  {:value (get-items state key)})

(defmulti mutate om/dispatch)
(defmethod mutate 'todo/toggle
  [{:keys [state]} _ {:keys [title done?]}]
  {:action (fn [] (swap! state update-in [:todo/by-title title :done?] #(not done?)))})

(defmethod mutate 'todo/add
  [{:keys [state]} _ {:keys [value]}]
  {:action (fn [] (do
                   (swap! state update-in [:todo/items] into {:todo/by-title value})
                   (swap! state update-in [:todo/by-title] assoc value {:title value :done? false})))})
;; (om/transact! reconciler `[(todo/toggle {:title "Abgeschlossene Items durchstreichen" :done? true})])
;; (om/transact! reconciler `[(todo/add {:value "foo"})])

;; -----------------------------------------------------------------------------
;; Auxiliary

(defn strike-through [done?]
  (if done?
    #js {:textDecoration "line-through"}
    #js {}))

;; -----------------------------------------------------------------------------
;; Components

(defui Item
  static om/Ident
  (ident [this {:keys [title]}]
         [:todo/by-title title])
  static om/IQuery
  (query [this]
         [:title :done?])
  Object
  (render [this]
          (let [{:keys [title done?] :as props} (om/props this)]
            (dom/div #js {:className (str "row" (when done? " text-muted"))}
                     (dom/div #js {:className "col-xs-1"}
                              (dom/input #js {:type "checkbox"
                                              :checked done?
                                              :onChange #(om/transact! this `[(todo/toggle ~props)])}))
                     (dom/div #js {:className "col-xs-11"
                                   :style (strike-through done?)}
                              title)))))
(def item (om/factory Item))

(defn todo-add [this]
  (dom/input #js {:className "form-control"
                  :placeholder "do something"
                  :onKeyDown #(when (= (.-key %) "Enter")
                                (om/transact! this `[(todo/add {:value ~(.. % -target -value)})]))}))

(defui TodoList
  Object
  (render [this]
          (let [items (om/props this)]
            (dom/div #js {:className "panel panel-default"}
                     (dom/div #js {:className "panel-heading"}
                              "Todo List")
                     (dom/div #js {:className "panel-body"}
                              (map item items)
                              (todo-add this))))))
(def todo-list (om/factory TodoList))

(defui Main
  static om/IQuery
  (query [this]
         `[{:todo/items ~(om/get-query Item)}])
  Object
  (render [this]
          (let [{:keys [todo/items]} (om/props this)]
            (todo-list items))))

(defonce reconciler
  (om/reconciler
   {:state  init-data
    :parser (om/parser {:read read
                        :mutate mutate})}))

(om/add-root! reconciler
              Main (gdom/getElement "app"))


;; -----------------------------------------------------------------------------

;; Normalisierte Daten liegen vor, wenn wir Ident definiert haben.
;; (def norm-data (om/tree->db Main init-data true))
;; norm-data
