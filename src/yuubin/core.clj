(ns yuubin.core
  (:gen-class)
  (:require [clojure.data.json :as json]
            [yuubin.mail :as mail]))

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (-> request
             ring.util.request/body-string
             json/read-str
             mail/format-for-mailgun
             json/write-str)})

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "郵便局"))
