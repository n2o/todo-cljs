(ns todo.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(def init-data
  {:count 0
   :todo/items [{:title "Liste irgendwie darstellen" :done? true}
                {:title "Abgeschlossene Items durchstreichen" :done? true}
                {:title "Abgeschlossene Items ausgrauen" :done? true}
                {:title "Per Klick Items abhaken" :done? true}
                {:title "Fertig werden, weil alle nach Hause wollen" :done? false}
                {:title "cljs / om f*** ups" :done? false}]})

;; -----------------------------------------------------------------------------
;; Parsing

(defn create-item [state title]
  (-> state
      (update-in [:todo/items] into {:todo/by-title title})
      (assoc-in [:todo/by-title title] {:title title :done? false})))

(defn get-items [state key]
  (let [st @state]
    (mapv #(get-in st %) (get st key))))

(defmulti read om/dispatch)
(defmethod read :todo/items
  [{:keys [state]} key _]
  {:value (get-items state key)})

(defmulti mutate om/dispatch)
(defmethod mutate 'todo/toggle
  [{:keys [state]} _ {:keys [title done?]}]
  {:action (swap! state update-in [:todo/by-title title :done?] #(not done?))})

(defmethod mutate 'todo/add
  [{:keys [state]} _ {:keys [title]}]
  {:action (fn [] (swap! state create-item title))})

;; (om/transact! reconciler '[(increment)])
;; (om/transact! reconciler '[(todo/add {:title "foo2"})])


;; -----------------------------------------------------------------------------
;; Auxiliary

(defn line-through [done?]
  (if done?
    #js {:textDecoration "line-through"}
    #js {}))

(defn add-item [this]
  (dom/input #js {:className "form-control"
                  :placeholder "Irgendwas wollte ich doch noch erledigen..."
                  :onKeyDown #(when (= (.-key %) "Enter")
                                (om/transact! this `[(todo/add {:title ~(.. % -target -value)})]))}))

;; -----------------------------------------------------------------------------
;; Components

(defui Item
  static om/IQuery
  (query [this]
         [:title :done?])
  static om/Ident
  (ident [this {:keys [title]}]
         [:todo/by-title title])
  Object
  (render [this]
          (let [{:keys [title done?] :as props} (om/props this)]
            (dom/div nil
                     (dom/input #js {:type "checkbox"
                                     :checked done?
                                     :onChange #(om/transact! this `[(todo/toggle ~props)])})
                     (dom/span #js {:className (when done? "text-muted")
                                    :style (line-through done?)}
                               title)))))
(def item (om/factory Item))

(defui Main
  static om/IQuery
  (query [this]
         `[{:todo/items ~(om/get-query Item)}])
  Object
  (render [this]
          (let [{:keys [todo/items]} (om/props this)]
            (dom/div #js {:className "panel panel-default"}
                     (dom/div #js {:className "panel-heading"}
                              "Todos")
                     (dom/div #js {:className "panel-body"}
                              (map item items)
                              (add-item this))))))

(def reconciler
  (om/reconciler
   {:state  init-data
    :parser (om/parser {:read read :mutate mutate})}))

(om/add-root! reconciler
              Main (gdom/getElement "app"))

;; -----------------------------------------------------------------------------

;; Normalisierte Daten liegen vor, wenn wir Ident definiert haben.
;; (def norm-data (om/tree->db Main init-data true))
;; norm-data

;; Time Travelling
;; (om/from-history reconciler #uuid "719e70ad-cca7-467d-96f1-eea41a5648fd")
;; Aktueller Zustand
;; @(om/app-state reconciler)


;; (defmethod mutate 'increment
;;   [{:keys [state]} _ _]
;;   {:action (fn [] (swap! state update-in [:count] inc))})
