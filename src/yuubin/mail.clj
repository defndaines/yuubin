(ns yuubin.mail
  (:require [clj-http.client :as client]
            [clojure.java.io :as io]))

;; Generate the HTTP address from the mailbox.
(defn- addr-of [mailbox]
  (str "https://api.mailgun.net/v3/" mailbox "/messages"))

;; Identify as coming from the postmaster unless otherwise provided.
(defn- from [mailbox]
  {"from" (str "Mailgun Sandbox <postmaster@" mailbox ">")})

;; The valid keys currently supported (not all of Mailgun's keys)
(def valid-keys
  ["from" "to" "cc" "bcc" "subject" "text" "html"])

;; Pre-load the known templates.
(def templates
  (into #{} (.list (-> "templates" io/resource io/file))))

(defn load-template [name]
  (-> (str "templates/" name) io/resource slurp))

;; Convert JSON to format required by Mailgun.
;; TODO When there's a body, only lets selected fields through.
;;      Maybe restrict all after testing.
(defn format-for-mailgun [json]
  (let [{body "body" template "template"} json]
    (cond
      body (assoc
             (select-keys json valid-keys)
             "html" body)
      (and template (contains? templates template))
        (assoc
          (select-keys json valid-keys)
          "html" (load-template template))
      :else json)))

;; TODO This version requires JSON to include necessary fields (one of "text" or "html")
;;   Also, "to" recipient must already be a verified user through Mailgun.
(defn post-message
  "POST the JSON message to the Mailgun mailbox using the provided key."
  [mailbox key json-msg]
  (client/post
    (addr-of mailbox)
    {:basic-auth ["api" key]
     :form-params (merge (from mailbox) (format-for-mailgun json-msg))}))
