# Yūbin

Small application to integrate with [Mailgun](http://www.mailgun.com/).

The service can take individual requests via a POST endpoint, or can monitor a
Kafka topic for incoming requests.

## Installation

Download from https://github.com/defndaines/yuubin/releases

## Usage

To run as a standalone service:

    $ java -jar yuubin-0.1.0-standalone.jar config.edn

### Configuration

In order to run the service from the standalone JAR, an EDN configuration file must
be passed with the following values provided (if "port" is not provided, will
default to 3000):
```
{:port 3030
 :mailbox "mailgun-api-mailbox"
 :api-key "mailgun-api-key"
 :template-dir "/etc/yuubin/templates"
 :bootstrap-servers "localhost:9092"
 :incoming-topic "yuubin.horyuu.json"
 :receipt-topic "yuubin.ryoushuu.json"}
```

## API

The service expects POST requests passing JSON data about the mail message to
send. At this time, the service does not authenticate the incoming request, so
the service will send out valid messages using the configured mailbox and API
key. If a "from" field is not provided, it uses the "postmaster"
account of the configured "mailbox".

### Examples

The examples below assume you have configured the service to listen on the
default port (`{ :port 3000 }`).

To send a simple message providing the message body:
```
curl -X POST -H "Content-Type: application/json" localhost:3000 -d '{"to": "mikan@e-mail.com", "subject": "Welcome to Yūbin", "body": "<h1>Hey Mikan!</h1><p>We are really happy to have you here at Yūbin. If you have any questions, please contact us."}'
```

To send a request using a template:
```
curl -X POST -H "Content-Type: application/json" localhost:3000 -d '{"to": "mikan@e-mail.com", "subject": "Welcome to Yūbin", "template": "welcome.html", "t:name": "Mikan"}'
```

## Templates

The service can send e-mails using a template body. Some sample templates are
defined under [resources/templates/](resources/templates/). Use the "template" key with the name of
the template file as the value to use this feature.

To use templates, provide a template directory in the configuration file
(`{ :template-dir "/etc/yuubin/templates" }`).

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

## Queuing Mail

The service can also read from a Kafka topic which acts as a queue for messages.
The incoming topic expects JSON messages in the same format as defined in the
API above.

To use the queuing feature, be sure to set the following configuration
values:
```
{:bootstrap-servers "localhost:9092"
 :incoming-topic "yuubin.horyuu.json"
 :receipt-topic "yuubin.ryoushuu.json"}
```
The `incoming-topic` will be monitored for incoming JSON messages.
The `receipt-topic` will contain the response from the
Mailgun request for each processed message.

The service hard-codes the Kafka group ID and syncs messages once they've been
handled. This means that if the service crashes and is restarted, it should not
resend messages which have already been sent.

At this time, the service does not provide an explicit way to track queued
requests and receipts. It could be changed to require incoming messages to
provide an ID and then include that ID in the receipt.

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

### Setting up Kafka

The service can read from and write to Kafka topics to track message requests.
Follow the instructions below to install a local instance of
[Kafka](https://kafka.apache.org/). If using Docker or a preexisting Kafka
installation, you can skip most of this, provided you configure topics.

Download the
[Kafka 0.10.1.0](https://www.apache.org/dyn/closer.cgi?path=/kafka/0.10.1.0/kafka_2.11-0.10.1.0.tgz)
tar ball and extract to a location which makes sense for your environment.

Start zookeeper.
```
cd kafka_2.11-0.10.1.0
bin/zookeeper-server-start.sh config/zookeeper.properties
```
Start the Kafka server. You may need to add the line
`advertised.host.name=localhost` to the `server.properties` file in order to
publish and consume from a local Kafka instance.
```
bin/kafka-server-start.sh config/server.properties
```
Create a couple topics to publish messages to.
```
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic yuubin.horyuu.json
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic yuubin.ryoushuu.json
```
Check that the topics are present.
```
bin/kafka-topics.sh --list --zookeeper localhost:2181
```
Write messages into the sending topic. Any content written or pasted into this
session this creates, per line, will publish into the topic. Use Ctrl-C to exit.
```
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic yuubin.horyuu.json
```
To test from the command line that the messages are in the topic:
```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic yuubin.horyuu.json --from-beginning
```

## License

Copyright © 2016 Michael S. Daines

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
