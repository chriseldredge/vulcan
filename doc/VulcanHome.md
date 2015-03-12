Vulcan uses a directory to store configuration data about your projects, including build outcomes and plugin data.

# Default Location #

By default Vulcan will use:

| **Operating System** | **Location** | **Remarks** |
|:---------------------|:-------------|:------------|
| Microsoft Windows    | %USERPROFILE%\vulcan | If you run Vulcan as a normal user, configuration data will typically go in `C:\Documents and Settings\joe\vulcan`.  If your web application server is running as a Windows Service, the service may be run with the LocalService account.  Since this account does not have a home directory, Vulcan settings will end up in `C:\vulcan`. |
| Mac OS X, Linux, Unix | $HOME/vulcan | Configuration data will go in a subdirectory named `vulcan` of the user running your web application server, e.g. `/home/joe/vulcan`. |


# Customizing Vulcan Home #

To change the location where Vulcan stores configuration data, navigate to the `WEB-INF` directory under the exploded web application.  For example, `$TOMCAT_HOME/webapps/vulcan/WEB-INF`.  Open the file `application.properties` and change the value of the `vulcan.home` property.

If you haven't deployed Vulcan yet, you can change this setting by exploding the war file manually.  A war file is really just a zip file.  Rename the file as a .zip file to make it easier to open with standard zip software.  Extract all the files into a new folder named `vulcan` in a temporary location (like your `Desktop` folder).  Make your changes to `beans.xml`, then move the entire temporary directory into `$TOMCAT_HOME/webapps`.