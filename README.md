# Yūbin

Small application to integrate with [Mailgun](http://www.mailgun.com/).

## Installation

Download from https://github.com/defndaines/yuubin/releases

## Usage

FIXME: explanation

    $ java -jar yuubin-0.1.0-standalone.jar [args]

## Options

FIXME: listing of options this app accepts.

## Examples

...


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

### Send Mail from REPL

You can test that everything is wired up correctly to send messages via mailgun
with the following:
```
(require '[yuubin.mail :as mail])

(def my-key (clojure.string/trim-newline (slurp "resources/mailgun-api.key")))
(def my-mailbox (clojure.string/trim-newline (slurp "resources/mailgun-api.mailbox")))
(def my-message {"to" "your.name@e-mail.com"
  "subject" "Hello World"
  "html" "<html><body><h1>Hello World!</h1></body></html>"})

(mail/post-message my-mailbox my-key my-message)
```
If this returns a 400, check the full exception, which can do a good job of
pointing out what is missing from the call.

## License

Copyright © 2016 Michael S. Daines

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
