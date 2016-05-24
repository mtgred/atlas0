(ns atlas.components
  (:require [reagent.core :as r]
            [atlas.routes :as routes]))

(defn link [{:keys [href] :as props} children]
  [:a {:href href
       :on-click (routes/goto href)}
   children])

(defn atom-input [props state]
  [:input (assoc props
                 :value @state
                 :on-change #(reset! state (-> % .-target .-value)))])

