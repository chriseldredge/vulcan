# Unnecessary Clutter #

Continuous integration software often tags a build upon success, or in some cases even failed builds.  Automatic tagging produces clutter in your source repository (tagging builds that may never need to be referenced again).

If you are using [Subversion](SubversionRepositoryConfiguration.md), tagging every build is unnecessary to begin with, since Vulcan tracks the revision of the working copy used for each build.  This revision can be used at any time in the future to create a tag from a previous build.

If you are using [CVS](CvsRepositoryConfiguration.md), the timestamp that Vulcan displays as a revision can be used in the same way.  You can create a working copy with files that are no newer than this timestamp, then produce a tag of these files.

# Releasing From a Tag #

Instead of tagging a release candidate, an opposite approach may be taken.  If you want to build a previous version of your software, you can specify a tag or branch to build from when using the ManualBuild capability.

![http://farm1.static.flickr.com/180/419326592_b1cae9f04c_o.png](http://farm1.static.flickr.com/180/419326592_b1cae9f04c_o.png)

The advantage of building **_after_** tagging is that you can inject version information into your deliverables.  This method provides traceability between your delivered artifacts and the tag that they were built from.  This way, if any follow on work needs to be done, you know which tag needs to be branched for further development.

# Injecting Version Info #

The various build plugins for Vulcan have predefined properties which are passed into your build scripts.  These properties can be used to version your deliverables.  The following data is available:

| | | Example |
|:|:|:--------|
| **Repository Revision** | The revision of the working copy | [r123](https://code.google.com/p/vulcan/source/detail?r=123) |
| **Numeric Repository Revision** | The revision of the working copy, as an integer | 123     |
| **Repository Tag** | The tag/branch of the working copy | trunk   |
| **Build Number** | The build number for the project | 95      |

See the documentation for your particular build plugin for details.