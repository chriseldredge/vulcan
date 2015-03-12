# Introduction #

The `Manual Build` link in the top menu provides several options for building projects.

| **Option** | **Description** |
|:-----------|:----------------|
| Source     | The default option builds the project using the branch or tag (usually trunk or HEAD) configured in `setup`.  If you select the alternate option, Vulcan will query your source repository to determine which branches and tags are available.  Once you select one, Vulcan will populate a working copy using that tag instead of the default one.  See BuildAndRelease for use cases. |
| Update Policy | The default option inherits the setting in ProjectConfiguration.  The other two options override that setting. |
| Projects   | Select the projects you want to schedule.  If your projects have interdependencies, Vulcan will automatically build them in the correct order. |
| Build Options | Here you can force a build even if there are no updates in your source repository.  This option is sometimes necessary to recover from a transient failure.  You can also force the build to proceed even if the project you are building depends on other projects which are not successfully built. |

# Incremental Builds #

If you are performing a manual build because you've committed a fix and want to rebuild quickly to verify that your fix is sound, you can use the incremental build option to speed up the build.  See IncrementalBuilds for other uses.

# Conditions for rebuild #

When Vulcan schedules a project for build, or when you perform a manual build without specifying the _"Build even if no updates are available"_ option, Vulcan uses a series of checks to determine whether a new build should be performed.  The following criteria would cause Vulcan to rebuild a project:

  * Changes found in repository
  * Dependency has newer build timestamp
  * Full build requested and previous build was incremental
  * ProjectConfiguration (or plugin configuration) was modified since last build
