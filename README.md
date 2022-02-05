# handoff

#### *anonymous identity manager.*

basically manages key documents (a protocol set forth in this software). these are used to manage secure 
messaging, identity, and signing. the goal with this software is to eventually allow any user to participate 
anonymously in any given environment.

#### what can it do?

````
bye : exit the app.
cur : what is your current key document `cur`
del : deletes all your key documents. be careful! `del`
echo : prints user input. `echo <any text string>`
gen : generate a new key document. message is optional. `gen [<message>]`
help : help information.
list : list all key documents. sorted by most recent first `list`
peek : view the details of a key document `peek <# from `list`>`
sign : sign a message. `sign <any text here>`
use : designate currently used key document. `use <# from `list`>`
vsign : verify a signed message. user will be prompted for public key, original message, and signature. `vsign`.
````

---

![last-commit](https://img.shields.io/github/last-commit/white5moke/handoff?style=for-the-badge)
![license: bsd-3-clause](https://img.shields.io/github/license/white5moke/handoff?style=for-the-badge) 
![repo-size](https://img.shields.io/github/repo-size/white5moke/handoff?style=for-the-badge)
