(ns todo.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(def init-data
  {:counter 0
   :todo/items [{:title "Liste irgendwie darstellen" :done? true}
                {:title "Per Klick Items abhaken" :done? false}
                {:title "Abgeschlossene Items ausgrauen" :done? false}
                {:title "Abgeschlossene Items durchstreichen" :done? false}
                {:title "Neue Items hinzufügen" :done? false}
                {:title "Item :title zu nehmen scheint nicht sinnvoll zu sein..." :done? false}]})

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
  [{:keys [state]} _ {:keys [title]}]
  {:action (fn [] (swap! state update-in [:todo/by-title title :done?] not))})

(defmethod mutate 'todo/add
  [{:keys [state]} _ {:keys [title]}]
  {:action (fn [] (swap! state create-item title))})

;; -----------------------------------------------------------------------------
;; Auxiliary

(defn add-item [this]
  (dom/input #js {:className "form-control"
                  :placeholder "Irgendwas wollte ich doch noch erledigen..."
                  :style #js {:marginBottom "1em"}
                  :onKeyDown #(when (= (.-key %) "Enter")
                                (om/transact! this
                                   `[(todo/add {:title ~(.. % -target -value)})]))}))

(def footer
  (dom/div #js {:className "text-muted"}
           "Clojure Meetup Düsseldorf "
           (dom/a #js {:href "https://twitter.com/clojure_dus"} "@clojure_dus")
           (dom/a #js {:className "pull-right"
                       :href "https://twitter.com/cmeter_"} "@cmeter_")))

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
          (let [{:keys [title done?]} (om/props this)]
            (dom/div nil
                     (dom/input #js {:type "checkbox"
                                     :checked done?
                                     :onChange #(om/transact! this `[(todo/toggle {:title ~title})])})
                     (dom/span #js {:className (when done? "text-muted")
                                    :style (when done? #js {:textDecoration "line-through"})}
                               title)))))
(def item (om/factory Item))

(defui Main
  static om/IQuery
  (query [this]
         `[{:todo/items ~(om/get-query Item)}])
  Object
  (render [this]
          (let [{:keys [:todo/items]} (om/props this)]
            (dom/div nil
                     (dom/div #js {:className "panel panel-default"}
                              (dom/div #js {:className "panel-heading"} "Todos")
                              (dom/div #js {:className "panel-body"}
                                       (add-item this)
                                       (map item items)))
                     footer))))

(defonce reconciler
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
;; (om/from-history reconciler #uuid "3a92181a-f24d-4a0e-8f05-548e2419e38a")

;; Aktueller Zustand
;; @(om/app-state reconciler)
