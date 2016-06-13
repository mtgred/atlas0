(ns atlas.db
  (:require [datomic.api :as d]
            [io.rkn.conformity :as c]))

(defn init
  [uri]
  (let [_created? (d/create-database uri)
        conn (d/connect uri)
        schema-norms (c/read-resource "schema.edn")]
    (c/ensure-conforms conn schema-norms)
    conn))

(defn find [db key fields]
  (d/q '[:find [(pull ?e fields) ...]
         :in $ ?key fields
         :where [?e ?key _]]
       db key fields))
