# Introduction #

Vulcan uses standard JEE declaritive security.  It is recommended that you enable security, not necessarily for the normal reasons, but because it allows Vulcan to keep an audit trail of who is doing what.  For example, when users access the Manual Build feature, you may want to keep track of who is performing these functions.  Also, depending on who will have access to your system, it is probably a good idea to limit who has access to the Setup section of Vulcan.

JEE security configuration in general is outside the scope of this documentation.  If you are using Tomcat, you can read about how to configure security in their documentation:
http://tomcat.apache.org/tomcat-5.5-doc/realm-howto.html

**Note:** _If users at your site are authenticated with a Windows Domain with Active Directory, it is possible to use the JNDIRealm in Tomcat to authenticate users against the ldap server running on your Domain Controller.  See TomcatSecurityActiveDirectory._

Vulcan resources are organized in a way that should make it easy to divide access across different groups.  Using the `<security-constraint>` declaration in `WEB-INF/web.xml`, you can use the following url-patterns:

| **url-pattern** | **description** |
|:----------------|:----------------|
| `/admin/*`      | Limit acces to the Setup section.  This will require a user to be authorized before changing Vulcan configuration. |
| `/buildmanagement/*` | Limit access to the Manual Build capability.  This will require a user to be authorized before forcing a build,  building from an alternate tag, or aborting a build in progress. |
| `/site/*`       | Limit access to working copies.  If your working copy contains sensitive information, you can limit access so only authorized users can browse the working copy through Vulcan. |
| `/*`            | Limit access to all parts of Vulcan.  This will require a user to log in before seeing the dashboard, build details and build history.  This level of security is not necessary in most situations. |

After choosing which section you want to secure, you need to choose a security-role which a user must be granted before accessing that section.  The role name is specific to your authentication realm.  If you are using Active Directory, you may already have Groups in the directory that can be used for this purpose.  For example, you may have a Developers group which you can use to limit access to `/buildmanagement/*`, and you may have a System Administrators or Configuration Management group which you can use to limit access to `/admin/*`.

When you choose your role names, add them to your `<security-constraint>` sections, then also add a `<security-role>` section for each role.

`web.xml` has example security configurations which you can use as a template.  If you want to disable security entirely, simply remove the `<security-constraint>` sections from `web.xml`.