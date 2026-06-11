// Web / non-VM stub. `dart:io` is not available here, so we default to
// `localhost` and rely on the caller to provide a real host via
// `--dart-define=API_HOST=...` if needed.
String defaultHost() => 'localhost:8000';
