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

(def pages
  {:home [home]
   :about [about]})

(defn main []
  (let [active-page (subscribe [:active-page])]
    (if-let [page (pages (:handler @active-page))]
      page
      [:div])))
