# Introduction #

An incremental build is defined as a build that is performed while existing intermediate artifacts are already built.  These intermediate artifacts (class files, jars, assemblies, etc.) may be used without being rebuilt if your build process believes that they are up to date.

Incremental builds can reduce build time in 2 places.  First, instead of deleting the working copy completely, an existing working copy may be updated with any changes that are found in your source repository.  If you have a very large project, this can dramatically reduce the time taken interacting with your source repository.  Additionally, if your build tool must compile a large amount of source files, time can be saved by only compiling source files which have changed.

While incremental builds provide speed-up benefits, they also have some risky side effects.  Ultimately, it comes down to how much you trust your build scripts and build tools to detect changes and rebuild everything that needs to be rebuilt.  However, anecdotal evidence suggests that incremental builds can sometimes produce spurious errors that simply go away by cleaning all intermediate artifacts and performing a full build.  For this reason, it is recommended that incremental builds be avoided in almost all cases.

# Vulcan Support For Incremental Builds #

Vulcan allows you to perform incremental builds in 2 places.  First, in
ProjectConfiguration you can configure your project to use incremental builds on a regular basis.  Second, you can override the project default when performing a ManualBuild.

There are some cases in which Vulcan will perform a full build even though an incremental build was requested.  If any of the following criteria are detected, a full build will be performed:

  * Working Copy location does not exist or is empty
  * Previous build does not exist
  * Previous build oucome resulted in ERROR
  * Previous build used a different branch or tag (see ManualBuild)

# The Best of Both Worlds #

If you need the speedup that incremental builds provide, but worry about making sure that your project can be built from scratch, there is a compromise option in the ProjectConfiguration settings.  You can configure Vulcan to perform a full build the first time the project is built each day, then Vulcan will perform incremental builds for the rest of the builds on the same day.