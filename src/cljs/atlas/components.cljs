(ns atlas.components
  (:require [reagent.core :as r]
            [re-frame.core :as rc]
            [atlas.routes :as routes]))

(defn link [{:keys [href] :as props} children]
  [:a {:href href
       :on-click (fn [e]
                   (.preventDefault e)
                   (routes/goto href))}
   children])

(defn atom-input [props state]
  [:input (assoc props
                 :value @state
                 :on-change #(reset! state (-> % .-target .-value)))])

(defn login-header
  []
  (let [current-user (rc/subscribe [:current-user])]
    (fn []
      (if-let [username @current-user]
        [:div "Logged in as " username]
        [:div "Anonymous"]))))
