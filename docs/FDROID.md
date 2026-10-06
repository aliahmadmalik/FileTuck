# F-Droid submission

Status: [Request for Packaging #4453](https://gitlab.com/fdroid/rfp/-/work_items/4453)
submitted on 25 September 2026. Version 2.0.2 adopts `com.nosleepjustcode.filetuck` and public author
`nosleepjustcode` for the first F-Droid submission. Earlier development tags
use a different application ID.
[Merge request !51427](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/51427)
was submitted on 6 October 2026 with the v2.0.2 source commit. Local metadata
checks pass. Fork CI is blocked by GitLab account verification; the MR asks
maintainers to trigger F-Droid CI. FileTuck has not yet been accepted or published.

## Resolve before submission

1. Apache-2.0 has been selected with owner authorization and added as `LICENSE`.
   README.md applies it to source, documentation, and original artwork. Include
   these changes in the public release and preserve third-party notices.
2. Version 2.0.2 (versionCode 8) contains the license and Fastlane metadata.
   Source tag v2.0.2 is public; existing development tags are preserved.
3. Add real device screenshots under
   `fastlane/metadata/android/en-US/images/phoneScreenshots/`. A 512px PNG icon
   rendered from the existing SVG is included at
   `fastlane/metadata/android/en-US/images/icon.png`.
   Screenshots should show the actual app. Changelog 8.txt describes this release.
4. Run the device checklist in RELEASING.md. Existing documentation records
   physical-device testing as pending.
5. Validate the recipe below using current fdroidserver tooling and the F-Droid
   build environment. A local Gradle build alone does not validate F-Droid's
   source scan, toolchain availability, or isolated build.

## Build recipe draft

Local verification on 25 September 2026: `assembleRelease lintRelease` succeeded
using the cached toolchain. Lint reported two SDK-age warnings (OldTargetApi and
GradleDependency), with no errors. No Android device was connected for screenshots
or device testing. Current fdroidserver tooling is installed in an isolated local environment.
The final recipe must pass metadata checks; isolated F-Droid build validation
remains pending until CI runs.

The submitted recipe is
`metadata/com.nosleepjustcode.filetuck.yml` in a fork of
[fdroiddata](https://gitlab.com/fdroid/fdroiddata). Local metadata checks pass; F-Droid isolated build validation remains pending.

```yaml
Categories:
  - System
License: Apache-2.0
AuthorName: nosleepjustcode
AuthorWebSite: https://gitlab.com/nosleepjustcode
SourceCode: https://github.com/aliahmadmalik/FileTuck
IssueTracker: https://github.com/aliahmadmalik/FileTuck/issues
Changelog: https://github.com/aliahmadmalik/FileTuck/releases

AutoName: FileTuck

RepoType: git
Repo: https://github.com/aliahmadmalik/FileTuck.git

Builds:
  - versionName: '2.0.2'
    versionCode: 8
    commit: 893df9e9c90e1c6af8327907728e2b784028bb56
    subdir: app
    gradle:
      - yes

AutoUpdateMode: Version v%v
UpdateCheckMode: Tags ^v[0-9]+\.[0-9]+(\.[0-9]+)?$
CurrentVersion: '2.0.2'
CurrentVersionCode: 8
```

Current source uses JDK 17, SDK 35, Build Tools 36.0.0, Gradle 9.7.1, and
Android Gradle Plugin 9.4.1. Check that F-Droid's build image supports these
versions; resolve any toolchain failures before submitting. There are no declared
third-party advertising/tracking SDKs; JUnit and Robolectric are test dependencies.
Do not bypass dependency scanning with broad ignore rules.

In the configured fdroiddata checkout/build environment, run:

```sh
fdroid readmeta
fdroid rewritemeta com.nosleepjustcode.filetuck
fdroid lint com.nosleepjustcode.filetuck
fdroid build com.nosleepjustcode.filetuck
```

For a first submission, open a [Request for Packaging](https://gitlab.com/fdroid/rfp/-/issues)
with the public source tag, license, description, and honest validation status.
Alternatively, open a merge request to fdroiddata with validated metadata and build results.
F-Droid maintainers review it, then their infrastructure builds and publishes the
app after acceptance. Uploading the GitHub APK alone is not a submission.

## Signing and updates

The draft uses the ordinary F-Droid build/signing path. An APK signed by F-Droid
cannot normally update an installation signed with the existing GitHub release
key. Document the distribution-channel switch for users. Keeping the upstream
signature requires a separately configured and verified reproducible-build
workflow; the Gradle reproducibility settings alone do not prove that it works.
Never provide the private signing key to F-Droid.

## References

- [Inclusion policy](https://f-droid.org/en/docs/Inclusion_Policy/)
- [Submission guide](https://f-droid.org/docs/Submitting_to_F-Droid_Quick_Start_Guide/)
- [Build metadata reference](https://f-droid.org/docs/Build_Metadata_Reference/)
- [Reproducible builds](https://f-droid.org/docs/Reproducible_Builds/)
