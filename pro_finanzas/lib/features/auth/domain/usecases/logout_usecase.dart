import '../repositories/auth_repository.dart';

class LogoutUseCase {
  const LogoutUseCase(this._repository);
  final AuthRepository _repository;

  Future<void> call(String refreshToken) => _repository.logout(refreshToken);
}
