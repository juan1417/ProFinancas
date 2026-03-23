import 'package:dio/dio.dart';
import 'api_constants.dart';

class ApiClient {
  ApiClient._() : _dio = _buildDio();

  static final ApiClient instance = ApiClient._();

  final Dio _dio;
  String? _accessToken;

  static Dio _buildDio() {
    return Dio(BaseOptions(
      baseUrl: ApiConstants.baseUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
      headers: {'Content-Type': 'application/json'},
    ));
  }

  void setAccessToken(String token) {
    _accessToken = token;
    _dio.options.headers['Authorization'] = 'Bearer $token';
  }

  void clearToken() {
    _accessToken = null;
    _dio.options.headers.remove('Authorization');
  }

  bool get isAuthenticated => _accessToken != null;

  Future<Response> get(String path, {Map<String, dynamic>? queryParameters}) =>
      _dio.get(path, queryParameters: queryParameters);

  Future<Response> post(String path, {dynamic data}) =>
      _dio.post(path, data: data);

  Future<Response> put(String path, {dynamic data}) =>
      _dio.put(path, data: data);

  Future<Response> patch(String path, {dynamic data}) =>
      _dio.patch(path, data: data);

  Future<Response> delete(String path) => _dio.delete(path);
}
