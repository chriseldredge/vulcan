The _Capability Maturity Model_ (CMM), and more recently _Capability Maturity Model (CMMI), Integrated_ are specifications developed by the [Software Engineering Institute](http://www.sei.cmu.edu/).

CMMI is a sort of cook book which does not dictate what processes must be used for software development, but specifies that whatever process is used should be documented.  Software organizations can be accredited at Level 1 through 5 of CMM / CMMI where the higher levels indicate a more mature process.

At Level 4, [Quantitative Project Management](http://seir.sei.cmu.edu/GDMforCMMI/CMMI_HTM_Files%5CQPM.htm) stipulates that quantitative metrics be used to actively monitor and manage a project during development.  Several of the metrics that Vulcan collects could be used to meet this requirement:

  * Build success rate (track pass/fail outcomes over a period of time)
  * Code coverage of unit tests
  * Lines of code, number of source files, number of packages, etc
  * Many more

_While the first bullet above is provided out of the box by using BuildHistoryReport, the others are supported by a combination of Vulcan, your build tool, and extensions to your build tool which produce XML metrics.  These metrics can be transformed such that they can be collected by Vulcan.  For more information, see XmlMetrics._

The metrics provided by Vulcan can be evaluated versus expected or ideal values.  If the actual metrics do not meet or exceed the expected values, project managers use this as impetus to identify small problems before they become big ones.  For example, you may require that unit tests written by developers exercise at least 80% of the production code base.  If this value is not met, this indicates that developers may be writing new production code with insufficient unit tests.  Project managers can use this information to resolve the problem before too much time is lost.