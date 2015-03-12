# Configuration #

|Remote Repository URL|URL to clone from (can be http, ssh or local file:/// based URL)|
|:--------------------|:---------------------------------------------------------------|
| Working Copy Branch |Branch, tag or revision to build working copy from.  `default` will be used if it is left blank.|
|SSH Command          |Allows you to override default ssh command or pass custom arguments to ssh.  This gets passed through as the `--ssh` parameter to hg.|
|Remote Command       |Allows you to override default remote command when cloning over ssh.  This gets passed through as the `--remote` parameter to hg.|
|Use Pull Protocol    |When cloning a repository on the same filesystem, this parameter will prevent optimizations in Mercurial that link data instead of completely copying it.  Same as the `--pull` parameter to hg.|
|Uncompressed Transfer|Gets passed through as `--uncompressed` to hg when cloning a repository.|

# SSH Known Hosts #

When cloning repositories over SSH, if the system account that Vulcan is running as does not trust the remote SSH server, an error will occur.  There are two ways to avoid the issue.  First, manually putting the remote hosts public key into the `known_hosts` file will make ssh trust the server.  If this is not possible, ssh host key checking can be disabled by using this `{SSH Command`: `ssh -o StrictHostKeyChecking=no`.

# Authentication #

Mercurial does not provide a means to provide a password on the command line because doing so is insecure.  If Vulcan needs a password to access the remote repository, the [Keyring Extension](http://mercurial.selenic.com/wiki/KeyringExtension) is recommended.

Configure the Keyring Extension in the `hgrc` for the account that Vulcan runs as, then manually connect to the remote repository.  After successfully connecting, the password will be stored and Vulcan should be able to connect without needing it.

# See Also #

MercurialConfig