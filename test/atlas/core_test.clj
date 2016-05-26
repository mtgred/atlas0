(ns atlas.core-test
  (:require [expectations :refer [expect in]]
            [peridot.core :as p]
            [cheshire.core :as json]
            [atlas.db :as db]
            [atlas.handler :as h]))

(let [counter (atom 0)]
  (defn test-app
    []
    (->> (swap! counter inc) (str "datomic:mem://") db/init h/handler)))

(expect string?
        (-> (p/session (test-app))
            (p/request "/")
            :response :body slurp))

(expect (-> (p/session (test-app))
            (p/request "/")
            :response :body slurp)
        (-> (p/session (test-app))
            (p/request "/does-not-exist")
            :response :body slurp))

(expect (-> (p/session (test-app))
            (p/request "/")
            :response :body slurp)
        (-> (p/session (test-app))
            (p/request "/does-not-exist/and/has/multiple/segments")
            :response :body slurp))

(expect "dummy\n"
        (-> (p/session (test-app))
            (p/request "/file-that-exists-for-tests")
            :response :body slurp))

(expect {:body "mtgred@gmail.com"
         :status 200}
        (in (-> (p/session (test-app))
                (p/request "/api/user/mtgred"
                           :request-method :post
                           :body "password")
                :response)))

(expect {:body (json/generate-string [:error "Unauthorized."])
         :status 401}
        (in (-> (p/session (test-app))
                (p/request "/api/user/mtgred"
                           :request-method :post
                           :body "wrong password")
                :response)))

(expect "{}"
        (-> (p/session (test-app))
            (p/request "/session")
            :response :body))

(expect {:body (json/generate-string [:error "Unauthorized."])
         :status 401}
        (in (-> (p/session (test-app))
                (p/request "/login"
                           :request-method :post
                           :content-type "application/json; charset=utf-8"
                           :body (json/generate-string {:username "mtgred"
                                                        :password "wrong password"}))
                :response)))

(expect {:body (json/generate-string [:error "Unauthorized."])
         :status 401}
        (in (-> (p/session (test-app))
                (p/request "/login"
                           :request-method :post
                           :content-type "application/json; charset=utf-8"
                           :body (json/generate-string {:garbage true}))
                :response)))

(expect {:body (json/generate-string [:success])
         :status 200}
        (in (-> (p/session (test-app))
                (p/request "/login"
                           :request-method :post
                           :content-type "application/json; charset=utf-8"
                           :body (json/generate-string {:username "mtgred"
                                                        :password "password"}))
                :response)))

(expect {:status 200
         :body (json/generate-string {:identity "mtgred"})}
        (in (-> (p/session (test-app))
                (p/request "/login"
                           :request-method :post
                           :content-type "application/json; charset=utf-8"
                           :body (json/generate-string {:username "mtgred"
                                                        :password "password"}))
                (p/request "/session")
                :response)))

(expect {:status 200
         :body (json/generate-string {})}
        (in (-> (p/session (test-app))
                (p/request "/login"
                           :request-method :post
                           :content-type "application/json; charset=utf-8"
                           :body (json/generate-string {:username "mtgred"
                                                        :password "password"}))
                (p/request "/logout")
                (p/request "/session")
                :response)))

(expect {:status 403
         :body (json/generate-string [:error "Already logged in."])}
        (in (-> (p/session (test-app))
                (p/request "/login"
                           :request-method :post
                           :content-type "application/json; charset=utf-8"
                           :body (json/generate-string {:username "mtgred"
                                                        :password "password"}))
                (p/request "/register"
                           :request-method :post
                           :content-type "application/json; charset=utf-8"
                           :body (json/generate-string {:username "new-user"
                                                        :password "monkey"}))
                :response)))

(expect {:status 200
         :body (json/generate-string {:user/username "new-user"})}
        (in (-> (p/session (test-app))
                (p/request "/register"
                           :request-method :post
                           :content-type "application/json; charset=utf-8"
                           :body (json/generate-string {:username "new-user"
                                                        :password "monkey"}))
                :response)))

(expect {:status 200
         :body (json/generate-string {:identity "new-user"})}
        (in (-> (p/session (test-app))
                (p/request "/register"
                           :request-method :post
                           :content-type "application/json; charset=utf-8"
                           :body (json/generate-string {:username "new-user"
                                                        :password "monkey"}))
                (p/request "/session")
                :response)))

(expect {:status 401
         :body (json/generate-string [:error "Unauthorized."])}
        (in (-> (p/session (test-app))
                (p/request "/register"
                           :request-method :post
                           :content-type "application/json; charset=utf-8"
                           :body (json/generate-string {:username "new-user"
                                                        :password "monkey"}))
                (p/request "/logout")
                (p/request "/login"
                           :request-method :post
                           :content-type "application/json; charset=utf-8"
                           :body (json/generate-string {:username "new-user"
                                                        :password "don't remember"}))
                :response)))

(expect {:status 200
         :body (json/generate-string {})}
        (in (-> (p/session (test-app))
                (p/request "/register"
                           :request-method :post
                           :content-type "application/json; charset=utf-8"
                           :body (json/generate-string {:username "new-user"
                                                        :password "monkey"}))
                (p/request "/logout")
                (p/request "/session")
                :response)))

(expect {:status 200
         :body (json/generate-string {})}
        (in (-> (p/session (test-app))
                (p/request "/register"
                           :request-method :post
                           :content-type "application/json; charset=utf-8"
                           :body (json/generate-string {:username "new-user"
                                                        :password "monkey"}))
                (p/request "/logout")
                (p/request "/login"
                           :request-method :post
                           :content-type "application/json; charset=utf-8"
                           :body (json/generate-string {:username "new-user"
                                                        :password "don't remember"}))
                (p/request "/session")
                :response)))

(expect {:status 200
         :body (json/generate-string [:success])}
        (in (-> (p/session (test-app))
                (p/request "/register"
                           :request-method :post
                           :content-type "application/json; charset=utf-8"
                           :body (json/generate-string {:username "new-user"
                                                        :password "monkey"}))
                (p/request "/logout")
                (p/request "/login"
                           :request-method :post
                           :content-type "application/json; charset=utf-8"
                           :body (json/generate-string {:username "new-user"
                                                        :password "monkey"}))
                :response)))

(expect {:status 200
         :body (json/generate-string {:identity "new-user"})}
        (in (-> (p/session (test-app))
                (p/request "/register"
                           :request-method :post
                           :content-type "application/json; charset=utf-8"
                           :body (json/generate-string {:username "new-user"
                                                        :password "monkey"}))
                (p/request "/logout")
                (p/request "/login"
                           :request-method :post
                           :content-type "application/json; charset=utf-8"
                           :body (json/generate-string {:username "new-user"
                                                        :password "monkey"}))
                (p/request "/session")
                :response)))

(expect {:status 409
         :body (json/generate-string [:error "Username already taken."])}
        (in (-> (p/session (test-app))
                (p/request "/register"
                           :request-method :post
                           :content-type "application/json; charset=utf-8"
                           :body (json/generate-string {:username "mtgred"
                                                        :password "password"}))
                :response)))
