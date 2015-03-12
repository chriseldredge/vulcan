# Introduction #

For JEE developers, Vulcan will be pretty easy to install.  You just need to have a Java5 Runtime Environment and a Servlet 1.4 compliant Web Application Server.

# Prerequisites #

## Java Runtime Environment (JRE) ##

You can download the Java Runtime Environment from http://java.sun.com.

## Apache Tomcat ##

If you don't already have a web application server, Tomcat is the recommended software.  Download 5.5 or later from http://tomcat.apache.org.

# Installing Vulcan #

## Download Vulcan ##

Download the latest release (click the `Downloads` tab above).

## Configure Vulcan Home ##

Read VulcanHome before deploying Vulcan.  This step is optional but is best to perform before deployment to avoid creating unnecessary files and folders.

## Quick Start ##

First you should rename the file from `vulcan-war-x.y.war` to just `vulcan.war`.  This will make Tomcat use `/vulcan` as the context root.

You can just drop your download into `$TOMCAT_HOME/webapps` and Tomcat will explode the application and start it.

## Security ##

Vulcan has declarative security enabled by default.  Read SecurityConfiguration for information on how to customize the security (or disable it if you don't need it).  This step can be done after deployment.