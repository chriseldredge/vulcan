# Introduction #

Before properly configuring Subversion for a given project, you must create one or more Repository profiles in the [main Subversion configuration area](SubversionRepositoryConfiguration.md).

# Settings #

| **Field** | **Required** | **Description** |
|:----------|:-------------|:----------------|
| Repository | Yes          | Select from the repository profiles defined in the [main Subversion configuration area](SubversionRepositoryConfiguration.md). |
| Repository Path | No           | The path, relative to the repository root, that should be checked out.  Most often, this would simply be `/trunk`, but you can use any path as long as it exists in your repository. |
| Recursive | No           | Uncheck this setting to perform a "shallow" checkout of your project.  All nested directories under the `Repository Path` will be skipped, meaning that only files immediately residing in the `Repository Path` will be checked out.  This option is particularly useful when building a maven2 POM project when nested modules are present in the repository, but you only want to build the parent POM. |
| Obtain Bugtraq Properties | No           | When this option is enabled, the Subversion plugin will check `Repository Path` for `bugtraq` properties.  If properties are detected, the Bugtraq settings in ProjectConfiguration will be overwritten with the detected values.  This allows you to specify these settings only once on your repository. |

**Note:** For more information on bugtraq, see:

  * [TortoiseSVN guidelines](http://tortoisesvn.net/docs/release/TortoiseSVN_en/tsvn-dug-bugtracker.html)
  * IssueTrackerIntegration
