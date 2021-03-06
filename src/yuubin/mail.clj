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

;; Substitute template attributes into message body.
(defn- sub-template-attrs [body attrs]
  (reduce-kv
    (fn [acc k v]
      (if (clojure.string/starts-with? k "t:")
        (let [re-attr (re-pattern (str "%%" k "%%"))]
          (clojure.string/replace acc re-attr v))
        acc))
    body
    attrs))

;; Avoid printing template attributes if they aren't used.
(defn- rm-unused-template-attrs [body]
  (clojure.string/replace body #"%%[^%]*%%" ""))

;; Load a template, replacing attributes if found.
;; Throws java.io.FileNotFoundException if file not found.
(defn load-template [dir name attrs]
  (-> (str dir "/" name)
      io/file
      slurp
      (sub-template-attrs attrs)
      rm-unused-template-attrs))

;; Convert JSON to format required by Mailgun.
(defn format-for-mailgun
  [json template-dir]
  (let [{body "body" template "template"} json
        message (select-keys json valid-keys)]
    (cond
      body
        (assoc message "html" body)
      template
        (assoc message "html" (load-template template-dir template json))
      :else message)))

(defn post-message
  "POST the JSON message to the Mailgun mailbox using the provided key.
  Note that the \"to\" recipient must already be a verified user through Mailgun
  for sending to succeed."
  [mailbox key template-dir json-msg]
  (client/post
    (addr-of mailbox)
    {:basic-auth ["api" key]
     :form-params (merge (from mailbox) (format-for-mailgun json-msg template-dir))}))
