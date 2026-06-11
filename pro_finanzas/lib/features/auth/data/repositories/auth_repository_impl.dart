import '../../domain/entities/user.dart';
import '../../domain/repositories/auth_repository.dart';
import '../datasources/auth_remote_datasource.dart';

class AuthRepositoryImpl implements AuthRepository {
  const AuthRepositoryImpl(this._datasource);
  final AuthRemoteDatasource _datasource;

  @override
  Future<({User user, String accessToken, String refreshToken})> login({
    required String email,
    required String password,
  }) =>
      _datasource.login(email: email, password: password);

  @override
  Future<({User user, String accessToken, String refreshToken})> register({
    required String email,
    required String username,
    required String password,
  }) =>
      _datasource.register(email: email, username: username, password: password);

  @override
  Future<void> logout(String refreshToken) => _datasource.logout(refreshToken);

  @override
  Future<User> getProfile() => _datasource.getProfile();

  @override
  Future<User> updateProfile({String? firstName, String? lastName, String? username}) =>
      _datasource.updateProfile(
        firstName: firstName,
        lastName: lastName,
        username: username,
      );

  @override
  Future<void> changePassword({required String oldPassword, required String newPassword}) =>
      _datasource.changePassword(
        oldPassword: oldPassword,
        newPassword: newPassword,
      );
}
