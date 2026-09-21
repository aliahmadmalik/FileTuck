#!/usr/bin/env bash
# Verify and package a signed APK from this source version. Never handles private keys.
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd)"
if [[ $# -ne 1 ]]; then
  echo "Usage: $0 /absolute/path/to/signed-release.apk" >&2
  exit 1
fi
: "${ANDROID_HOME:?Set ANDROID_HOME to your Android SDK directory}"
config="$root/app/build.gradle.kts"
version="$(sed -nE 's/^[[:space:]]*versionName = "([^"]+)".*/\1/p' "$config")"
code="$(sed -nE 's/^[[:space:]]*versionCode = ([0-9]+).*/\1/p' "$config")"
build_tools="${ANDROID_BUILD_TOOLS_VERSION:-$(sed -nE 's/^[[:space:]]*buildToolsVersion = "([^"]+)".*/\1/p' "$config")}"
[[ "$version" =~ ^[0-9A-Za-z][0-9A-Za-z._-]*$ && "$code" =~ ^[0-9]+$ && -n "$build_tools" ]] || {
  echo 'Could not read release version or Build Tools version from app/build.gradle.kts.' >&2; exit 1;
}
tools="$ANDROID_HOME/build-tools/$build_tools"
for tool in apksigner aapt zipalign; do
  [[ -x "$tools/$tool" ]] || {
    echo "Missing $tool in Build Tools $build_tools. Install that version or set ANDROID_BUILD_TOOLS_VERSION to an installed version." >&2; exit 1;
  }
done
if command -v sha256sum >/dev/null 2>&1; then
  checksum=(sha256sum)
elif command -v shasum >/dev/null 2>&1; then
  checksum=(shasum -a 256)
else
  echo 'Install sha256sum or shasum to calculate SHA-256 checksums.' >&2; exit 1
fi
[[ -f "$1" ]] || { echo 'APK not found' >&2; exit 1; }
expected_signer="$(tr -d '\r\n' < "$root/docs/SIGNING-CERTIFICATE.sha256")"
[[ "$expected_signer" =~ ^[0-9a-f]{64}$ ]] || { echo 'Invalid expected signing fingerprint.' >&2; exit 1; }
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
apk="FileTuck-$version.apk"
cp "$1" "$tmp/$apk"
"$tools/apksigner" verify --verbose --print-certs "$tmp/$apk" > "$tmp/SIGNING-CERTIFICATE.txt"
if ! grep -Fqx "Signer #1 certificate SHA-256 digest: $expected_signer" "$tmp/SIGNING-CERTIFICATE.txt" ||
   ! grep -Fqx 'Number of signers: 1' "$tmp/SIGNING-CERTIFICATE.txt"; then
  echo 'Refusing an APK that does not match the official FileTuck signing identity.' >&2; exit 1
fi
"$tools/aapt" dump badging "$tmp/$apk" > "$tmp/badging.txt"
grep -Fq "package: name='com.aliahmad.savetodownloads' versionCode='$code' versionName='$version'" "$tmp/badging.txt"
if grep -q 'application-debuggable' "$tmp/badging.txt"; then
  echo 'Refusing a debuggable APK.' >&2; exit 1
fi
permissions="$("$tools/aapt" dump permissions "$tmp/$apk")"
if grep -q 'uses-permission' <<< "$permissions"; then
  echo 'Unexpected permissions: review before release.' >&2; exit 1
fi
"$tools/zipalign" -c -v 4 "$tmp/$apk" > /dev/null
(cd "$tmp" && "${checksum[@]}" "$apk" > SHA256SUMS)
output="$root/release/$version"
[[ ! -e "$output" ]] || { echo "Existing release/$version: move it before packaging again." >&2; exit 1; }
mkdir -p "$root/release"
# Prepare the entire release set before making it visible at the output path.
bundle="$(mktemp -d "$root/release/.package-XXXXXX")"
trap 'rm -rf "$tmp" "${bundle:-}"' EXIT
for asset in "$apk" SHA256SUMS SIGNING-CERTIFICATE.txt; do cp "$tmp/$asset" "$bundle/$asset"; done
mv "$bundle" "$output"
echo "Verified assets are in release/$version/. Review before uploading."
