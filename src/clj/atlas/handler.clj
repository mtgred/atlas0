(ns atlas.handler
  (:require [bidi.ring :as bidi]
            [ring.util.response :as resp]
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

(defn handler
  [conn]
  (bidi/make-handler
    ["/" [["api" {:post
                  {["/user/" :username] (query-user (d/db conn))}}]
          ["" (bidi/resources-maybe {:prefix "public/"})]
          [#".*" index-handler]]]))
