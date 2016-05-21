(ns atlas.handler
  (:require [bidi.ring :refer [make-handler resources]]
            [ring.util.response :refer [file-response]]))

(defn index-handler [request]
  (file-response "index.html" {:root "resources/public"}))

(defn handler
  [conn]
  (make-handler
   ["" [[#".*" index-handler]
        ["" (resources {:prefix "public"})]]]))
