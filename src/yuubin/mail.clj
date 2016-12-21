(ns yuubin.mail
  (:require [clj-http.client :as client]))

;; Generate the HTTP address from the mailbox.
(defn- addr-of [mailbox]
  (str "https://api.mailgun.net/v3/" mailbox "/messages"))

;; Identify as coming from the postmaster unless otherwise provided.
(defn- from [mailbox]
  {"from" (str "Mailgun Sandbox <postmaster@" mailbox ">")})

;; TODO This version requires JSON to include necessary fields (one of "text" or "html")
;;   Also, "to" recipient must already be a verified user through Mailgun.
(defn post-message
  "POST the JSON message to the Mailgun mailbox using the provided key."
  [mailbox key json-msg]
  (client/post
    (addr-of mailbox)
    {:basic-auth ["api" key]
     :form-params (merge (from mailbox) json-msg)}))
