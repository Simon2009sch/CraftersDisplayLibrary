# GitHub releases

The [release workflow](../../.github/workflows/release.yml) runs when a tag starting
with `v` is pushed. It builds the library on GitHub with Java 21, runs Maven's
`verify` lifecycle, and publishes a GitHub release with generated release notes,
the library JAR, and `SHA256SUMS.txt`. The test plugin is excluded by the explicit
`library` profile. No additional repository secrets are required; publishing uses
the workflow's `GITHUB_TOKEN` with `contents: write` permission.

## Publish a version

1. Set the version in `CraftersDisplayLibrary/pom.xml`. Keep the library dependency
   in `DisplayTestPlugin/pom.xml` in sync. The parent POM has its own version and
   does not need to match the library version.
2. Build and check the project in IntelliJ IDEA, then commit and push the changes,
   including the workflow.
3. Create and push a matching tag, for example for the current library version:

   ```sh
   git tag v1.1.1
   git push origin v1.1.1
   ```

4. Check the **Release** run in GitHub Actions and the resulting entry under
   **Releases**.

The tag must exactly match `v` plus the library's POM version. Versions such as
`1.2.0-rc.1` produce prereleases that are not marked as latest. Snapshot versions
and mismatched tags fail before the build. Existing releases are not overwritten;
rerun a failed workflow only after checking whether publication already succeeded.
Do not move a published tag to a different commit.

Local builds remain in IntelliJ IDEA. The Maven command in the workflow runs only
on the GitHub-hosted runner. This workflow publishes downloadable GitHub assets;
the existing JitPack dependency setup remains separate.
