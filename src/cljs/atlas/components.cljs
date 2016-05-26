(ns atlas.components
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe]]
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

(defn user-view
  ([user] (user-view user 24))
  ([{:keys [username email-hash] :as user} size]
   [:div
    [:img {:src (str "http://www.gravatar.com/avatar/" email-hash "?d=retro&s=" size)}]
    username]))

(defn navbar []
  (let [current-user (subscribe [:current-user])]
    (fn []
     [:div.navbar
      (if @current-user
        [user-view @current-user]
        [:div
         [link {:href "/register"} "Register"]
         [link {:href "/login"} "Login"]])])))
