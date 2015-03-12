# Introduction #

If you are not using version control, stop reading this and go set one up.

You're still here?  Seriously, your team needs version control.

# File System support in Vulcan #

You can use the File System plugin for building projects that reside on the same computer.  Vulcan will copy the directory you specify to the `Working Copy Directory`.

If you configure a project using this plugin to build on a schedule, the project will always be rebuilt because the plugin does not attempt to determine if the source files have changed since the last build.