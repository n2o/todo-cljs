(defproject todo "0.1.0-SNAPSHOT"
  :description "Implementing a todo-app with om-next"
  :url "https://github.com/n2o/todomvc"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.456"]
                 [org.omcljs/om "1.0.0-alpha47"]]

  :plugins [[lein-figwheel "0.5.9"]
            [lein-cljsbuild "1.1.5" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]
                :figwheel {:on-jsload "todo.core/on-js-reload"
                           :open-urls ["http://localhost:3449/index.html"]}

                :compiler {:main todo.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/todo.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           :preloads [devtools.preload]}}
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/todo.js"
                           :main todo.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {:css-dirs ["resources/public/css"]} ;; watch and update CSS

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.0"]
                                  [figwheel-sidecar "0.5.9"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   ;; need to add dev source path here to get user.clj loaded
                   :source-paths ["src" "dev"]
                   ;; for CIDER
                   ;; :plugins [[cider/cider-nrepl "0.12.0"]]
                   :repl-options {; for nREPL dev you really need to limit output
                                  :init (set! *print-length* 50)
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}

)
