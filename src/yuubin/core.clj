(ns yuubin.core
  (:gen-class)
  (:require
    [clojure.java.io :as io]
    [ring.util.request :as ring-util]
    [ring.adapter.jetty :as ring-jetty]
    [clojure.data.json :as json]
    [yuubin.mail :as mail]))

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

;; For `lein ring server`
(def handler
  (let [template-dir (.getFile (-> "templates" clojure.java.io/resource))]
    (ring-handler (fn [json] (mail/format-for-mailgun json template-dir)))))

(defn -main
  [& args]
  (if-let [[conf-file & _] args]
    (let [config (verify-config (read-conf conf-file))
          mail-handler (fn [json] (mail/format-for-mailgun json (:template-dir config)))]
      (ring-jetty/run-jetty (ring-handler mail-handler) {:port (:port config)}))
      (println "Must pass EDN configuration file to run.\n  Usage: yuubin <config.edn>")))
