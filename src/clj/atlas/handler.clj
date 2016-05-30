(ns atlas.handler
  (:require [bidi.ring :as bidi]
            [ring.util.response :as resp]
            [ring.middleware.session :as session]
            [ring.middleware.json :as json]
            [buddy.auth.middleware :as buddy]
            [buddy.auth.backends.session :as bs]
            [clojurewerkz.scrypt.core :as sc]
            [datomic.api :as d]
            [atlas.utils :as utils]))

(defn index-handler [request]
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
        {p :user/password email :user/email} (d/pull db
                                                     [:user/password]
                                                     [:user/username username])]
    (if (and password p (sc/verify password p))
      (-> (resp/response {:username username
                          :email-hash (utils/md5 email)})
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

(defn query-interests
  [db req]
  (if-let [id (:identity req)]
    (let [interests (-> (d/q '[:find (pull ?act [:interest/name])
                               :in $ ?id
                               :where
                               [?e :user/username ?id]
                               [?e :user.interested-in/interests ?act]]
                             db id)
                        vec first)]
      (-> (resp/response interests)))
    (-> (resp/response [:error])
        (resp/status 401))))

(defn print-session
  [req]
  (resp/response (:session req)))

(defn base-handler
  [conn]
  (bidi/make-handler
    ["/" [[:post [["login" #(login (d/db conn) %)]
                  ["register" #(register conn %)]]]
          [:get [["session" print-session]
                 ["logout" logout]]]
          ["api/" [[:post [[["user/" :username] #(query-user (d/db conn) %)]]]
                   ["interests" #(query-interests (d/db conn) %)]]]
          [:get [["" (bidi/resources-maybe {:prefix "public/"})]
                 [#".*" index-handler]]]]]))

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
