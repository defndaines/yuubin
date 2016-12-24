(defproject yuubin "0.1.0-SNAPSHOT"
  :description "Application integrating with Mailgun API"
  :url "https://github.com/defndaines/yuubin"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "3.4.1"]
                 [ring/ring-core "1.5.0"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [org.clojure/data.json "0.2.6"]]
  :plugins [[lein-ring "0.10.0"]]
  :ring {:handler yuubin.core/handler}
  :main ^:skip-aot yuubin.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
