import 'package:flutter/foundation.dart';
import '../../domain/entities/user.dart';
import '../../domain/usecases/login_usecase.dart';
import '../../domain/usecases/register_usecase.dart';
import '../../domain/usecases/logout_usecase.dart';
import '../../../../core/api/api_client.dart';

class AuthProvider extends ChangeNotifier {
  AuthProvider({
    required LoginUseCase loginUseCase,
    required RegisterUseCase registerUseCase,
    required LogoutUseCase logoutUseCase,
  })  : _login = loginUseCase,
        _register = registerUseCase,
        _logout = logoutUseCase;

  final LoginUseCase _login;
  final RegisterUseCase _register;
  final LogoutUseCase _logout;

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
}
