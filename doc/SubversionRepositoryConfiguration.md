# Introduction #

The main configuration section for the Subversion plugin allows you to configure repositories and specify settings which apply to all projects using the plugin.

The plugin relies on [TMate Software's SVNKit](http://www.svnkit.com/) for Subversion integration.  SVNKit is a 100% pure Java implementation and does not require you to install the Subversion command line programs like `svn.exe`.

# Repository Locations #

Click the `Add` button to create new `Repository Locations`, or click `Configure` to edit existing ones.  Defining `Repository Location` settings here allows you to reuse the same repository for many projects, instead of having to specify a redundant URL, username and password for each project.

See SubversionRepositoryProfileConfiguration.

# Tag Folders #

The `Tag Folders` setting allows you to customize the keywords that are used to determine which branch or tag is being used to perform a build.  The defaults follow the [Recommended repository layout](http://svnbook.red-bean.com/nightly/en/svn.tour.importing.html#svn.tour.importing.layout) in the standard [Subversion Book](http://svnbook.red-bean.com/).

Because `trunk` is a special case, you cannot override or remove it.  When none of the values in `Tag Folders` match a repository path, the default value will be `trunk`.

In addition to showing the correct line of development under the `Tag` field in the DashBoard and in ProjectStatus, these settings configure where to search for available tags and branches when performing a [release build](BuildAndRelease.md).

Note that the Subversion plugin correctly handles submodules:

| **Repository Path** | **Computed Tag** |
|:--------------------|:-----------------|
| /branches/foo       | branches/foo     |
| /myProject/branches/foo | branches/foo     |
| /anotherProject/tags/1.0 | tags/1.0         |
| /complexProject/branches/rewrite/submodule-1 | branches/rewrite |
| /no/matching/value  | trunk            |
| /trunk              | trunk            |

You can specify your own `Tag Folders`, replacing or augmenting the default values.  This is useful if you tag your deliveries under `/deliveries/`, `/releases/` or use some other scheme.

# Speeding Up Performance #

The pure java Subversion API is great for simple deployments and most users will probably find it to be adequate.  However, anecdotal evidence indicates that the native Subversion client API performs better than SVNKit.  Users working with very large projects with thousands of files and directories may wish to improve performance by using native bindings.

Using the native JavaHL bindings requires that the standard Subversion package for your operating system be installed.  In addition, the JavaHL bindings themselves must be downloaded.  You can find these packages at the [Subversion](http://subversion.tigris.org/) project homepage.

Shut down the app server hosting Vulcan if it is running.

Once you have installed Subversion, modify your systems PATH environment variable to include the `bin` directory.

Next, navigate to `$VULCAN_HOME/plugins/net.sourceforge.vulcan.subversion` and delete `svnkit-javahl-1.1.4.jar`.  Drop the javahl jar file into this directory.

Finally, if the JavaHL download included a `.dll` or `.so` file, drop it into the bin directory where you installed Subversion.

When you restart your app server, the Vulcan Subversion plugin should start using the native bindings.

Please note that the native bindings use the JNI interface.  If errors occur in binding the native libraries, check the Vulcan log file for hints.  Attempting to restart or upgrade Vulcan without stopping and starting your app server may lead to errors due to a limitation in how JNI loads native libraries.