(ns atlas.db-test
  (:require [expectations :refer [expect]]
            [datomic.api :as d]
            [atlas.db :as db]))

(def conn (db/init (str "datomic:mem://" (d/squuid))))

(expect [{:user/username "mtgred"
          :user/email "mtgred@gmail.com"}
         {:user/username "gaverhae"
          :user/email "gary.verhaegen@gmail.com"}]
        (db/find (d/db conn) :user/username [:user/username :user/email]))

(expect {:user/username "mtgred"
         :user/name "Minh Tran"
         :user/email "mtgred@gmail.com"
         :user/password "$s0$e0801$37gTCNd70g3vEumfrn0lVg==$mwAVN6DB12X1ZzXqg+LvpCR3nGAgfPJSWEwlk8cRzXA="
         :user/registration-date #inst "2016-05-10T00:00:00.000-00:00"
         :user/last-connection #inst "2016-05-15T19:28:04.000-00:00"}
        (-> (db/find (d/db conn) :user/username '[*])
            first
            (dissoc :db/id)))
