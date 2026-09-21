<p align="center">
  <img src="docs/assets/filetuck-icon.svg" width="88" height="88" alt="FileTuck — Android share-to-save app icon">
</p>

<h1 align="center">FileTuck — Save shared files on Android</h1>

<p align="center">
  Send files from the Android share sheet to Downloads or a folder you choose.<br>
  No account. No ads. No network permission.
</p>

<p align="center">
  <a href="https://github.com/aliahmadmalik/FileTuck/releases/latest"><img src="https://img.shields.io/github/v/release/aliahmadmalik/FileTuck" alt="Latest FileTuck APK release"></a>
  <a href="https://github.com/aliahmadmalik/FileTuck/actions/workflows/build.yml"><img src="https://github.com/aliahmadmalik/FileTuck/actions/workflows/build.yml/badge.svg" alt="Android build and lint status"></a>
  <img src="https://img.shields.io/badge/Android-10%2B-3DDC84?logo=android&amp;logoColor=white" alt="Requires Android 10 or newer">
</p>

<p align="center">
  <strong><a href="https://github.com/aliahmadmalik/FileTuck/releases/download/v2.0/FileTuck-2.0.apk">Download Android APK</a></strong>
  · <a href="https://github.com/aliahmadmalik/FileTuck/releases/latest">Release notes</a>
  · <a href="https://github.com/aliahmadmalik/FileTuck/issues/new/choose">Report a bug</a>
</p>

FileTuck is a lightweight Android **share-to-save app**. Share a photo, PDF, document,
or another file from an app that supports Android file sharing, then choose
**Save with FileTuck**. It copies the shared file to your selected destination.
You can save one file or several files at once without opening a full file manager.

[Install](#install-filetuck) · [How to use](#how-to-save-files) ·
[Privacy](#privacy-and-permissions) · [FAQ](#frequently-asked-questions) ·
[Build](#build-from-source) · [Security](SECURITY.md)

## Why FileTuck?

| Feature | What it does |
| --- | --- |
| Save from the share sheet | Adds **Save with FileTuck** as an Android share target. |
| Choose your destination | Save to Downloads, Downloads / FileTuck, or a custom folder. |
| Share multiple files | Copy several attachments in one share action. |
| Remember your choice | Reuse your destination across app launches. |
| Keep it simple | Native Android interface; no account, ads, or analytics SDKs. |

## Install FileTuck

**Android 10 or newer is required.** Download the signed **[FileTuck-2.0.apk](https://github.com/aliahmadmalik/FileTuck/releases/download/v2.0/FileTuck-2.0.apk)**
from the official GitHub release. The “Source code” ZIP/TAR files are for developers
and cannot be installed as apps.

1. Open the downloaded APK on your Android phone or tablet.
2. If prompted, allow that browser or file manager to **install unknown apps**.
3. Install FileTuck. You can turn that source’s installation permission off afterward.
4. Open FileTuck and choose where shared files should be saved.

Updates are installed manually from [GitHub Releases](https://github.com/aliahmadmalik/FileTuck/releases).
Official updates must use the same signing key. If you installed an earlier debug
build, you may need to uninstall it first; this resets FileTuck’s destination settings.
Files already copied to shared storage remain outside the app’s private storage.

<details>
<summary><strong>Verify the APK download</strong></summary>

Download `SHA256SUMS` alongside the APK from the same official release. In that folder:

```sh
# macOS
shasum -a 256 -c SHA256SUMS

# Linux
sha256sum -c SHA256SUMS
```

On Windows, run `Get-FileHash .\FileTuck-2.0.apk -Algorithm SHA256` in PowerShell
and compare the result with `SHA256SUMS`.

The release also includes `SIGNING-CERTIFICATE.txt`. The FileTuck signing
certificate’s SHA-256 fingerprint is:

```text
8601b981685333df94dc420295e0677a5be26b49796abc2b43114557cbca05f6
```

The certificate is public. The private signing key is kept outside this repository.
Checksums detect altered downloads; they do not by themselves prove an app is safe.

</details>

## How to save files

1. Select one or more files in another app and tap **Share**.
2. Choose **Save with FileTuck**. Expand the share sheet if needed.
3. A message reports the number of files saved and the destination.

Open FileTuck from your launcher to change the destination:

| Destination | Behavior |
| --- | --- |
| **Downloads** | Saves directly to the public Downloads folder; the default. |
| **Downloads / FileTuck** | Creates the FileTuck subfolder on the first save. |
| **Choose another folder** | Uses Android’s folder picker; available folders depend on the storage provider. |

Changing the destination affects future saves only. Existing files are not moved.
Canceling the picker keeps your previous choice.

## Privacy and permissions

FileTuck requests **no Android permissions**, including no internet or broad storage
permissions. Access to shared files and chosen folders uses Android URI grants.
There is no developer-operated server, advertising, or analytics in the app.

The app remembers the destination mode, custom folder name, and folder URI locally.
Cloud app backup is disabled through `allowBackup=false`; device-to-device transfer
behavior can vary by Android device manufacturer. A selected custom folder grant
may remain until replaced or app data is cleared.

**Cloud folders are different:** if you choose a cloud-backed document provider,
that provider may transfer the files under its own settings and privacy policy.
Files in shared storage follow Android’s access rules.

Read the [privacy policy](docs/PRIVACY.md). Report vulnerabilities through
[private security reporting](https://github.com/aliahmadmalik/FileTuck/security/advisories/new),
not a public issue containing sensitive information.

## Frequently asked questions

| Question | Answer |
| --- | --- |
| Can FileTuck download a web link? | No. Share the actual file attachment; plain text and URLs are unsupported. |
| Why is FileTuck missing from Share? | The source app may be sharing text or a link rather than a file URI. |
| Why did a save fail? | Check free space and source access. Reselect a custom folder if it was deleted or its permission was revoked. |
| What happens with duplicate names? | A new destination is requested; naming and collision behavior depend on the provider. |
| Can I save a large file? | Copying runs off the main thread, but there is no persistent background service, progress bar, or retry queue. Android can interrupt a transfer. |
| Can I use it offline? | Local file copies do not require internet. A cloud-backed source or destination may require connectivity. |
| Why won’t the APK install? | Check Android version, download integrity, and whether an existing installation has a different signer. Device security policies can also affect installation. |

If only some files save, successful copies remain. Re-sharing the whole selection
may create duplicates. Legacy `file://` shares are rejected; the sending app must
provide an accessible `content://` URI.

## Build from source

Use **JDK 17**, Android SDK **35**, and SDK Build Tools **34.0.0**. The included
wrapper selects **Gradle 8.11.1**; the project uses **Android Gradle Plugin 8.7.3**.

```sh
git clone https://github.com/aliahmadmalik/FileTuck.git
cd FileTuck
# Configure ANDROID_HOME, or open the project in Android Studio first.
./gradlew testDebugUnitTest assembleDebug lintDebug
```

On Windows use `gradlew.bat`. The debug APK is written to
`app/build/outputs/apk/debug/app-debug.apk`. Internet is required for the first
build’s tooling downloads. There are no third-party runtime libraries.

See [development and testing](docs/DEVELOPMENT.md) and [signing a release](docs/RELEASING.md).

## Project status and support

FileTuck 2.0 is an early public release. Build, lint, release signature, and download
checks passed. **Physical-device installation and file-sharing tests are still
pending**, so broad device compatibility has not been established. See the
[public-readiness review](docs/PUBLIC-READINESS.md) for scope and remaining work.

[Open a bug report](https://github.com/aliahmadmalik/FileTuck/issues/new/choose)
with your Android version, FileTuck version, source app, destination type, and
reproduction steps. Use harmless sample files and redact personal information.

## License

You may download and install the official release APK for personal use. No
source-code reuse license has been granted; all other rights are reserved.
The Gradle wrapper retains its upstream Apache 2.0 licensing notices.
