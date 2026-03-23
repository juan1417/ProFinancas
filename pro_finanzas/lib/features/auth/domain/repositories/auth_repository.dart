import '../entities/user.dart';

abstract class AuthRepository {
  Future<({User user, String accessToken, String refreshToken})> login({
    required String email,
    required String password,
  });

  Future<({User user, String accessToken, String refreshToken})> register({
    required String email,
    required String username,
    required String password,
  });

  Future<void> logout(String refreshToken);

  Future<User> getProfile();

  Future<User> updateProfile({String? firstName, String? lastName, String? username});

  Future<void> changePassword({
    required String oldPassword,
    required String newPassword,
  });
}
