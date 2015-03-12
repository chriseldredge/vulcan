# Introduction #

You can notify committers and other users when a build breaks using the Jabber plugin.  This component builds on the XMPP standard, but using a gateway you can also send Instant Messages (IMs) to legacy clients such as AOL Instant Messenger (AIM).

# Configuration #

| **Field** | **Required** | **Description** |
|:----------|:-------------|:----------------|
| Jabber Server | Yes          | Host name or IP address of Jabber server. |
| Port      | Yes          | The TCP port to use.  The standard port is 5222. |
| Service Name | No           | The XMPP service to connect to. |
| Login     | Yes          | The login of the user who will send IMs.  |
| Password  | No           | The password for the login. |
| Vulcan URL | Yes          | The URL where Vulcan is hosted, for the purpose of sending links in IMs. |
| Screen Name Lookup Method | Yes          | The method to use to find screen names based on commit authors.  See Lookup Methods below for more info. |
| Screen Name Lookup Config | Yes          | Click `Configure` to specify additional settings. |
| Projects to monitor | Yes          | Specify which projects you want the plugin to send IMs about. |
| Projects  | No           | If `Projects to monitor` is set to `Specify`, choose them here. |
| Build events to monitor | Yes          | Which type of build messages should trigger notifications. |
| Error Regex | No           | If specified, build errors must match this regex in order to trigger a notification. |
| Warning Regex | No           | If specified, build warnings must match this regex in order to trigger a notification. |
| Build Master Screen Names | No           | List of screen names that should always be sent IMs regardless of whether or not those users committed changes.  You might put the build master's screen name here. |

# Lookup Methods #

| **Method** | **Description** |
|:-----------|:----------------|
| [Regex](JabberRegexScreenNameMapperConfig.md) | Use commit authors as screen names with optional replacements. |
| [Dictionary](JabberDictionaryScreenNameMapperConfig.md) | Specify mapping of users within Vulcan configuration. |
| [Jdbc](JabberJdbcScreenNameMapperConfig.md) | Lookup screen names from a JDBC database. |

# Example: Google Talk #

The following configuration could be used for projects hosted by Google Code:

| Jabber Server | talk.google.com |
|:--------------|:----------------|
| Port          | 5222            |
| Service Name  | gmail.com       |
| Username      | (your GMail account) |
| Screen Name Lookup Method | Regex           |

See JabberRegexScreenNameMapperConfig for GMail example configuration.