(ns atlas.db
  (:require [datomic.api :as d]
            [clojure.pprint :refer [pprint]]))

(def uri "datomic:mem://atlas")

(d/create-database uri)

(def conn (d/connect uri))

(def schema (read-string (slurp "resources/schema.edn")))

