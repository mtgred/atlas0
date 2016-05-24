(ns atlas.handlers
  (:require [re-frame.core :refer [register-handler] :as rc]
            [ajax.core :as ajax]))

(register-handler
 :go-to-page
 (fn [db [_ page]]
   (assoc db :active-page page)))

(register-handler
  :fetch-current-user
  (fn [db _]
    (ajax/GET "/session"
              {:handler (fn [resp] (rc/dispatch [:set-current-user resp]))})
    db))

(register-handler
  :set-current-user
  (fn [db [_ user]]
    (if-not (empty? user)
      (assoc db :current-user user)
      db)))
