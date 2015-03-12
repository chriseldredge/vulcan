# Introduction #

This screen allows you to configure the Ant plugin for use with your project.

| **Field** | **Required** | **Description** |
|:----------|:-------------|:----------------|
| Build Script | No           | The relative path to your Ant build script.  If not specified, `build.xml` is assumed. |
| Target(s) | No           | The targets that should be executed, separated by spaces.  If not specified, the defaults in your build script will be used. |
| Java Home | No           | Select the JDK you wish to use to execute Ant.  These are defined in the global [Ant Configuration](AntConfiguration.md).  If you do not specify one, the JRE/JDK in which Vulcan is running will be used. |
| Verbose Logging | No           | Check this option to pass the `-verbose` flag to Ant, causing diagnostic messages to appear in the build log. |
| Debug Logging | No           | Check this option to pass the `-debug` flag to Ant, resulting in very verbose diagnostic messages in the build log. |
| Ant Properties | No           | Any number of properties, in the form of `name=value`, to be passed into Ant.  If the same property is defined here and in the global [Ant Configuration](AntConfiguration.md) section, the property defined here will take precedence. |