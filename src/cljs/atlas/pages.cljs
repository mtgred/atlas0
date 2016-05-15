(ns atlas.pages
  (:require [re-frame.core :refer [subscribe]]
            [atlas.routes :refer [link]]))

(defn home []
  [:div
   [:h1 "Home page"]
   [:div
    [link {:href "/about"} "About"]]])

(defn about []
  [:div
   [:h1 "About page"]
   [:div
    [link {:href "/"} "Home"]]])

(defn subatlas [name]
  [:div
   [:h1 (str "Subatlas " name)]])

(defn get-page [{:keys [handler route-params]}]
  (case handler
    :home [home]
    :about [about]
    :subatlas [subatlas (:name route-params)]
    [:div]))

(defn main []
  (let [active-page (subscribe [:active-page])]
    (fn []
      (get-page @active-page))))
