import '../../../../core/api/api_client.dart';
import '../../../../core/api/api_constants.dart';
import '../../../../core/errors/app_exception.dart';
import '../../../../core/errors/dio_error_mapper.dart';
import '../models/user_model.dart';
import 'package:dio/dio.dart';

class AuthRemoteDatasource {
  AuthRemoteDatasource(this._client)
      : _errors = DioErrorMapper(ApiConstants.baseUrl);
  final ApiClient _client;
  final DioErrorMapper _errors;

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
      throw _errors.map(e);
    }
  }

  Future<UserModel> updateProfile({
    String? firstName,
    String? lastName,
    String? username,
  }) async {
    // Only send fields the caller actually wants to change. The backend
    // treats omitted fields as "keep existing".
    final body = <String, dynamic>{};
    if (firstName != null) body['first_name'] = firstName;
    if (lastName != null) body['last_name'] = lastName;
    if (username != null) body['username'] = username;
    try {
      final response = await _client.patch(ApiConstants.profile, data: body);
      return UserModel.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw _errors.map(e);
    }
  }

  Future<void> changePassword({
    required String oldPassword,
    required String newPassword,
  }) async {
    try {
      await _client.post(ApiConstants.changePassword, data: {
        'old_password': oldPassword,
        'new_password': newPassword,
        'new_password_confirm': newPassword,
      });
    } on DioException catch (e) {
      throw _errors.map(e);
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
