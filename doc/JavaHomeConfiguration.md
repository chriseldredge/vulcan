# Introduction #

These settings allow you to use different Java installations for different projects.

# Configuration #

| **Field** | **Required** | **Description** |
|:----------|:-------------|:----------------|
| Description | Yes          | This is the label which will appear in the [project configuration](AntProjectConfiguration.md) for the Ant plugin. |
| Java Home | Yes          | The base directory (not the `bin` directory) where this Java Home lives. |
| Max Memory | No           | The max heap space, in megabytes, that the Java Home will be allowed to use.  This option uses the `-Xmx` flag and may not be compatible with Java Homes from vendors other than Sun Microsystems. |
| System Properties | No           | Properties and values, in the form of `name=value`, which will be used as system properties when invoking Java. |
