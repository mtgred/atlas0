(ns atlas.core
  (:require [org.httpkit.server :refer [run-server]]
            [atlas.handler :refer [handler]]))

(defn -main []
  (println "Atlas server running on port 2442")
  (run-server handler {:port 2442}))
