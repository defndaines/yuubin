(defproject yuubin "0.1.0-SNAPSHOT"
  :description "Application integrating with Mailgun API"
  :url "https://github.com/defndaines/yuubin"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :main ^:skip-aot yuubin.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
