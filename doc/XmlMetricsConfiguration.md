# Introduction #

The XML Metrics plugin scans your working copy after a build to summarize on various statistics that were reported during the build process.

Read more at XmlMetrics.

# Configuration #

| **Field** | **Required** | **Description**|
|:----------|:-------------|:|
| Include Patterns | No           | List of one or more glob patterns to use to find XML files in your working copy which should be included.  Use `**` to indicate any number of subdirectories. |
| Exclude Patterns | No           | List of glob patterns to exclude.  Only files matched by `Include Patterns` would need to be excluded here.  This option is primarily useful for performance.  If you have extremely large XML files in your working copy which do not contain metric data supported by Vulcan, you can exclude these files to speed up processing. |

The files that match one or more `Include Patterns` and do not match any `Exclude Patterns` will be merged into one big XML Document which will be searched for metrics that are supported by Vulcan.  Matches will show up in your build reports.