# Introduction #

Google Code has a configuration setting that will send a request whenever a user commits changes to a project's repository.  See [PostCommitWebHooks](http://code.google.com/p/support/wiki/PostCommitWebHooks) for details on configuring your Google Code project.

Vulcan can trigger builds when Google Code sends data to `/googleCodeCommitHook`.

# Configuring Google Code #

Go to `http://code.google.com/p/YOUR-PROJECT/adminSource` and find the section `Post-Commit web hooks`.

In `Post-Commit URL`, enter an URL like `http://your-vulcan-server/vulcan/googleCodeCommitHook?targets=vulcan-project-name&requestBy=Google+Code+Commit+Hook`

Note: vulcan-project-name is the name of the project to build in Vulcan.  You can build multiple projects by repeating this parameter, e.g. `targets=project1&targets=project2`.

Vulcan will only build the project if it finds changes in the branch that the project is configured to build from.  You can force Vulcan to always build by appending the parameter `buildOnNoUpdates=true`.

Make a note of your project's `Post-Commit Authentication Key`.

# Configuring Vulcan #

Edit `WEB-INF/web.xml`:

Paste your authentication key into the `sharedSecret` init parameter for the `SignedRequestAuthorizationFilter`.