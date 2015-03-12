# Introduction #

The forms in Vulcan for ManualBuild and other areas can be used to integrate other tools with Vulcan for kicking off builds, locking projects and other tasks.  This page outlines some of the more commonly used integration points.

# Project Locks #

Project locks allow a user or external system to prevent Vulcan from building a project (either by schedule or by user request).  The locks can be used for configuration management purposes or to prevent Vulcan from clearing a working copy while it is needed (e.g. for deployments or other tasks).

Project locks can be obtained by posting to /buildmanagement/manageLocks.do.

The following query parameters should be sent:

| Parameter | Description |
|:----------|:------------|
| action    | One of `lock`, `unlock`, or `clear`. |
| projectNames | One or more projects to lock.  If more than one project is to be locked, the projectNames parameter should be specified for each project. |
| lockId    | This parameter is only required when `action=unlock`.  This is the id that was in the response when locking the project(s). |
| message   | Optional message to display.  When specified this message will appear in Vulcan so users will understand why a project is currently locked.  |

Example REST request:

```
GET http://localhost:8080/vulcan/buildmanagement/manageLocks.do?action=lock&projectNames=common&projectNames=web-ui&message=Locked+for+QA
```

If all is well, an xml response like the following will be sent:

```
200 OK

<rest-response>
    <lock-id>12312434321</lock-id>
</rest-response>
```

If for some reason a project cannot be locked (if the project is being built or in the build queue), the request will fail and a `409 CONFLICT` response will be sent to the client.

To unlock projects:

```
GET http://localhost:8080/vulcan/buildmanagement/manageLocks.do?action=unlock&lockId=12312434321
```

If your request was not accepted, you will get a response similar to this:

```
400 Bad Request

<rest-response>
    <error request-parameter="action">This field is required.</error>
</rest-response>
```

# Manual Build #

The manual build form can be used to kick off a build immediately.  This can be used, for example, to start a build from a post commit hook in your source repository so a build will start whenever someone commits.

The following parameters should be specified:

| Parameter | Description |
|:----------|:------------|
| chooseTags | In most cases this should be set to `false`.  When set to `true`, Vulcan will prompt the client to override the tag or branch to use when building the project as well as where to build the project on the server. |
| updateStrategy | One of `Default`, `Full` or `Incremental`.  This setting applies to whether Vulcan will do a full checkout or an incremental update of the working copy. |
| targets   | The name of the project(s) to build.  If more than one project should be built, targets should be specified once for each project name. |
| buildOnNoUpdates | Set to `true` to force a build even when no updates are detected in the source repository.  Set to `false` for default behavior. |
| buildOnDependencyFailure | Set to `true` to force a build even when a project has dependencies that are not currently in PASS state. |
| dependencies | One of `NONE`, `AS_NEEDED` or `FORCE`.  This setting effects whether Vulcan will build dependencies first before building the specified targets. The default setting is `NONE`. |
| action    | Set this to `build` |

Here is an example request:
```
GET http://localhost:8080/vulcan/buildmanagement/manualBuild.do?action=build \
&targets=proj-a&targets=proj-b&chooseTags=false&updateStrategy=Default \
&buildOnNoUpdates=false&buildOnDependencyFailure=false&dependencies=NONE
```

When `chooseTags=true`, Vulcan will not start the build immediately.  Instead, it is important to save your session cookie from the response and pass it along in a subsequent response.  The initial response will look something like this:

```
200 OK
Set-Cookie: JSESSIONID=21312432413434; path=/vulcan; ...

<rest-response>
    <available-tags>
        <project>
            <project-name>Example B</project-name>
            <most-recently-used-tag>trunk</most-recently-used-tag>
            <default-build-directory>/home/vulcan/work/example-b</default-build-directory>
            <tags>
                <tag description="branches/refactor-database">branches/refactor-database</tag>
                <tag description="branches/mikes-feature-branch">branches/mikes-feature-branch</tag>
                <tag description="trunk">trunk</tag>
            </tags>
        </project>
        <project>
            <project-name>Example A</project-name>
            <most-recently-used-tag>trunk</most-recently-used-tag>
            <default-build-directory>/home/vulcan/work/example-a</default-build-directory>
            <tags>
                <tag description="trunk">trunk</tag>
            </tags>
        </project>
    </available-tags>
</rest-response>
```

This response is intended to provide the client with a list of available tags and branches to build from.  The server will keep the initial request in the session and wait for the client to submit a followup request that includes the following parameters:

| Parameter | Description |
|:----------|:------------|
| chooseTags | This should be set to true to match the original request. |
| targets   | This should be set using the same targets as the original request. |
| selectedTags | For each target, this should be set to the tag or branch that should be used to build the project. |
| workDirOverrides | For each target, this should be set to the working copy that should be used.  If you do not wish to override this value, use the default value from the resposne to the original request. |
| action    | This value should be set to `build`. |

**Note** When building multiple projects, the selectedTags and workDirOverrides parameters must be submitted in an order that corresponds to the order that the project names appear in the REST response to the initial request.  Note that this order may be different than the order the projects were submitted in.  The order reflects the order that the projects will be built in based on dependencies with other projects.