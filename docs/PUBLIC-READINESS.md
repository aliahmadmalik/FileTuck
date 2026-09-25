# Public release readiness review

Reviewed 21 September 2026 for FileTuck 2.0 (version code 6).

## Assessment

FileTuck is reasonable to distribute as an **early public Android utility**, with
its testing limits clearly disclosed. This review does not establish production
readiness across devices and storage providers, and is not an independent security
audit. The downloadable 2.0 APK has not been changed by this documentation update.

## Checks and findings

| Area | Finding |
| --- | --- |
| APK distribution | Signed release APK, SHA-256 checksum, and public certificate available on GitHub. |
| Signing identity | Project-only certificate subject `CN=FileTuck`; private key/password outside the repository. |
| Permissions | Release manifest requests no permissions; content access uses Android URI grants. |
| Runtime dependencies | Android framework code; no third-party runtime libraries, ad SDKs, or analytics SDKs. |
| File access | `content://` sources only; raw filesystem shares rejected. Provider filenames have path separators removed. |
| Failure handling | Copy errors are caught; incomplete destination cleanup is attempted. Process death can still interrupt work. |
| Source privacy | Tracked source and reachable published history checked for common credential patterns and private paths. Pattern scanning is not exhaustive. |
| Repository protection | Secret scanning, push protection, private vulnerability reporting, and dependency security alerts enabled. |
| Automation | Build/lint checks; Actions pinned to full commits; read-only token; no signing secrets in CI. |
| Documentation | Direct APK link, requirements, install/update instructions, privacy, limitations, troubleshooting, and support channels. |
| Licensing | Updated 25 September 2026: source code, documentation, and original artwork licensed under Apache-2.0 with owner authorization; see LICENSE. |
| Device testing | No connected device available during this review; installation and real file-sharing behavior remain unverified. |

## Work needed before broad promotion

1. **Test the signed APK on devices.** At minimum, cover Android 10 and a recent
   Android version, each destination, multiple files, duplicate names, revoked
   grants, low storage, and an upgrade signed with the same key. Record the actual
   device/provider results rather than claiming general compatibility.
2. **Add input and lifecycle regression tests.** Exercise malformed parcelable
   extras and invalid share payloads: intent parsing currently assumes correctly
   typed URI extras. Also exercise activity recreation and interrupted transfers.
   Introduce defensive parsing if those tests expose failures. Lint does not test
   these behaviors.
3. **Improve transfer resilience for large files.** The current activity-owned
   worker has no persistent job, progress display, cancellation, or retry state.
   Documented limitations are appropriate for an early release; a durable transfer
   design needs explicit UX and device validation before promising background saves.
4. **Make backup exclusions explicit if strict migration privacy is required.**
   `allowBackup=false` disables cloud backup, but some manufacturers still allow
   device-to-device transfer. Add and test Android 12+ extraction rules in a future
   app update. The privacy policy now describes 2.0 accurately. [Android guidance](https://developer.android.com/identity/data/autobackup)
5. **Review Android developer registration.** Google's published schedule currently
   lists 30 September 2026 for participating stores in Brazil, Indonesia, Singapore,
   and Thailand, followed by broader rollout in 2027. Do not treat an APK signature
   as developer verification or promise unrestricted installation on every device.
   Registration and any identity checks must be completed by the account owner.
   [Official verification guidance](https://developer.android.com/developer-verification)
6. **Publish the licensed source release for F-Droid.** Apache-2.0 was selected
   on 25 September 2026 with owner authorization. The new license and metadata
   still need to be included in a published release tag; see FDROID.md.

## Other quality improvements

The release lint report contains 12 warnings and no errors: fixed orientation,
plural/translation handling, backup configuration, icon variants, and unused
resources. These are useful follow-up work, especially localization and adaptable
layouts. Add genuine device screenshots only after testing; no mock screenshot is
presented as evidence of runtime behavior.

## GitHub search and README presentation

The repository description and topics describe actual capabilities: Android,
share sheet, file sharing, Downloads, and Storage Access Framework. The README
uses a descriptive title, concise introduction, clear sections, meaningful image
alt text, and links to release/support information. It avoids unsupported claims,
keyword stuffing and unsupported security claims.

GitHub controls page metadata and search-engine indexing; a README cannot set the
page's HTML SEO meta tags or guarantee rankings. The project has no separate
marketing website. The changes improve clarity and discoverability within that scope.

## References used

- [GitHub repository best practices](https://docs.github.com/en/repositories/creating-and-managing-repositories/best-practices-for-repositories)
- [GitHub Actions secure use](https://docs.github.com/en/actions/reference/security/secure-use)
- [Android content-resolver security](https://developer.android.com/privacy-and-security/risks/content-resolver)
- [Android backup behavior](https://developer.android.com/identity/data/autobackup)
- [Android developer verification](https://developer.android.com/developer-verification)

The owner was separately informed of an old cached commit containing a personal
email address. Rewriting current history does not purge GitHub's cached commits;
server-side removal is a separate GitHub Support request, not something this review
can certify as completed.
