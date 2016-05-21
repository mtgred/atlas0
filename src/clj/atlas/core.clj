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

  (def db-conn (db/init (str "datomic:mem://" (d/squuid))))

  (def stop-dev-server
    (run-server (fn [req]
                  ((handler db-conn) req))
                {:port 2442}))

  (stop-dev-server)

  (require '[clojurewerkz.scrypt.core :as sc])

  (sc/encrypt "password" 16384 8 1)

  ;; $ curl -X POST -d 'password' localhost:2442/api/user/mtgred
  ;; mtgred@gmail.com
  ;; $ curl -X POST -d 'passwrd' localhost:2442/api/user/mtgred
  ;; Unauthorized.

  )
