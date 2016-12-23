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

;; Load a template, replacing attributes if found.
(defn load-template [name attrs]
  (-> (str "templates/" name) io/resource slurp))

;; Convert JSON to format required by Mailgun.
(defn format-for-mailgun [json]
  (let [{body "body" template "template"} json
        message (select-keys json valid-keys)]
    (cond
      body
        (assoc message "html" body)
      (and template (contains? templates template))
        (assoc message "html" (load-template template json))
      :else message)))

(defn post-message
  "POST the JSON message to the Mailgun mailbox using the provided key.
  Note that the \"to\" recipient must already be a verified user through Mailgun
  for sending to succeed."
  [mailbox key json-msg]
  (client/post
    (addr-of mailbox)
    {:basic-auth ["api" key]
     :form-params (merge (from mailbox) (format-for-mailgun json-msg))}))
