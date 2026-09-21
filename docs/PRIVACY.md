# FileTuck privacy policy

This policy describes the source for FileTuck 2.0 in this repository.

FileTuck copies files explicitly shared with it to a destination selected on your
device. It has no account system, analytics, advertising, remote backend, or network
permission. File contents are read to make the requested copy. FileTuck does not
send file contents or usage data to the developer.

The destination mode, custom folder URI, and folder name are stored in private app
preferences. Android manages temporary access to shared source files and persistent
access to a selected destination folder. Cloud app backup is disabled with `android:allowBackup="false"`. On some Android
12+ devices, manufacturer-managed device-to-device migration can still transfer
app data; FileTuck 2.0 does not define explicit data-extraction rules. See
[Android backup behavior](https://developer.android.com/identity/data/autobackup).

Custom storage providers may offer cloud destinations and transfer files themselves.
Those providers operate under their own policies. Saved files in shared storage
are subject to Android’s access rules and the chosen provider’s behavior.

Changing a destination does not move saved files. Clearing app data or uninstalling
resets local preferences; copies saved outside app-private storage are not managed
or removed by FileTuck. Manage saved files using your file manager or storage provider.

GitHub handles visits, downloads, and issue submissions under GitHub’s own privacy
policy. Public issues are public: do not include private file contents or credentials.
