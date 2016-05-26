(ns atlas.handler
  (:require [bidi.ring :as bidi]
            [ring.util.response :as resp]
            [ring.middleware.session :as session]
            [ring.middleware.json :as json]
            [buddy.auth.middleware :as buddy]
            [buddy.auth.backends.session :as bs]
            [clojurewerkz.scrypt.core :as sc]
            [datomic.api :as d]))

(defn index-handler [request]
  (resp/file-response "index.html" {:root "resources/public"}))

(defn query-user
  [db]
  (fn [{:as req {:keys [username]} :route-params}]
    (let [u (d/pull db
                    [:user/password :user/email]
                    [:user/username username])
          p (if (:body req)
              (slurp (:body req))
              "")]
      (if (sc/verify p (:user/password u))
        (-> (resp/response (:user/email u))
            (resp/status 200))
        (-> (resp/response "Unauthorized.")
            (resp/status 401))))))

(defn login
  [db]
  (fn [req]
    (let [{{:keys [username password next-url]} :body} req
          {p :user/password} (d/pull db
                                     [:user/password]
                                     [:user/username username])]
      (if (and password p (sc/verify password p))
        (-> (resp/response [:success])
            (assoc :session (assoc (:session req) :identity username)))
        (-> (resp/response [:error])
            (resp/status 401))))))

(defn query-interests
  [db]
  (fn [req]
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
          (resp/status 401)))))

(defn print-session
  [req]
  (resp/response (:session req)))

(defn base-handler
  [conn]
  (bidi/make-handler
    ["/" [[:post [["login" (login (d/db conn))]]]
          [:get [["session" print-session]]]
          ["api/" [[:post [[["user/" :username] (query-user (d/db conn))]]]
                   ["interests" (query-interests (d/db conn))]]]
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
