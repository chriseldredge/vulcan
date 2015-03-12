# Introduction #

This plugin requires a valid installation of [Apache Ant](http://ant.apache.org/).  Once you've installed ant, you can configure Vulcan to point to the location where it was installed.

All of the normal peculiarities of Ant apply when using it with Vulcan.  For example, you still have to put the JUnit jar into `$ANT_HOME/lib` if you plan to execute JUnit tests.

However, Vulcan executes Ant directly (instead of using `ant.sh` or `ant.bat`), so it is not necessary to modify them to return the exit code.

# Configuration #

| **Field** | **Required** | **Description** |
|:----------|:-------------|:----------------|
| Ant Home  | Yes          | This is the absolute path to the base directory where Ant was installed. |
| Java Homes | No           | You can configure any number of Java Homes, then select the one you want to use for each project.  This allows you to build different projects with different versions of Java.  By default, the Java Home which in which Vulcan is running will be available.  However, if this is a Java Runtime Environment, it may not be suitable for building projects since they probably require a full Java Development Kit. See JavaHomeConfiguration. |
| Ant Properties | No           | You can specify any number of properties here which will be passed to Ant.  The properties defined here are globally scoped, meaning that any project which uses Ant will have these properties defined. |
| Build Number Property | No           | This property will contain the build number that Vulcan assigns so your build script can reference it for versioning or other purposes. |
| Revision Label Property | No           | This property will contain the revision label that corresponds to the revision of the working copy. |
| Revision Number Property | No           | This property is similar to `Revision Label Property` above, but is an integer value.  For example, if you are using Subversion, the above value my be `r123` and this property would be `123`. |
| Revision Tag Name Property | No           | The tag name that was used to populate the working copy.  This value will be `trunk` in most cases, but could also be a tag or branch value. |

The last four properties, while optional, are intended to provide traceability between Vulcan build numbers, source repository	revisions and your deliverables.  You can inject these values into Jar manifests, properties files or anywhere else.  For example:

```
<target name="inject-version">
	<echo file="docroot/WEB-INF/version.properties">version.build=${project.build.number}</echo>
	<echo file="docroot/WEB-INF/version.properties" append="true">version.revision=${project.revision}</echo>
	<echo file="docroot/WEB-INF/version.properties" append="true">version.tag=${project.tag}</echo>
</target>
```

The `Revision Number Property` is intended for constructing ordinal version numbers.  In other cases, the `Revision Label Property` may provide a better representation of the actual revision.