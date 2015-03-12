# Find your Primary Domain Controller #

  * In `My Network Places`, open `Search Active Directory`.
  * Change the `Find:` value to `Computers`.
  * Click `Browse...`
  * Select `Domain Controllers` under your domain.
  * Click `Ok`.
  * Click `Find Now`.

The list is populated with one or more Domain Controllers.  If you are
unsure which one to use, ask your network support team for help.

# Create/obtain service account #

Active Directory does not allow anonymous bind to LDAP.  Therefore,
Tomcat will use an account and password to connect to LDAP to search
before authenticating users.  In many cases, you may be able to use the
existing `ADUser` account.  Consult your network support team for help
in getting the password for this account.

# Figure out your schema #

  * Back in `Search Active Directory`, switch `Find:` back to `Users, Contacts and Groups`.
  * Click the `View` menu and select `Choose columns...`
  * Select `X500 Distinguished Name` and click `Ok`.
  * Type your own name into the `Name` field and click `Find Now`.
  * The distinguished name that appears should give you an idea of what value to use for user search.

# Configure Tomcat security realm #

In your Tomcat home, open `conf/server.xml`.  Find the `UserDatabaseRealm` 

&lt;Realm&gt;

 node and comment it out.

Enter the following node (replacing template values with your own):
```
<Realm  className="org.apache.catalina.realm.JNDIRealm"
        connectionName="cn=ADUser,ou=Service Accounts,dc=example,dc=com"
        connectionPassword="password"
        connectionURL="ldap://dc1.example.com:389"
        userBase="ou=users,dc=example,dc=com"
        userSubtree="true"
        userSearch="(sAMAccountName={0})"
        roleBase="ou=groups,dc=example,dc=com"
        roleName="cn"
        roleSearch="(member={0})"
        roleSubtree="true"/>
```

Save `server.xml`.

The key here is that `sAMAccountName` is the attribute Active Directory uses for a login name.  Therefore, we use this attribute to search for accounts.

Once the account/password are authenticated, the role attributes are used to determine in which groups the account has membership.  These groups are treated as J2EE roles.

# Configure Vulcan web.xml #

Tomcat will use Active Directory groups to role based authorization.
Therefore, `WEB-INF/web.xml` in Vulcan must declare roles that correspond
to actual groups in Active Directory.  Either create new groups in
Active Directory and add users who should be authorized, or change
`web.xml` to use existing groups.

# Restart Tomcat and test #

Use account(s) to verify that you must login to access resources you wish to protect, and that only users in the correct groups are allowed to access them.