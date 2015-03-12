Projects can be created, modified and deleted through Vulcan's web interface.  Click the Setup link in the top menu on the Vulcan dashboard.

To create a new project, click New Project in the left-hand menu.  You can review and modify existing project configuration by clicking the project of interest in the same menu.

**Note:** _If this is the first time you are configuring Vulcan, you should probably configure your Repository Adaptor Plugins and Build Tool Plugins before creating new projects._

# Configuration Options #

The Project Configuration screen allows you to enter the following
information about your project.

| **Field** | **Required** | **Description** |
|:----------|:-------------|:----------------|
| Project Name | Yes          | The name you want to use to identify your project within Vulcan.  You may use mixed-case letters, numbers, hyphens and underscores. |
|Working Copy Directory | Yes          | The location to use when checking out files from your repository. |
|Site Index |No            |Vulcan allows users to browse the working copy of the most recent build.  If you have a landing page in the workig copy, you can configure the link from the build details page to link directly to that page.  For example, if you use Apache Maven and generate a site, you could use "target/site/index.html" for this value.|
| Bugtraq URL | No           | See IssueTrackerIntegration. |
| Bugtraq Keyword Regex | No           | See IssueTrackerIntegration. |
| Bugtraq ID Regex | No           | See IssueTrackerIntegration. |
| Repository Adaptor | Yes          | The plugin to use which will integrate with your source repository.  Choose from the available plugins, then you must click the Configure button to enter information specific to the adaptor. |
| Build Tool | Yes          | The plugin to use which will build your project.  Choose from the available plugins, then you must click the Configure button to enter  information specific to the build tool. |
| Dependencies | No           | If you are building more than one project with Vulcan, and some projects depend on others, you can express those dependencies here. Vulcan will automatically build your projects in the correct order. |
| Build Options | No           | This section has several options which should be self explanatory.  The first option is useful if you have many dependencies and don't want to assign each individual project to a scheduler.  By checking this option on your highest level projects, the dependencies will automatically be queued for build even though they are not assigned to a scheduler. |
| Update Policy | Yes          | This section allows you to configure incremental builds for your project.  See the section on IncrementalBuilds before changing the default setting. |
| Build Schedulers | No           | This is where you assign a project to be added to the build queue when the selected schedulers fire.  If you do not want your project to be built on a periodic schedule, do not check any of the schedulers. |
| Initial Build Number | No           | This option is only available when copying a project.  You can set the initial build number you would like to use with the new project.  The default setting uses the source project's current build number plus one.  If you would like to start counting from zero, simply set this field back to zero. |























