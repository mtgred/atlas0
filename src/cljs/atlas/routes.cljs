(ns atlas.routes
  (:require [re-frame.core :refer [dispatch]]
            [bidi.bidi :as bidi]
            [pushy.core :as pushy]))

(def routes
  ["/" {"" :home
        "about" :about
        "login" :login
        "a/" {[:name] :subatlas}}])

(defn go-to-page [page]
  (dispatch [:go-to-page page]))

(def history
  (pushy/pushy go-to-page (partial bidi/match-route routes)))

(pushy/start! history)

(defn goto [url]
  (fn [e]
    (.preventDefault e)
    (pushy/set-token! history url)))
