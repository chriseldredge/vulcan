# Introduction #

If you use an issue tracker, Vulcan can link messages in repository commit logs to your issue tracker.  This feature allows users to drill down from a build report into your issue tracking system, providing enhanced access to information.

![http://farm1.static.flickr.com/158/418804896_5ee9ab1f00_o.png](http://farm1.static.flickr.com/158/418804896_5ee9ab1f00_o.png)

# Basic Configuration #

In the ProjectConfiguration for your project, enter an URL which links to your issue tracker under the `Bugtraq URL` setting.  Use the value `%BUGID% where the unique bug ID should be substituted.  Examples:

```
http://code.google.com/p/vulcan/issues/detail?id=%BUGID%

http://bugs.example.com/show_bug.cgi?id=%BUGID%
```

Next, enter a regular expression which will match references to bugs and issues under `Bugtraq Keyword Regex`.  Usually, you would want to match the word _bug_ or _issue_ (or maybe both), followed by an integer.  For example:

```
[Bb]ug (\d+)
```

The above pattern would match "Bug" or "bug" followed by a single space, followed by one or more numbers.  The id is placed in a capture group to allow it to be extracted for linking.

Finally, if you would like to match more complicated patterns, like the word _bugs_ followed by one or more integers, you could use `Bugtraq Keyword Pattern` to match the entire pattern, then set `Bugtraq ID Pattern` to match the individual IDs in the larger pattern.

# Subversion Configuration #

The way Vulcan integrates with issue trackers is compatible with the [guidelines](http://tortoisesvn.net/docs/release/TortoiseSVN_en/tsvn-dug-bugtracker.html) outlined by the Tortoise SVN project.

If you have these properties set on your repository, you can configure the Vulcan [Subversion](SubversinRepository.md) plugin to obtain the properties from your repository and synchronize the settings in Vulcan.  That way, you only have to configure these settings in one place.
