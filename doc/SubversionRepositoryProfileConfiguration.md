# Introduction #

Settings are specified here which configure a repository which can be used by many Vulcan projects.

# Settings #

| **Field** | **Required** | **Description** |
|:----------|:-------------|:----------------|
| Description | Yes          | Label used to identify this repository.  This is the label that will appear in the list of choices available on the [Subversion project configuration](SubversionProjectConfiguration.md) screen. |
| Repository URL | Yes          | The absolute URL pointing to the root of your repository.  Supported protocols are `http`, `https`, `svn`, `svn+ssh` and `file`. |
| Username  | No           | If your repository requires authentication for read-access, enter the username here.  It is recommended that a service account with limited (read-only) access be used.  This option is not required if your server allows anonymous access. |
| Password  | No           | The password corresponding to the username. |

**Note:** The repository root should be the highest level of your repository (not including `trunk`) in order to use tags and branches correctly.

**Note:** Vulcan only requires read-access to your repository because Vulcan never modifies your repository.