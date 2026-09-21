# FileTuck 2.0

Save shared Android files to Downloads, Downloads / FileTuck, or a custom folder.

- Supports single and multiple file shares.
- Remembers the chosen destination between launches.
- Uses Android’s folder picker for custom destinations.
- Reports successful, partial, and failed saves.
- Requires Android 10 or newer; no network or broad storage permissions.
- Restricts shared sources to content URIs; legacy file-path shares are rejected.

## Installation

Download **FileTuck-2.0.apk** under Assets and open it on Android.
The source archives are for developers and cannot be installed as apps.
`SHA256SUMS` verifies the download; `SIGNING-CERTIFICATE.txt` records the public
signing certificate identity. Keep the same signer for future updates.

If an old debug build is installed, Android may require uninstalling it first.
This resets destination settings. Files saved in shared storage remain there.

## Known limitations

Text-only and link-only shares are unsupported. Custom folder behavior depends on
the storage provider. There is no transfer progress UI or persistent background
queue; Android terminating the app can interrupt a large copy.

## Verification

Release and debug builds and Android lint passed locally. APK signature, package
identity, alignment, and absence of requested permissions were checked. Physical
device installation and file-sharing tests have not yet been performed for this release.
