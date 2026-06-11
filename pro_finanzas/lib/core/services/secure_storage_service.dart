import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class SecureStorageService {
  SecureStorageService._();

  static const _storage = FlutterSecureStorage(
    aOptions: AndroidOptions(encryptedSharedPreferences: true),
  );

  static const String _keyEmail = 'biometric_email';
  static const String _keyPassword = 'biometric_password';

  static Future<void> saveBiometricCredentials({
    required String email,
    required String password,
  }) async {
    try {
      await _storage.write(key: _keyEmail, value: email);
      await _storage.write(key: _keyPassword, value: password);
    } catch (_) {
      // Ignore errors on platforms that don't support secure storage
    }
  }

  static Future<Map<String, String>?> getBiometricCredentials() async {
    try {
      final email = await _storage.read(key: _keyEmail);
      final password = await _storage.read(key: _keyPassword);
      if (email == null || password == null) return null;
      return {'email': email, 'password': password};
    } catch (_) {
      return null;
    }
  }

  static Future<void> clearBiometricCredentials() async {
    try {
      await _storage.delete(key: _keyEmail);
      await _storage.delete(key: _keyPassword);
    } catch (_) {
      // Ignore errors
    }
  }

  static Future<bool> hasBiometricCredentials() async {
    try {
      final creds = await getBiometricCredentials();
      return creds != null;
    } catch (_) {
      return false;
    }
  }
}