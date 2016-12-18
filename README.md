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

## License

Copyright © 2016 Michael S. Daines

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
