(ns atlas.core
  (:require [org.httpkit.server :refer [run-server]]
            [datomic.api :as d]
            [environ.core :as env]
            [atlas.handler :as h]
            [atlas.db :as db]))

(defn -main []
  (let [db-uri (env/env :database-url "datomic:mem://temp")
        db-conn (db/init db-uri)
        port (env/env :port 2442)]
    (printf "Atlas server running on port %s with db %s\n"
            port db-uri)
    (run-server (h/handler db-conn) {:port port})))

(comment
  ;; Start with in-memory db

  (def db-conn (db/init (str "datomic:mem://" (d/squuid))))

  (def stop-dev-server
    (run-server
      (h/wrap-handler
        (fn [req]
          ((h/base-handler db-conn) req)))
      {:port 2442}))

  (stop-dev-server)

  (require '[clojurewerkz.scrypt.core :as sc])

  (sc/encrypt "password" 16384 8 1)
  )
