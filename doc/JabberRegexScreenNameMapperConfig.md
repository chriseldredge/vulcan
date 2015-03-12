# Introduction #

Part of JabberPluginConfig.

This mapper can be used to determine which screen names to send instant messages to when the commit authors on your project are the same or similar to the screen names on your chat service.  For example, a project hosted by Google Code could use this mapper to send messages over Google Talk.

# Examples #

## Simple Identity ##

| Regex | `(.*)` |
|:------|:-------|
| Replacement | `$1`   |

## Remove Domain ##

If you have a Windows Active Directory domain, you may need to remove the domain:

| Regex | `.*\\(.*)` |
|:------|:-----------|
| Replacement | `$1`       |

## Append Gateway ##

Your Jabber server may require you to append a gateway domain to your screen names, such as `chris.eldredge@gmail.com`:

| Regex | `(.*)` |
|:------|:-------|
| Replacement | `$1@gmail.com` |

# See Also #

  1. JabberPluginConfig
  1. JabberDictionaryScreenNameMapperConfig
  1. JabberJdbcScreenNameMapperConfig