# Yūbin

Small application to integrate with [Mailgun](http://www.mailgun.com/).

## Installation

Download from https://github.com/defndaines/yuubin/releases

## Usage

To run as a standalone service:

    $ java -jar yuubin-0.1.0-standalone.jar config.edn

In order to run the service from the standalone JAR, an EDN configuration file must
be passed with the following values provided (if "port" is not provided, will
default to 3000):
```
{:port 3030
 :mailbox "mailgun-api-mailbox"
 :api-key "mailgun-api-key"
 :template-dir "/etc/yuubin/templates"}
```

## API

The service expects POST requests passing JSON data about the mail message to
send. At this time, the service does not authenticate the incoming request, so
the service will send out valid messages using the configured mailbox and API
key. If a "from" field is not provided, it uses the "postmaster"
account of the configured "mailbox".

## Examples

To send a simple message providing the message body:
```
curl -X POST -H "Content-Type: application/json" localhost:3000 -d '{"to": "mikan@e-mail.com", "subject": "Welcome to Yūbin", "body": "<h1>Hey Mikan!</h1><p>We are really happy to have you here at Yūbin. If you have any questions, please contact us."}'
```

To send a request using a template:
```
curl -X POST -H "Content-Type: application/json" localhost:3000 -d '{"to": "mikan@e-mail.com", "subject": "Welcome to Yūbin", "template": "welcome.html", "t:name": "Mikan"}'
```

## Templates

The service can send e-mails using a template body. The template must be
predefined under [resources/templates/](resources/templates/). Use the "template" key with the name of
the template file as the value to use this feature.

This example sends the Password Reset template to the user:
```
{ "to": "user@mail.com"
, "subject": "Password Reset"
, "template": "password-reset.html"
}
```

Templates can contain attributes to replace. Template attributes must be
prefixed with "t:". In the HTML document, they must be surrounded with
double-percent signs, like "%%t:attr%%". If a template includes attributes
but the request does not define them, they will be stripped from the outgoing
message to avoid leaving the template identifiers in the message.
This example replaces the "name" attribute in the "Welcome" template:
```
{ "to": "user@mail.com"
, "subject": "Welcome to Yūbin"
, "template": "welcome.html"
, "t:name": "Mikan"
}
```

## Development

Use of this project requires an API key from Mailgun. See their
[Sign Up](https://mailgun.com/signup) page to get a key.
The [.gitignore](.gitignore) file is set to
ignore the "mailgun-api.key" file, so you can store a personal key there for
development to avoid accidentally committing it. Additionally, if you have
[git-secrets](https://github.com/awslabs/git-secrets) installed, run a command
like the following to prevent you from committing a file containing the key:
```
git secrets --add '<your-key-here>'
```

### Send Mail from the REPL

You can test that everything is wired up correctly to send messages via Mailgun
with the following:
```
(require '[yuubin.mail :as mail])

(def my-key (clojure.string/trim-newline (slurp "resources/mailgun-api.key")))
(def my-mailbox (clojure.string/trim-newline (slurp "resources/mailgun-api.mailbox")))
(def template-dir "resources/templates")
(def my-message {"to" "your.name@e-mail.com"
  "subject" "Hello World"
  "body" "<html><body><h1>Hello World!</h1></body></html>"})

(mail/post-message my-mailbox my-key my-message)
```
If this returns a 400, check the full exception, which can do a good job of
pointing out what is missing from the call.

### Ring Server

For development, you can run the service using:
```
lein ring server 3000
```

This will run the server locally on port 3000. It will also pick up many code
changes dynamically so that the service can be tweaked and tested.

## License

Copyright © 2016 Michael S. Daines

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
