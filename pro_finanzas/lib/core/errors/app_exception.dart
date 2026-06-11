class AppException implements Exception {
  const AppException(this.message);
  final String message;

  @override
  String toString() => message;
}

class NetworkException extends AppException {
  const NetworkException([super.message = 'Error de conexión. Verifica tu red.']);
}

class AuthException extends AppException {
  const AuthException([super.message = 'No autorizado. Por favor inicia sesión.']);
}

class ValidationException extends AppException {
  const ValidationException(super.message);
  // message contains the first DRF validation error string
}

class NotFoundException extends AppException {
  const NotFoundException([super.message = 'Recurso no encontrado.']);
}

class ServerException extends AppException {
  const ServerException([super.message = 'Error interno del servidor.']);
}
