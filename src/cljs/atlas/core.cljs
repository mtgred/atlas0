(ns atlas.core
    (:require [reagent.core :as reagent]
              [re-frame.core :as re-frame]
              [atlas.handlers]
              [atlas.subs]
              [atlas.pages :as pages]
              [atlas.config :as config]))

(when config/debug?
  (println "dev mode"))

(defn mount-root []
  (reagent/render [pages/main-page]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize-db])
  (mount-root))
