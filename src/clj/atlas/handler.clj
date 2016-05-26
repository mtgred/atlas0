(ns atlas.handler
  (:require [compojure.core :as cj]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [ring.middleware.session :as session]
            [ring.middleware.json :as json]
            [buddy.auth.middleware :as buddy]
            [buddy.auth.backends.session :as bs]
            [clojurewerkz.scrypt.core :as sc]
            [datomic.api :as d]))

(defn index-handler []
  (resp/file-response "index.html" {:root "resources/public"}))

(defn query-user
  [db {:as req {:keys [username]} :route-params}]
  (let [u (d/pull db
                  [:user/password :user/email]
                  [:user/username username])
        p (if (:body req)
            (slurp (:body req))
            "")]
    (if (sc/verify p (:user/password u))
      (-> (resp/response (:user/email u)))
      (-> (resp/response [:error "Unauthorized."])
          (resp/status 401)))))

(defn login
  [db req]
  (let [{{:keys [username password next-url]} :body} req
        {p :user/password} (d/pull db
                                   [:user/password]
                                   [:user/username username])]
    (if (and password p (sc/verify password p))
      (-> (resp/response [:success])
          (assoc :session (assoc (:session req) :identity username)))
      (-> (resp/response [:error "Unauthorized."])
          (resp/status 401)))))

(defn logout
  [req]
  (-> (resp/response "")
      (assoc :session (dissoc (:session req) :identity))))

(defn hash-password
  [p]
  (sc/encrypt p 16384 8 1))

(defn register
  [conn req]
  (if (:identity req)
    (-> (resp/response [:error "Already logged in."])
        (resp/status 403))
    (let [desired-username (get-in req [:body :username])
          desired-password (get-in req [:body :password])]
      (if (and desired-password desired-username)
        (let [now (java.util.Date.)
              tx-result (d/transact conn
                                    [{:db/id #db/id [:db.part/user]
                                      :user/username desired-username
                                      :user/password (hash-password desired-password)
                                      :user/registration-date now
                                      :user/last-connection now}])]
          (try
            @tx-result
            (-> (resp/response {:user/username desired-username})
                (assoc :session (assoc (:session req) :identity desired-username)))
            (catch java.util.concurrent.ExecutionException _
              (-> (resp/response [:error "Username already taken."])
                  (resp/status 409)))))
        (-> (resp/response [:error "Missing fields."])
            (resp/status 400))))))

(defn print-session
  [req]
  (resp/response (:session req)))

(defn base-handler
  [conn]
  (cj/routes
    (cj/routes
      (cj/POST "/login" req (login (d/db conn) req))
      (cj/POST "/register" req (register conn req))
      (cj/GET "/session" req (print-session req))
      (cj/GET "/logout" req (logout req)))
    (cj/routes
      (cj/POST "/api/user/:username" req (query-user (d/db conn) req)))
    (cj/routes
      (route/resources "/" {:root "public/"})
      (route/not-found (index-handler)))))

(defn wrap-handler
  [h]
  (-> h
      (buddy/wrap-authentication (bs/session-backend))
      session/wrap-session
      (json/wrap-json-body {:keywords? true})
      json/wrap-json-response))

(defn handler
  [conn]
  (wrap-handler (base-handler conn)))
