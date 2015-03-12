# Introduction #

The XmlMetrics plugin comes bundled with Vulcan.  The plugin works by scanning your working copy for XML files after a build completes.  The files that are found are merged into a large XML document which is run through a series of transforms to produce metrics that can be stored and displayed in Vulcan's reporting system.

![http://farm1.static.flickr.com/160/418809366_3f461014bb_o.png](http://farm1.static.flickr.com/160/418809366_3f461014bb_o.png)

_Example metrics table displaying unit test information_

The plugin has special support for unit test results.  Test failures will be tracked from one build to the next in order to track when the test started failing.  This information provides an indication of how quickly your team responds to failing tests, and how effective they are at keeping the build green.

![http://farm1.static.flickr.com/184/418809365_df05f9f507_o.png](http://farm1.static.flickr.com/184/418809365_df05f9f507_o.png)

_Example of unit test failures_

At this time, XmlMetrics supports the following:

  * [JUnit](http://www.junit.org/index.htm)
  * [NUnit](http://www.nunit.org/)
  * [Selenium](http://www.openqa.org/selenium/)
  * [Cobertura](http://cobertura.sourceforge.net/) (Java code coverage)
  * [Emma](http://emma.sourceforge.net/) (Java code coverage)
  * [NCover](http://www.ncover.com/) (.NET code coverage)
  * FxCop (static code analysis)

Support is also planned for more tools.

# Configuration #

To prevent Vulcan from wasting resources by processing irrelevant XML documents in your build tree, the metrics plugin will only process files that match a configurable search path.  This allows Vulcan to process only the XML that contains metrics.

The plugin is pre-configured to search in some standard locations for XML files, but you may need to customize the search patterns to allow your files to be processed.  These settings can be found in the `setup` area.  In the left-hand menu, select "XML Metrics" under the "Publisher Plugins" sub-menu.

You can specify as many `Include Pattern` values as you need.  Similar to Apache Ant file glob syntax, use `*` to match one directory and `**` to match 0 or more directories.

# Collecting your own metrics #

You can get your own metrics to show up in these reports.  There are two basic approaches to accomplish this goal.

## Producing XML in target schema ##

You can generate XML files in your working copy that follow this basic structure:
```
<root>
  <metric key="A label" value="12" type="number|percent|string"/>
  <.../>
</root>
```

The XmlMetrics plugin will use these metrics as is.  The `key` attribute can be a message key for internationalization or it can be a literal label for the metric you are providing.

The `type` attribute must be one of the available data types (number, percent, or string).  This attribute helps format data in reports.  For `percent` data, the value should be a real number between 0.00 and 1.00 (not a number between 0 and 100).  This data will be displayed with a percent symbol in reports.

## Adding your own transforms ##

The plugin can be extended to include other XSL transforms, though at this time the plugin must be rebuilt from source in order to include the new stylesheets.  If you are adding support for a popular product, check with the [vulcan-devel](http://groups.google.com/group/vulcan-devel) group to collaborate with others.  The extensions will likely be added to the core distribution.