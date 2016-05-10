(defproject atlas "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [ring "1.4.0"]
                 [bidi "2.0.9"]
                 [reagent "0.5.1"]
                 [re-frame "0.7.0"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :plugins [[lein-cljsbuild "1.1.3"]]

  :clean-targets ^{:protect false} ["resources/public/cljs" "target"]

  :figwheel {:css-dirs ["resources/public/css"]
             :ring-handler atlas.handler/handler
             :server-port 2442}

  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.2.1"]
                                  [figwheel-sidecar "0.5.2"]]
                   :plugins [[lein-figwheel "0.5.3"]]
                   :source-paths ["src/cljs"]}}

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

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
