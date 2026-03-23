import '../entities/user.dart';
import '../repositories/auth_repository.dart';

class RegisterUseCase {
  const RegisterUseCase(this._repository);
  final AuthRepository _repository;

  Future<({User user, String accessToken, String refreshToken})> call({
    required String email,
    required String username,
    required String password,
  }) =>
      _repository.register(email: email, username: username, password: password);
}
