// VM implementation (Android/iOS/Windows/macOS/Linux). `dart:io` is fine
// here; on Android emulators the host loopback is reachable via `10.0.2.2`.
import 'dart:io' show Platform;

String defaultHost() =>
    Platform.isAndroid ? '10.0.2.2:8000' : 'localhost:8000';
