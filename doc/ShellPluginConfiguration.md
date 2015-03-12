# Introduction #

The global configuration section for the Shell Plugin allows you to specify which environment variables to pass into programs executed by the plugin.

| **Field** | **Required** | **Description** |
|:----------|:-------------|:----------------|
| Build Number Variable | No           | This variable will contain the build number that Vulcan assigns so your build script can reference it for versioning or other purposes. |
| Revision Label Variable | No           | This variable will contain the revision label that corresponds to the revision of the working copy. |
| Revision Number Variable | No           | This variable is similar to `Revision Label Variable` above, but is an integer value.  For example, if you are using Subversion, the above value my be `r123` and this value would be `123`. |
| Revision Tag Name Variable | No           | The tag name that was used to populate the working copy.  This value will be `trunk` in most cases, but could also be a tag or branch value. |
| Environment Variables | No           | Any other environment variables that should be defined for all projects using the plugin. |

The first four properties, while optional, are intended to provide traceability between Vulcan build numbers, source repository	revisions and your deliverables.  You can inject these values into your "about" screen or in other places where version information is relevant.