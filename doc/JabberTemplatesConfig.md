# Introduction #

Part of JabberPluginConfig.

The templates are used to send messages to clients based on certain conditions.

| **Template Name** | **Description** |
|:------------------|:----------------|
| Comitter Message Template | The text that should be sent in the IM.  See Template Substitutions below for more info. |
| Build Master Template | Alternate template to send to build masters.  This is to help build masters distinguish between when they may have broken a build and when they are being notified that someone else broke the build. |
| Claim Responsibility Acknowledgement Template | A confirmation message to send back to users when they claim a broken build. |
| Claim Notification Template | Message to send to other users when someone claims a broken build. |
| Pithy Retorts     | Messages (one per line) to send to users when they attempt to engage the Jabber client in converstation. |


# Template Substitutions #

The templates support the following expressions which will be substituted with the relevant information at runtime:

| **Expression** | **Description** |
|:---------------|:----------------|
| {Message}      | Error message recorded by build tool. |
| {File}         | File, if applicable, where error occurred. |
| {LineNumber}   | The line number in {File} where error occurred. |
| {Code}         | The error code, if available. |
| {ProjectName}  | The name of the project. |
| {BuildNumber}  | The build number. |
| {Link}         | The URL that will link to the build report. |
| {Users}        | A list of other users who have been notified.  This is intended to facilitate communication for determining which committer actually broke the build. |
| {ClaimUser}    | The user that claimed a build failure (only availble for Claim Notification Template. |

## Advanced Template Expressions ##

The substitution parameters use the Java [MessageFormat](http://java.sun.com/j2se/1.5.0/docs/api/java/text/MessageFormat.Field.html) family of formatters.  This means that you can supply additional formatting options as you would with a standard MessageFormat.

Example:
```
{Line,number}
```
_Formats the line number as human readable with Locale specific decimal grouping separators._

In addition to the standard MessageFormat capabilities, this plugin supports conditional rendering of blocks of text based on whether or not a given parameter is defined and non-null/non-blank.

Example:
```
{Users?,We also notified: {Users}}
```
_This text will render "`We also notified: Sam, Jordan`" when other users are also being sent messages, but will render nothing when `{Users}` is blank._