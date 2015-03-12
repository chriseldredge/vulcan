# Prerequisites #

To build Vulcan from source you need:

  * [Java Development Kit](http://java.sun.com/) 5.0 or later
  * [Apache Maven](http://maven.apache.org/) 2.0 or later

In order to build the vulcan-dotnet plugin, you need:

  * [NAnt](http://nant.sourceforge.net/) 0.85 or later

In order for some of the unit tests to pass, you need:

  * [Apache Ant](http://ant.apache.org/) 1.6.5 or later
  * [Apache Maven](http://maven.apache.org/) 1.0.2 or later

# Checkout a working copy #

Checkout Vulcan on your workstation.

Using either the [Subversion](http://subversion.tigris.org) command line client:
```
svn co http://vulcan.googlecode.com/svn/trunk vulcan
```

Or using [TortoiseSVN](http://tortoisesvn.tigris.org/).

# Configure Maven Settings #

In your home directory (`C:\Documents and Settings\your_login` in Windows), create a folder named `.m2` if not present.  Create a file named `settings.xml` with the following contents:
```
<settings>
	<profiles>
		<profile>
			<activation>
				<activeByDefault>true</activeByDefault>
			</activation>
			<properties>
				<nunit.bin.dir>c:/Progra~1/NUnit-Net-2.0 2.2.8/bin</nunit.bin.dir>
				<nant.program.path>c:/Progra~1/NAnt-0.85-rc4/bin/NAnt.exe</nant.program.path>
				<nant.target.framework>net-2.0</nant.target.framework>
				<ant.home>c:/program files/java/apache-ant-1.6.5</ant.home>
				<maven1.home>c:/Program Files/Java/Maven-1.0.2</maven1.home>
				<maven2.home>c:/Program Files/Java/Maven-2.0.4</maven2.home>
				<project.build.number>0</project.build.number>
				<project.revision.numeric>0</project.revision.numeric>
			</properties>
		</profile>
	</profiles>
</settings> 
```

# Build Vulcan #

From the directory where you created your working copy, run
```
mvn install
```

The first time you do this you may need to download some jars that are not available in the maven repository.  Follow the directions that appear in the output.

The distribution war file ends up in `vulcan-web/target`.