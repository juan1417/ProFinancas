import 'package:flutter/foundation.dart';
import '../../domain/entities/user.dart';
import '../../domain/repositories/auth_repository.dart';
import '../../domain/usecases/login_usecase.dart';
import '../../domain/usecases/register_usecase.dart';
import '../../domain/usecases/logout_usecase.dart';
import '../../../../core/api/api_client.dart';

class AuthProvider extends ChangeNotifier {
  AuthProvider({
    required LoginUseCase loginUseCase,
    required RegisterUseCase registerUseCase,
    required LogoutUseCase logoutUseCase,
    required AuthRepository repository,
  })  : _login = loginUseCase,
        _register = registerUseCase,
        _logout = logoutUseCase,
        _repository = repository;

  final LoginUseCase _login;
  final RegisterUseCase _register;
  final LogoutUseCase _logout;
  final AuthRepository _repository;

  User? currentUser;
  String? _refreshToken;
  bool isLoading = false;
  String? error;

  bool get isAuthenticated => currentUser != null;

  Future<void> login({required String email, required String password}) async {
    _setLoading(true);
    try {
      final result = await _login(email: email, password: password);
      _setSession(result.user, result.accessToken, result.refreshToken);
    } catch (e) {
      error = e.toString();
    } finally {
      _setLoading(false);
    }
  }

  Future<void> register({
    required String email,
    required String username,
    required String password,
  }) async {
    _setLoading(true);
    try {
      final result = await _register(
          email: email, username: username, password: password);
      _setSession(result.user, result.accessToken, result.refreshToken);
    } catch (e) {
      error = e.toString();
    } finally {
      _setLoading(false);
    }
  }

  Future<void> logout() async {
    if (_refreshToken == null) return;
    try {
      await _logout(_refreshToken!);
    } finally {
      currentUser = null;
      _refreshToken = null;
      ApiClient.instance.clearToken();
      notifyListeners();
    }
  }

  void _setSession(User user, String accessToken, String refreshToken) {
    currentUser = user;
    _refreshToken = refreshToken;
    ApiClient.instance.setAccessToken(accessToken);
    error = null;
  }

  void _setLoading(bool value) {
    isLoading = value;
    notifyListeners();
  }

  /// Patches the current user's profile and updates the local `currentUser`
  /// on success. Pass `null` for any field you want to leave untouched.
  Future<bool> updateProfile({
    String? firstName,
    String? lastName,
    String? username,
  }) async {
    if (currentUser == null) {
      error = 'No active session.';
      notifyListeners();
      return false;
    }
    _setLoading(true);
    try {
      final updated = await _repository.updateProfile(
        firstName: firstName,
        lastName: lastName,
        username: username,
      );
      currentUser = updated;
      error = null;
      return true;
    } catch (e) {
      error = e.toString();
      return false;
    } finally {
      _setLoading(false);
    }
  }

  /// Changes the password of the currently authenticated user. Returns
  /// `true` on success; the error is available on [error] for the caller
  /// to show in a snackbar/dialog.
  Future<bool> changePassword({
    required String oldPassword,
    required String newPassword,
  }) async {
    if (currentUser == null) {
      error = 'No active session.';
      notifyListeners();
      return false;
    }
    _setLoading(true);
    try {
      await _repository.changePassword(
        oldPassword: oldPassword,
        newPassword: newPassword,
      );
      error = null;
      return true;
    } catch (e) {
      error = e.toString();
      return false;
    } finally {
      _setLoading(false);
    }
  }
}
