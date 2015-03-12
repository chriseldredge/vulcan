# Introduction #

Instead of manually configuring source repositories and creating new projects, Vulcan can import projects.

Simply enter a URL pointing to your build script in source control, and Vulcan will figure out the correct parameters to use to build your project.

If your build tool supports sub-projects, you can specify whether Vulcan should create one big project that builds everything, or create a separate project for each sub-project too.

You can also specify which schedulers should build the new projects.

![http://farm1.static.flickr.com/171/472535838_9a38d4bf59_o.png](http://farm1.static.flickr.com/171/472535838_9a38d4bf59_o.png)

# Subversion Info #

If your repository requires credentials for read access, just enter the URL as you normally would.  When you click `import`, Vulcan will notice that it needs credentials, and the form will reload with inputs allowing you to specify them.  If you have already set up a project that uses this repository, Vulcan will reuse the existing repository profile so you don't have to enter the credentials over and over again.

Example: `http://svn.apache.org/repos/asf/maven/components/trunk/pom.xml`

# CVS Info #

Specify an URL as follows:

:method:[user:password@]host[:port]:cvsroot:module/buildfile

This is standard CVS connection string syntax with the path to your build script appended.

Example: `:pserver:anonymous@springframework.cvs.sourceforge.net:/cvsroot/springframework:spring/build.xml`

Currently there is no way to specify a tag or branch (only HEAD will be used).

Note: If authentication fails Vulcan will prompt you for a username and password, however you must specify the username and password in the connection string in order for the CVS plugin to see it.

# Maven Info #

Only Maven2 is supported.  You must specify the location of your maven2 installation in the [maven](MavenConfiguration.md) plugin configuration area prior to importing maven2 projects.

If you do not have a valid maven2 configuration, you will see this error message:

`Cannot import Maven 2 POM without a Maven Home defined that points to a Maven 2 installation.`

# Ant Info #

Ant project import should work as expected.  Sub-projects are not supported, so specifying the option to create multiple projects will be ignored.

# MSBuild Info #

The Microsoft.NET plugin supports importing projects from Microsoft Visual Studio Solution (.sln) files and from MSBuild project files.

If the option to create multiple projects is selected, Vulcan will look for included projects under a Solution file, or will look for ProjectReference nodes under an ItemGroup and create projects for each one found.



