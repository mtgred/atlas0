(ns atlas.pages
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [atlas.routes :as routes]
            [atlas.components :refer [atom-input link]]))

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

(defn login []
  (let [username (r/atom "")
        password (r/atom "")
        error-msg (r/atom "")]
    (fn []
     [:div
      [:h1 "Login"]
      [:form {:on-submit (fn [e]
                           (.preventDefault e)
                           (go (let [resp (<! (http/post "/login"
                                                         {:json-params {:username @username
                                                                        :password @password}}))]
                                 (if (:success resp)
                                   (routes/goto "/")
                                   (reset! error-msg "Invalid login or password")))))}
       [:p
        [:label "Username"]]
       [:p
        [atom-input {:type "text"} username]]
       [:p
        [:label "Password"]]
       [:p
        [atom-input {:type "password"} password]]
       [:button "Login"]
       (when-not (empty? @error-msg)
         [:p @error-msg])]])))

(defn get-page [{:keys [handler route-params]}]
  (case handler
    :home [home]
    :about [about]
    :login [login]
    :subatlas [subatlas (:name route-params)]
    [:div]))

(defn main []
  (let [active-page (subscribe [:active-page])]
    (fn []
      (get-page @active-page))))
