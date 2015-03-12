## Introduction

Vulcan provides ContinuousIntegration and BuildAndRelease services in a web application which can easily by accessed and navigated by developers, project managers and stakeholders.  Notification plugins publish reports on desired build outcomes over RssFeed and EmailNotification.

Vulcan runs in a standard JEE Web Application Server, like [Apache Tomcat](doc/Tomcat.md) or [BEA WebLogic](doc/WebLogic.md).

## Supported Build Tools

  * [Apache Ant](http://ant.apache.org/)
  * [Apache Maven](http://maven.apache.org/)
  * [MSBuild](http://msdn.microsoft.com/en-us/library/wea2sca5%28VS.90%29.aspx)
  * [NAnt](http://nant.sourceforge.net/)
  * [Shell scripts](doc/ShellPluginConfiguration.md) including Autoconf, GNU Make, etc.

## Supported Source Repository Software

  * [Git](http://git-scm.com/)
  * [Mercurial](http://mercurial.selenic.com/)
  * [Subversion](http://subversion.apache.org/)
  * [CVS](http://en.wikipedia.org/wiki/Concurrent_Versions_System)
  * FileSystem pseudo-scm

## Features

  * Import your projects in [one step](doc/ImportProject.md)
  * Scheduled and [manual](doc/ManualBuild.md) builds
  * [Incremental](doc/IncrementalBuilds.md) and full checkout
  * [Build](doc/BuildAndRelease.md) from branch/tag
  * Configuration through web interface
  * Inter-project dependencies
  * [Issue tracker integration](doc/IssueTrackerIntegration.md)
  * Build multiple projects asynchronously
  * Capture customizable metrics for trend analysis
  * Role based security (optional)
  * RSS Feed, Jabber and e-mail notification
  * Browse project sandbox through web interface
  * Support for internationalization

## More Info

Unlike other ContinuousIntegration software, Vulcan allows you to configure most settings through its web interface.  This includes ProjectConfiguration, SchedulerConfiguration, BuildDaemonConfiguration, and configuration of build plugins, repository plugins and notification plugins.  The only settings which cannot be edited through the web interface are SecurityConfiguration and VulcanHome.

Vulcan's ManualBuild capability provides many advanced features, including the ability to build from a tag or branch.  This capability allows users to produce a ReleaseBuild with version information that provides traceability from the delivered product back to the source repository.

While the standard distribution of Vulcan supports the most popular build tools and source repository software, an extensible plugin architecture allows third-parties to easily add support for custom tools and repositories.

Vulcan retains summary information about every build, providing metrics which can be aggregated over time to track success rate, code coverage, unit test failure rate and custom metrics.  This capability provides a framework for meeting CapabilityMaturityModelIntegrated Level 4 requirements for Quantitative Project Management.  Metric data is gathered by the XmlMetrics plugin, which can be extended to include metrics from any XML document.

Vulcan also provides IssueTrackerIntegration.  If you have an issue tracker with a web interface, Vulcan can link directly to issues that are referenced in commit logs, providing traceability between the latest integrated build and the issues which were addressed.

A combination of RestServices and XmlDataSources provide an open API that can be used by custom tools and utilities to obtain information about current build status and build history.  Builds can also be scheduled by treating the ManualBuild form as a REST service.

## Getting Started

Please see the section on [Getting Started](doc/GettingStarted.md).