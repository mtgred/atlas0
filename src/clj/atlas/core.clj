(ns atlas.core
  (:require [org.httpkit.server :refer [run-server]]
            [datomic.api :as d]
            [environ.core :as env]
            [atlas.handler :as h]
            [atlas.db :as db]))

(defn -main []
  (let [db-uri (env/env :database-url)
        db-conn (db/init db-uri)]
    (println "Atlas server running on port 2442")
    (run-server (h/handler db-conn) {:port 2442})))

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

  ;; $ curl -v -X POST -d '{"username": "mtgred", "password": "password"}' \
  ;;           -H 'Content-Type: application/json; charset=utf-8' \
  ;;           localhost:2442/login
  ;; *   Trying ::1...
  ;; * Connected to localhost (::1) port 2442 (#0)
  ;; > POST /login HTTP/1.1
  ;; > Host: localhost:2442
  ;; > User-Agent: curl/7.43.0
  ;; > Accept: */*
  ;; > Content-Type: application/json; charset=utf-8
  ;; > Content-Length: 46
  ;; >
  ;; * upload completely sent off: 46 out of 46 bytes
  ;; < HTTP/1.1 200 OK
  ;; < Set-Cookie: ring-session=1dd79a35-8bb8-42da-9213-ff41dcf68623;Path=/;HttpOnly
  ;; < Content-Type: application/json; charset=utf-8
  ;; < Content-Length: 11
  ;; < Server: http-kit
  ;; < Date: Sun, 22 May 2016 16:49:08 GMT
  ;; <
  ;; * Connection #0 to host localhost left intact
  ;; ["success"]
  ;; $ curl -v --cookie 'ring-session=1dd79a35-8bb8-42da-9213-ff41dcf68623;Path=/;HttpOnly' localhost:2442/session
  ;; *   Trying ::1...
  ;; * Connected to localhost (::1) port 2442 (#0)
  ;; > GET /session HTTP/1.1
  ;; > Host: localhost:2442
  ;; > User-Agent: curl/7.43.0
  ;; > Accept: */*
  ;; > Cookie: ring-session=1dd79a35-8bb8-42da-9213-ff41dcf68623;Path=/;HttpOnly
  ;; >
  ;; < HTTP/1.1 200 OK
  ;; < Content-Type: application/json; charset=utf-8
  ;; < Content-Length: 21
  ;; < Server: http-kit
  ;; < Date: Sun, 22 May 2016 16:49:36 GMT
  ;; <
  ;; * Connection #0 to host localhost left intact
  ;; {"identity":"mtgred"}

  (require '[clojurewerkz.scrypt.core :as sc])

  (sc/encrypt "password" 16384 8 1)
  )
