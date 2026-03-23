import 'package:dio/dio.dart';
import '../../../../core/api/api_client.dart';
import '../../../../core/api/api_constants.dart';
import '../../../../core/errors/app_exception.dart';
import '../models/category_model.dart';
import '../models/transaction_model.dart';

class TransactionRemoteDatasource {
  const TransactionRemoteDatasource(this._client);
  final ApiClient _client;

  Future<List<TransactionModel>> getTransactions({
    String? type,
    int? categoryId,
    String? search,
  }) async {
    try {
      final response = await _client.get(ApiConstants.transactions, queryParameters: {
        if (type != null) 'type': type,
        if (categoryId != null) 'category': categoryId,
        if (search != null) 'search': search,
      });
      return (response.data as List)
          .map((e) => TransactionModel.fromJson(e as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      throw _handleDioError(e);
    }
  }

  Future<TransactionModel> createTransaction(Map<String, dynamic> data) async {
    try {
      final response = await _client.post(ApiConstants.transactions, data: data);
      return TransactionModel.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw _handleDioError(e);
    }
  }

  Future<void> deleteTransaction(int id) async {
    try {
      await _client.delete('${ApiConstants.transactions}$id/');
    } on DioException catch (e) {
      throw _handleDioError(e);
    }
  }

  Future<Map<String, dynamic>> getSummary({
    String period = 'month',
    String? startDate,
    String? endDate,
  }) async {
    try {
      final response = await _client.get(
        ApiConstants.transactionsSummary,
        queryParameters: {
          'period': period,
          if (startDate != null) 'start_date': startDate,
          if (endDate != null) 'end_date': endDate,
        },
      );
      return response.data as Map<String, dynamic>;
    } on DioException catch (e) {
      throw _handleDioError(e);
    }
  }

  Future<List<CategoryModel>> getCategories({String? type, bool? isActive}) async {
    try {
      final response = await _client.get(ApiConstants.categories, queryParameters: {
        if (type != null) 'type': type,
        if (isActive != null) 'is_active': isActive,
      });
      return (response.data as List)
          .map((e) => CategoryModel.fromJson(e as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      throw _handleDioError(e);
    }
  }

  Future<CategoryModel> createCategory(Map<String, dynamic> data) async {
    try {
      final response = await _client.post(ApiConstants.categories, data: data);
      return CategoryModel.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw _handleDioError(e);
    }
  }

  AppException _handleDioError(DioException e) {
    final statusCode = e.response?.statusCode;
    if (statusCode == 401) return const AuthException();
    if (statusCode == 400) {
      final data = e.response?.data;
      if (data is Map) {
        final first = data.values.first;
        final msg = first is List ? first.first.toString() : first.toString();
        return ValidationException(msg);
      }
    }
    if (statusCode == null) return const NetworkException();
    return const ServerException();
  }
}
