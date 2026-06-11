/// Maps a [DioException] to a user-friendly [AppException] with a message
/// that actually tells the user what went wrong.
///
/// The previous implementation collapsed every non-2xx response (and any
/// transport-level failure) into the generic
///   "Error de conexión. Verifica tu red."
/// which left users (and us) guessing whether the backend was down, the
/// URL was wrong, cleartext HTTP was blocked, or the request just timed out.
///
/// This mapper inspects [DioException.type] and the response to produce a
/// specific, actionable message.
library;

import 'package:dio/dio.dart';

import 'app_exception.dart';

class DioErrorMapper {
  const DioErrorMapper(this.baseUrl);

  /// Base URL of the API, used in error messages so the user knows which
  /// host/port the client was trying to reach.
  final String baseUrl;

  AppException map(DioException e) {
    final status = e.response?.statusCode;
    if (status == 401) return const AuthException();
    if (status == 400) {
      return ValidationException(_extractDetail(e.response?.data));
    }
    if (status == 403) {
      return const AuthException('No tienes permisos para esta acción.');
    }
    if (status == 404) {
      return NotFoundException(
        'Endpoint no encontrado en $baseUrl. '
        '¿Está corriendo el backend en el puerto correcto?',
      );
    }
    if (status != null && status >= 500) {
      return ServerException(
        'El servidor devolvió $status. Revisa los logs del backend.',
      );
    }

    // No HTTP response — a transport-level failure. The user almost
    // certainly sees this as "no tienes conexión"; here we tell them
    // which of the common causes is most likely.
    return NetworkException(_transportMessage(e));
  }

  String _transportMessage(DioException e) {
    final type = e.type;
    final uri = e.requestOptions.uri;
    switch (type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
        return 'Tiempo de espera agotado al conectar con $uri. '
            '¿Está corriendo el backend?';
      case DioExceptionType.connectionError:
        return 'No se pudo conectar a $uri. '
            'Verifica que el servidor esté corriendo y que el '
            'puerto/host sean correctos. '
            '(baseUrl=$baseUrl)';
      case DioExceptionType.badCertificate:
        return 'Certificado TLS inválido al conectar a $uri.';
      case DioExceptionType.cancel:
        return 'La petición fue cancelada.';
      case DioExceptionType.badResponse:
        // Should have been handled by the status code branches above.
        return 'Respuesta inválida del servidor ($uri).';
      case DioExceptionType.unknown:
        if (e.error != null) {
          return 'Error de red: ${e.error} (intentando $uri). '
              'Si estás en Android, asegúrate de que '
              '`usesCleartextTraffic` esté habilitado para HTTP en '
              'debug, o usa HTTPS.';
        }
        return 'Error de red desconocido al conectar con $uri '
            '(baseUrl=$baseUrl).';
    }
  }

  String _extractDetail(dynamic data) {
    if (data is Map) {
      // DRF validation errors are typically a map of field -> [strings].
      // Pick the first error message we can find so the user sees
      // something specific (e.g. "Este campo es requerido.").
      for (final value in data.values) {
        if (value is List && value.isNotEmpty) return value.first.toString();
        if (value is String) return value;
      }
      if (data['detail'] is String) return data['detail'] as String;
    }
    return data?.toString() ?? 'Error de validación.';
  }
}
