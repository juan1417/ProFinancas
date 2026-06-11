import 'package:flutter/foundation.dart' show kIsWeb;

import '_platform_io_stub.dart'
    if (dart.library.io) '_platform_io_vm.dart' as platform;

class ApiConstants {
  ApiConstants._();

  /// Default host for the Django dev server.
  /// - Web/Windows desktop: `localhost:8000`
  /// - Android emulator:    `10.0.2.2:8000`  (emulator alias for host loopback)
  /// - iOS simulator:       `localhost:8000`
  ///
  /// Override at build/run time with
  ///   `--dart-define=API_HOST=192.168.1.50:8000`
  /// (useful for physical devices that can't reach `localhost`).
  static const String _defaultHostWebDesktop = 'localhost:8000';

  /// Public base URL: `http://<host>/api`.
  ///
  /// The host is chosen at runtime based on the platform so the same
  /// code works on web, desktop, and Android emulator without edits.
  static String get baseUrl {
    const override = String.fromEnvironment('API_HOST', defaultValue: '');
    if (override.isNotEmpty) return 'http://$override/api';
    if (kIsWeb) return 'http://$_defaultHostWebDesktop/api';
    return 'http://${platform.defaultHost()}/api';
  }

  // Auth
  static const String register = '/auth/register/';
  static const String login = '/auth/login/';
  static const String logout = '/auth/logout/';
  static const String profile = '/auth/profile/';
  static const String changePassword = '/auth/change-password/';
  static const String tokenRefresh = '/auth/token/refresh/';

  // Categories
  static const String categories = '/categories/';
  static const String categoriesByType = '/categories/by_type/';

  // Transactions
  static const String transactions = '/transactions/';
  static const String transactionsSummary = '/transactions/summary/';
  static const String transactionsByCategory = '/transactions/by_category/';
}
