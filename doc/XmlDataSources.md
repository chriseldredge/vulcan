# Introduction #

Vulcan stores project configuration, dashboard status and build reports as XML.

# Dashboard #

Summary information for the latest build outcomes of all projects (as seen on the dashboard) can be accessed as XML by requesting /projects.jsp on your instance of Vulcan.

Example:
```
<projects>
  <project name="commons-logging">
    <status>FAIL</status>
    <timestamp age="2 Hour(s)" millis="1173738388012">2007-03-12 18:26:28</timestamp>
    <message>
      Build failed on target compile-non-log4j: "Compile failed; see the compiler error output for details."
    </message>
    <build-number>3</build-number>
    <revision numeric="496002">r496002</revision>
    <repository-tag-name>trunk</repository-tag-name>
  </project>
  <project name="...">
     ..
  </project>
</projects>
```

# Build Details #

The details of a build report are available as XML from the same URI which generates the XHTML report.  Simply append "xml" to the URI, e.g. `/projects/commons-io/1234/xml`.

The structure of this XML document should be intuitive when you review an example.