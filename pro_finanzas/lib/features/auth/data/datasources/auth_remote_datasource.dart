import '../../../../core/api/api_client.dart';
import '../../../../core/api/api_constants.dart';
import '../../../../core/errors/app_exception.dart';
import '../models/user_model.dart';
import 'package:dio/dio.dart';

class AuthRemoteDatasource {
  const AuthRemoteDatasource(this._client);
  final ApiClient _client;

  Future<({UserModel user, String accessToken, String refreshToken})> login({
    required String email,
    required String password,
  }) async {
    try {
      final response = await _client.post(ApiConstants.login, data: {
        'email': email,
        'password': password,
      });
      return _parseAuthResponse(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw _handleDioError(e);
    }
  }

  Future<({UserModel user, String accessToken, String refreshToken})> register({
    required String email,
    required String username,
    required String password,
  }) async {
    try {
      final response = await _client.post(ApiConstants.register, data: {
        'email': email,
        'username': username,
        'password': password,
      });
      return _parseAuthResponse(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw _handleDioError(e);
    }
  }

  Future<void> logout(String refreshToken) async {
    try {
      await _client.post(ApiConstants.logout, data: {'refresh': refreshToken});
    } on DioException catch (e) {
      throw _handleDioError(e);
    }
  }

  Future<UserModel> getProfile() async {
    try {
      final response = await _client.get(ApiConstants.profile);
      return UserModel.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw _handleDioError(e);
    }
  }

  ({UserModel user, String accessToken, String refreshToken}) _parseAuthResponse(
    Map<String, dynamic> data,
  ) {
    final tokens = data['tokens'] as Map<String, dynamic>;
    return (
      user: UserModel.fromJson(data['user'] as Map<String, dynamic>),
      accessToken: tokens['access'] as String,
      refreshToken: tokens['refresh'] as String,
    );
  }

  AppException _handleDioError(DioException e) {
    final statusCode = e.response?.statusCode;
    if (statusCode == 401) return const AuthException();
    if (statusCode == 400) {
      final detail = _extractDetail(e.response?.data);
      return ValidationException(detail);
    }
    if (statusCode == null) return const NetworkException();
    return const ServerException();
  }

  String _extractDetail(dynamic data) {
    if (data is Map) {
      final first = data.values.first;
      if (first is List && first.isNotEmpty) return first.first.toString();
      return first.toString();
    }
    return data?.toString() ?? 'Error desconocido';
  }
}
