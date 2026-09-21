#!/usr/bin/env bash
# Package only an already signed release; no key/password handling or uploading.
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd)"
if [[ $# -ne 1 ]]; then
  echo "Usage: $0 /absolute/path/to/signed-release.apk" >&2
  exit 1
fi
: "${ANDROID_HOME:?Set ANDROID_HOME to your Android SDK directory}"
tools="$ANDROID_HOME/build-tools/34.0.0"
for tool in apksigner aapt zipalign; do
  [[ -x "$tools/$tool" ]] || { echo "Missing Android build tool: $tool" >&2; exit 1; }
done
[[ -f "$1" ]] || { echo 'APK not found' >&2; exit 1; }
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
cp "$1" "$tmp/FileTuck-2.0.apk"
"$tools/apksigner" verify --verbose --print-certs "$tmp/FileTuck-2.0.apk" > "$tmp/SIGNING-CERTIFICATE.txt"
if grep -qi 'Android Debug' "$tmp/SIGNING-CERTIFICATE.txt"; then
  echo 'Refusing an Android debug signing certificate.' >&2; exit 1
fi
"$tools/aapt" dump badging "$tmp/FileTuck-2.0.apk" > "$tmp/badging.txt"
grep -q "package: name='com.aliahmad.savetodownloads' versionCode='6' versionName='2.0'" "$tmp/badging.txt"
if grep -q 'application-debuggable' "$tmp/badging.txt"; then
  echo 'Refusing a debuggable APK.' >&2; exit 1
fi
if "$tools/aapt" dump permissions "$tmp/FileTuck-2.0.apk" | grep -q 'uses-permission'; then
  echo 'Unexpected permissions: review before release.' >&2; exit 1
fi
"$tools/zipalign" -c -v 4 "$tmp/FileTuck-2.0.apk" > /dev/null
(cd "$tmp" && shasum -a 256 FileTuck-2.0.apk > SHA256SUMS)
mkdir -p "$root/release"
for asset in FileTuck-2.0.apk SHA256SUMS SIGNING-CERTIFICATE.txt; do
  [[ ! -e "$root/release/$asset" ]] || { echo "Existing release/$asset: move it before packaging again." >&2; exit 1; }
done
for asset in FileTuck-2.0.apk SHA256SUMS SIGNING-CERTIFICATE.txt; do
  cp "$tmp/$asset" "$root/release/$asset"
done
echo 'Verified assets are in release/. Review the certificate identity before uploading.'
