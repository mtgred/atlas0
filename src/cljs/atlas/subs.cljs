(ns atlas.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub] :as rc]))

(register-sub :active-page (fn [db _] (reaction (:active-page @db))))

(register-sub
  :current-user
  (fn [db _]
    (let [user (reaction (:current-user @db))]
      (reaction
        (if @user
          (:user/username @user)
          (do (rc/dispatch [:fetch-current-user])
              nil))))))
