(ns atlas.handlers
  (:require [re-frame.core :refer [register-handler]]))

(register-handler
 :go-to-page
 (fn [db [_ page]]
   (assoc db :active-page page)))
