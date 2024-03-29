(defproject atlas "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [com.datomic/datomic-free "0.9.5359"]
                 [io.rkn/conformity "0.4.0"]
                 [clojurewerkz/scrypt "1.2.0"]
                 [environ "1.0.3"]
                 [ring/ring-json "0.4.0"]
                 [http-kit "2.1.18"]
                 [ring "1.4.0"]
                 [buddy/buddy-auth "1.0.0"]
                 [bidi "2.0.9"]
                 [kibu/pushy "0.3.6"]
                 [reagent "0.5.1"]
                 [re-frame "0.7.0"]
                 [cljs-http "0.1.40"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-figwheel "0.5.3"]]

  :clean-targets ^{:protect false} ["resources/public/cljs" "target"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.2.1"]
                                  [figwheel-sidecar "0.5.3"]
                                  [peridot "0.4.3"]
                                  [expectations "2.1.8"]]
                   :plugins [[lein-autoexpect "1.9.0"]]
                   :resource-paths ["dev-resources"]
                   :source-paths ["src/clj" "src/cljs"]}}

  :repl-options {:init-ns atlas.core}

  :main atlas.core

  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["src/cljs"]
                        :figwheel     {:on-jsload "atlas.core/mount-root"}
                        :compiler     {:main                 atlas.core
                                       :output-to            "resources/public/js/atlas.js"
                                       :output-dir           "resources/public/cljs"
                                       :asset-path           "/cljs"
                                       :source-map-timestamp true}}

                       {:id           "prod"
                        :source-paths ["src/cljs"]
                        :compiler     {:main                 atlas.core
                                       :output-to            "resources/public/js/atlas.js"
                                       :optimizations        :advanced
                                       :closure-defines      {goog.DEBUG false}
                                       :pretty-print         false}}]})
