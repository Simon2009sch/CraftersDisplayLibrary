# DisplayTestPlugin deployment

Deployment is intentionally configured only in `DisplayTestPlugin`. The library release does not
copy or upload anything.

## Local server deployment

Set `PLUGINSFOLDER` in the IntelliJ Maven run configuration. Packaging the test plugin then copies
the final shaded `DisplayTestPlugin` JAR into that folder. If the variable is absent, this step is
skipped.

## Remote deployment

1. Copy `deploy-local.example.py` to `deploy-local.py` in this directory.
2. Keep `deploy-local.py` uncommitted; it is covered by the repository `.gitignore`.
3. Configure these environment variables in the IntelliJ Maven run configuration:

   - `PELICAN_PANEL_URL` — panel base URL, without a trailing slash
   - `PELICAN_API_TOKEN` — Pelican client API token; never put it in Git or the POM
   - `PELICAN_SERVER_ID` — Pelican client-server identifier
   - `PELICAN_PLUGIN_PATH` — optional remote path; defaults to `/plugins/DisplayTestPlugin.jar`

4. Run the `DisplayTestPlugin` package build. When `deploy-local.py` exists, Maven invokes it after
   packaging and passes the generated JAR path as its only argument.

The launcher uses Python's standard library and works with native Windows, Linux, and WSL. Set
`PELICAN_DEPLOY_DRY_RUN=true` to validate the artifact and configuration without uploading.

## Release behavior

The root `library` profile is active by default and contains only `CraftersDisplayLibrary`. The
`dev` profile adds `DisplayTestPlugin` for local development. Release/JitPack builds therefore
build and publish the library only; they never build or deploy the test plugin.
