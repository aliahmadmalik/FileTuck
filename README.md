# FileTuck

**Share a file. Save it where you want it.**

FileTuck is a small native Android app that adds **Save with FileTuck** to the
Android share sheet. Copy one or more shared files to Downloads, a dedicated
FileTuck folder, or a folder you choose. No account is required.

## Download and install

Requires **Android 10 or newer** (API 29+).

**[Download FileTuck 2.0 for Android](https://github.com/aliahmadmalik/FileTuck/releases/download/v2.0/FileTuck-2.0.apk)**

Open the [Releases page](https://github.com/aliahmadmalik/FileTuck/releases/latest)
for release notes, checksums, and the signing certificate. GitHub’s
“Source code” ZIP and TAR files contain the project, not an installable app.

1. Download the APK on your Android device and open it.
2. If prompted, allow your browser or file manager to install apps from that source.
3. Install FileTuck, then turn that permission off again if you no longer need it.
4. Open FileTuck and choose your preferred save destination.

Release assets should also include `SHA256SUMS` and `SIGNING-CERTIFICATE.txt`.
To check the download on a computer, place the APK and `SHA256SUMS` in the same
folder and run `shasum -a 256 -c SHA256SUMS` (macOS) or
`sha256sum -c SHA256SUMS` (Linux). A checksum detects a changed download;
only trust checksums obtained from the authentic project release.

**Updating:** install the newer APK over the existing app. Android requires the
same signing key. A previously installed development/debug build may need to be
uninstalled before installing the public release; uninstalling resets FileTuck’s
folder settings. Files already saved in shared storage remain outside app storage.

## How to use

1. In another app, select a file or multiple files and tap **Share**.
2. Choose **Save with FileTuck** (you may need to expand the share sheet).
3. A message reports how many files were saved and the destination.

Open FileTuck from the launcher to change the destination:

| Destination | Behavior |
| --- | --- |
| Downloads | Saves to the public Downloads folder; the default. |
| Downloads / FileTuck | Creates the FileTuck subfolder on the first save. |
| Choose another folder | Uses Android’s folder picker; folder creation depends on the provider. |

Your selection is remembered. Changes affect future saves; existing files are
not moved. Canceling the folder picker keeps the previous selection.

## Features and limits

- Supports Android `ACTION_SEND` and `ACTION_SEND_MULTIPLE` file shares.
- Uses Android MediaStore for Downloads and the Storage Access Framework for custom folders.
- Creates a new destination document for each copy; filename collision behavior depends on the provider.
- Reports complete, partial, or failed saves and attempts to remove incomplete copies.
- Accepts `content://` file URIs with access supplied by the sending app.
- Plain text, web links, and legacy `file://` shares are not supported.
- No progress bar, background transfer service, or retry queue. Large transfers
  can be interrupted if Android terminates the app.
- Folder availability, supported filenames, and duplicate naming depend on Android
  and the selected storage provider. Protected files cannot be copied without access.

## Privacy and permissions

FileTuck requests **no network or broad storage permissions**. The source contains
no analytics, advertising, tracking SDKs, accounts, or remote backend. Android app
backup is disabled. FileTuck stores its destination choice, folder label, and
selected folder URI in private local preferences. It does not log file contents.

File access uses Android URI grants. A custom-folder grant can persist across
launches. Shared Downloads files may be accessible to other apps under Android’s
storage rules. If you select a cloud-backed document provider, that provider may
upload files under its own settings and privacy policy; FileTuck does not control it.

See [the privacy policy](docs/PRIVACY.md).

## Troubleshooting

- **Not in the share sheet:** share an actual file attachment rather than only a link or text.
- **Could not save:** check free space, confirm the source is accessible, and reselect
  the custom destination if its permission was revoked or the folder was removed.
- **Only some files saved:** check the missing files individually; earlier successful
  copies are retained. Re-sharing the whole selection may create duplicates.
- **App not installed:** confirm Android 10+, a complete download, and no existing
  installation signed with a different key.

## Build from source

Toolchain: **JDK 17**, **Gradle 8.11.1** (included wrapper), **Android Gradle Plugin
8.7.3**, Android SDK platform **35**, and build tools **34.0.0**. Runtime code uses
Android framework APIs without third-party runtime libraries.

1. Install Android Studio and the required Android SDK components.
2. Open the project, use JDK 17, and let Android Studio configure `local.properties`;
   alternatively set `ANDROID_HOME` to your SDK directory.
3. Run:

```sh
./gradlew assembleDebug lintDebug
./gradlew assembleRelease lintRelease
```

On Windows, use `gradlew.bat`. First builds need internet access to download tools
and dependencies. `local.properties` is machine-specific and must not be committed.

- Development APK: `app/build/outputs/apk/debug/app-debug.apk`
- Unsigned release APK: `app/build/outputs/apk/release/app-release-unsigned.apk`

An unsigned release APK is **not the public download**. Follow the
[release guide](docs/RELEASING.md) to sign, verify, and package it locally.

## Project layout

- `app/src/main/java/…/MainActivity.java`: destination settings screen.
- `app/src/main/java/…/SaveSettings.java`: local preferences and destination paths.
- `app/src/main/java/…/ShareReceiverActivity.java`: share handling and file copies.
- `app/src/main/res/`: app name, messages, colors, themes, and icon resources.
- `scripts/package-release.sh`: verifies and packages a locally signed release APK.
- `docs/`: privacy policy, release process, and release notes.

Application ID: `com.aliahmad.savetodownloads`. Version: **2.0**; version code: **6**.
The application ID is public metadata and must remain stable for updates.

## Testing and contributions

CI builds debug and unsigned release variants and runs Android lint. It does not
sign or publish releases. There are currently no automated device tests.
Before a public release, run the manual checks in the release guide on a device.

For bug reports, include Android version, FileTuck version, source app, destination
type, and steps to reproduce. Use harmless sample files and remove personal data
from screenshots and logs. Do not attach private files, signing keys, or credentials.

## License

You may download and install the official release APK for personal use. No
source-code reuse license has been granted; all other rights are reserved.
The Gradle wrapper retains its upstream Apache 2.0 licensing notices.
