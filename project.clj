(defproject snap "1.0.0"
  :description "State change diff engine"

  :url "https://github.com/swlkr/snap"

  :license {:name "The MIT License"
            :url "https://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.89" :scope "provided"]
                 [proto-repl "0.3.1"]]

  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-doo "0.1.7"]]

  :min-lein-version "2.6.1"

  :source-paths ["src"]

  :test-paths ["test"]

  :clean-targets ^{:protect false} [:target-path :compile-path "resources/public/js"]
  :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
  :uberjar {:name "quiescent.jar"}

  :repl-options {:init-ns snap.core}

  :cljsbuild {:builds
               [{:id "min"
                 :source-paths ["src"]
                 :jar true
                 :compiler {:main snap.core}
                           :output-to "resources/public/js/compiled/snap.js"
                           :output-dir "target"
                           :source-map-timestamp true
                           :optimizations :advanced
                           :pretty-print false}

                {:id "test"
                 :source-paths ["src" "test"]
                 :compiler {:output-to "resources/public/js/compiled/testable.js"}
                           :main snap.test-runner
                           :optimizations :none
                           :target :nodejs}]}

  :doo {:build "test"}

  :scm {:name "git"
         :url "https://github.com/swlkr/snap"})
