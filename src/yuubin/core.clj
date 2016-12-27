(ns yuubin.core
  (:gen-class)
  (:require
    [clojure.java.io :as io]
    [ring.util.request :as ring-util]
    [ring.adapter.jetty :as ring-jetty]
    [clojure.data.json :as json]
    [yuubin.mail :as mail]
    [yuubin.kafka :as kafka]))

;; Read in .edn file for configuration.
(defn- read-conf [conf-file]
  (with-open [reader (-> conf-file
                         io/reader
                         java.io.PushbackReader.)]
    (clojure.edn/read reader)))

;; Ensure required fields are set, and default port to 3000 if not provided.
(defn- verify-config [conf]
  {:pre [(contains? conf :mailbox)
         (contains? conf :api-key)
         (contains? conf :template-dir)]}
  (merge {:port 3000} conf))

(defn- ring-handler [mail-handler]
  (fn [request]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (-> request
               ring-util/body-string
               json/read-str
               mail-handler
               json/write-str)}))

;; For `lein ring server`. Doesn't contact mail server, only echoes request back.
(def handler
  (let [template-dir (.getFile (-> "templates" clojure.java.io/resource))]
    (ring-handler (fn [json] (mail/format-for-mailgun json template-dir)))))

;; Runs a daemon thread to processes a queue.
(defn- watch-queue
  [queue-fn]
  (doto (Thread. queue-fn)
    (.setDaemon true)
    (.start)))

;; TODO Require :bootstrap-servers and :incoming-topic to be in config.
(defn- monitor-queue [config mail-handler]
  (let [consumer-config (merge kafka/default-consumer-config {"bootstrap.servers" (:bootstrap-servers config)})
        consumer (kafka/consumer consumer-config (:incoming-topic config))]
    (watch-queue
      (kafka/read-topic
        consumer
        (fn [record]
          (-> record
              json/read-str
              mail-handler))))))

(defn -main
  [& args]
  (if-let [[conf-file & _] args]
    (let [config (verify-config (read-conf conf-file))
          mail-handler (partial mail/post-message (:mailbox config) (:api-key config) (:template-dir config))]
      (monitor-queue config mail-handler)
      (ring-jetty/run-jetty (ring-handler mail-handler) {:port (:port config)}))
    (println "Must pass EDN configuration file to run.\n  Usage: yuubin <config.edn>")))
