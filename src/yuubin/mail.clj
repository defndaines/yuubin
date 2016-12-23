(ns yuubin.mail
  (:require [clj-http.client :as client]))

;; Generate the HTTP address from the mailbox.
(defn- addr-of [mailbox]
  (str "https://api.mailgun.net/v3/" mailbox "/messages"))

;; Identify as coming from the postmaster unless otherwise provided.
(defn- from [mailbox]
  {"from" (str "Mailgun Sandbox <postmaster@" mailbox ">")})

;; Convert JSON to format required by Mailgun.
;; TODO When there's a body, only lets selected fields through.
;;      Maybe restrict all after testing.
(defn format-for-mailgun [json]
  (let [{body "body"} json]
    (cond
      body (assoc
             (select-keys json ["to" "from" "subject"])
             "html" body)
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
