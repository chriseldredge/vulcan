# Configuration #

|Field|Description|
|:----|:----------|
|Executable|The path to the hg command if it is not in the system PATH or if you want to use a different version than the default.|
|Enable Purge Extension|This setting controls how Vulcan will clean a working copy.  When the setting is enabled, Vulcan will use the [Purge Extension](http://mercurial.selenic.com/wiki/PurgeExtension) to remove unversioned and ignored files from the working copy.  If this setting is disabled, Vulcan will completely remove the working copy and create a new one.  For larger working copies, the purge extension will generally be much faster.|

# See Also #

MercurialProjectConfig