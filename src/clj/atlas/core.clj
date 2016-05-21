(ns atlas.core
  (:require [org.httpkit.server :refer [run-server]]
            [datomic.api :as d]
            [environ.core :as env]
            [atlas.handler :refer [handler]]
            [atlas.db :as db]))

(defn -main []
  (let [db-uri (env/env :database-url)
        db-conn (db/init db-uri)]
    (println "Atlas server running on port 2442")
    (run-server (handler db-conn) {:port 2442})))

(comment
  ;; Start with in-memory db

  (def db-conn (db/init "datomic:mem://atlas"))

  (def srv (run-server (handler db-conn)
                       {:port 2442}))

  (.close srv)

  (require '[clojurewerkz.scrypt.core :as sc])

  (sc/encrypt "password" 16384 8 1)

  )
