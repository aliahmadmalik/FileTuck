# Development and testing

## Toolchain

| Component | Version |
| --- | --- |
| Minimum Android | Android 10 / API 29 |
| Compile / target SDK | API 35 |
| Build Tools | 34.0.0 |
| JDK | 17 |
| Gradle wrapper | 8.11.1, with distribution checksum verification |
| Android Gradle Plugin | 8.7.3 |
| App version / code | 2.0 / 6 |

The application ID is `com.aliahmad.savetodownloads`. Keep it and the signing key
stable for compatible updates. The app’s Java code uses Android framework APIs.

Configure the SDK through `ANDROID_HOME` or Android Studio’s local SDK settings.
Do not commit `local.properties`, passwords, signing keys, or generated build files.

```sh
./gradlew assembleDebug assembleRelease lintDebug lintRelease
```

Outputs:

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Unsigned release APK: `app/build/outputs/apk/release/app-release-unsigned.apk`
- Lint reports: `app/build/reports/lint-results-debug.html` and `lint-results-release.html`

The unsigned release must be signed before distribution. See [RELEASING.md](RELEASING.md).

## Source map

| File | Responsibility |
| --- | --- |
| `MainActivity.java` | Destination settings and Android folder picker |
| `SaveSettings.java` | Local preferences and destination paths |
| `ShareReceiverActivity.java` | Share intent parsing, file copying, result messages |
| `app/src/main/res/` | App icon, themes, colors, and message resources |
| `scripts/package-release.sh` | Signature, version, permissions, alignment, and checksum checks |

Java files are under `app/src/main/java/com/aliahmad/savetodownloads/`.

## Validation

CI builds both variants and runs Android lint. Actions are pinned to commit SHAs;
Dependabot proposes updates for review. CI has read-only repository permissions,
no release signing credentials, and does not publish APKs.

There are no automated unit or device tests yet. Builds and lint are useful checks,
but they do not establish that sharing, storage providers, or device upgrades work.
Use the manual device checklist in [RELEASING.md](RELEASING.md).

Current lint warnings concern orientation, backup configuration, translation/plural
handling, icon resources, and unused resources. They are not proof of a security
problem, but should be reviewed before broader promotion.

## Contributing and support

Use the issue forms for reproducible bugs and focused feature requests. Do not
submit copyrighted/private sample files, tokens, keystores, or unredacted logs.
Discuss substantial code changes before opening a pull request. The repository
currently has no source-code reuse license; visibility is not an open-source license.
Security reports belong in the private channel described in [SECURITY.md](../SECURITY.md).
