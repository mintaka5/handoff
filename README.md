# handoff

#### *anonymous identity manager.*

basically manages key documents (a protocol set forth in this software, mentioned below). these are used to manage secure 
messaging, identity, and signing. the goal with this software is to eventually allow any user to participate 
anonymously in any given environment.

#### what can it do?

````shell
bye | exit | quit                 : close application.
cur | current                     : what is the current document being used?
gen | generate <string>?          : generate a new key document. add text after command, to include message.
hash <string>                     : provide a text string, get a sha256 hash of it.
help                              : list all available commands.
ls | list | keys                  : get a list of all your key documents.
sign <random message>             : sign any given message with currently active key document
use | select | pick <num>         : set the default/current document to be used for things like signing or encrypting.
verify <orig msg> <sig> <pub key> : verify a signed message
view | peek | show | deets <num>  : provides some more details about the document.
````

#### json document policy

```json
{
  "tag": "[string] a randomly generated 4 byte string",
  "timestamp": "[long] time of creation",
  "message": "[string] user-defined text message",
  "hash": "[string] a sha256 hash of entire key document",
  "signature": "[string] base64-encoded ec signature of the whole document", 
  "signing": {
    "priv": "[string] base64-encoded ec private signing key",
    "pub": "[string] base64-encoded ec public key"
  },
  "encrypting": {
    "priv": "[string] base64-encoded rsa private signing key",
    "pub": "[string] base64-encoded rsa public signing key"
  }
}
```

---

![last-commit](https://img.shields.io/github/last-commit/white5moke/handoff?style=for-the-badge)
![license: bsd-3-clause](https://img.shields.io/github/license/white5moke/handoff?style=for-the-badge) 
![repo-size](https://img.shields.io/github/repo-size/white5moke/handoff?style=for-the-badge)
