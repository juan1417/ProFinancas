class AppException implements Exception {
  const AppException(this.message);
  final String message;

  @override
  String toString() => message;
}

class NetworkException extends AppException {
  const NetworkException([String message = 'Error de conexión. Verifica tu red.'])
      : super(message);
}

class AuthException extends AppException {
  const AuthException([String message = 'No autorizado. Por favor inicia sesión.'])
      : super(message);
}

class ValidationException extends AppException {
  const ValidationException(super.message);
  // message contains the first DRF validation error string
}

class NotFoundException extends AppException {
  const NotFoundException([String message = 'Recurso no encontrado.'])
      : super(message);
}

class ServerException extends AppException {
  const ServerException([String message = 'Error interno del servidor.'])
      : super(message);
}
