# Introduction #

BEA WebLogic is a complete JEE Application Server which provides many enterprise capabilities.

Vulcan requires a Servlet 1.4 compatible container.  The WebLogic 9 series provides this capability.

# Known Issues #

## JSP Doctype ##

BEA Change Request Number CR287986 affects the way web browsers display xhtml pages in Vulcan.  This problem was resolved in version 9.2 MP 1.

See [this page](http://edocs.bea.com/wls/docs92/issues/known_resolved.html) for more information.

If you are using a version prior to 9.2 MP 1, it is recommended to upgrade prior to deploying Vulcan.