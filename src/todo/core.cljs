(ns todo.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(def init-data
  {:todo/items [{:title "Liste irgendwie darstellen" :done? false}
                {:title "Abgeschlossene Items durchstreichen" :done? false}
                {:title "Abgeschlossene Items ausgrauen" :done? false}
                {:title "Per Klick Items abhaken" :done? false}
                {:title "Formular für neue Items" :done? false}
                {:title "Nach Kategorien sortieren" :done? false}]})

;; -----------------------------------------------------------------------------
;; Parsing

(defn get-items [state key]
  (let [st @state]
    (into [] (map #(get-in st %)) (get st key))))

(defmulti read om/dispatch)
(defmethod read :todo/items
  [{:keys [state] :as env} key params]
  {:value (get-items state key)})

(defmulti mutate om/dispatch)
(defmethod mutate 'todo/toggle
  [{:keys [state]} _ {:keys [title done?]}]
  {:action (fn [] (swap! state update-in
                        [:todo/by-title title :done?] #(not done?)))})

;; -----------------------------------------------------------------------------
;; Components

(defui Item
  static om/Ident
  (ident [this {:keys [title]}]
         [:todo/by-title title])
  static om/IQuery
  (query [this]
         '[:title :done?])
  Object
  (render [this]
          (let [{:keys [title done?] :as props} (om/props this)]
            (dom/li nil
                    (dom/button #js{:onClick
                                    (fn [e](om/transact!
                                           this `[(todo/toggle ~props)]))}
                                "toggle")
                    " "
                    (dom/label nil (str title ", done? " done?))))))
(def item (om/factory Item))

(defui ListView
  Object
  (render [this]
          (let [items (om/props this)]
            (dom/ul nil
                    (map item items)))))

(def list-view (om/factory ListView))

(defui RootView
  static om/IQuery
  (query [this]
         `[{:todo/items ~(om/get-query Item)}])
  Object
  (render [this]
          (let [{:keys [todo/items]} (om/props this)]
            (list-view items))))

(def reconciler
  (om/reconciler
   {:state  init-data
    :parser (om/parser {:read read :mutate mutate})}))

(om/add-root! reconciler
              RootView (gdom/getElement "app"))


;; -----------------------------------------------------------------------------
;; Zum Analysieren

;; Normalisierte Daten liegen vor, wenn wir Ident definiert haben.
;; (def norm-data (om/tree->db RootView init-data true))
;; norm-data
