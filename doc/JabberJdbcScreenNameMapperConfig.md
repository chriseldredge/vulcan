# Introduction #

Part of JabberPluginConfig.

The JDBC mapper is the most complicated to configure, but the simplest to maintain if you happen to have a JDBC compatible data source that contains login and screen name information for your organization or project.

# Data Source #

This mapper requires a JNDI DataSource to be configured in your application container and exposed to the Vulcan web application.  Such configuration is outside the scope of this documentation.

Typically when you configure a JNDI DataSource the application container will make it available using a name such as `java:comp/env/jdbc/MyDataSource`.  You may see the name in your settings as `jdbc/MyDataSource` but you should use the full name when configuring Vulcan.

# SQL Query #

Since every database is completely different, the SQL is completely up to you.  The query will be executed once for each commit author that is being resolved.  Your query only needs to select a single column (the screen name).  If your query returns more than one column, make sure the screen name is the first column returned.

## Example ##

```
SELECT replace(screen_name, ' ', '') + '@aim.example.com'
FROM employees WHERE login = ?
```

This example does the following:

  * Remove spaces from result (in case employees have entered a formatted screen name)
  * Append a gateway to the result to work appropriately with an internal Jabber server gateway

# See Also #

  1. JabberPluginConfig
  1. JabberDictionaryScreenNameMapperConfig
  1. JabberRegexScreenNameMapperConfig