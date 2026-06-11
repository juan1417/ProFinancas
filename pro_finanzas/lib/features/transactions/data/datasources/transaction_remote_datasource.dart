import 'package:dio/dio.dart';
import '../../../../core/api/api_client.dart';
import '../../../../core/api/api_constants.dart';
import '../../../../core/errors/dio_error_mapper.dart';
import '../models/transaction_model.dart';

/// Talks to the transactions + summary backend endpoints.
///
/// Categories are NOT served from here — they live in their own
/// datasource at lib/features/categories/.../category_remote_datasource.dart.
class TransactionRemoteDatasource {
  TransactionRemoteDatasource(this._client)
      : _errors = DioErrorMapper(ApiConstants.baseUrl);

  final ApiClient _client;
  final DioErrorMapper _errors;

  Future<List<TransactionModel>> getTransactions({
    String? type,
    int? categoryId,
    String? search,
  }) async {
    try {
      final response = await _client.get(
        ApiConstants.transactions,
        queryParameters: {
          if (type != null) 'type': type,
          if (categoryId != null) 'category': categoryId,
          if (search != null) 'search': search,
        },
      );
      return (response.data as List)
          .map((e) => TransactionModel.fromJson(e as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      throw _errors.map(e);
    }
  }

  Future<TransactionModel> createTransaction(Map<String, dynamic> data) async {
    try {
      final response = await _client.post(ApiConstants.transactions, data: data);
      return TransactionModel.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw _errors.map(e);
    }
  }

  Future<void> deleteTransaction(int id) async {
    try {
      await _client.delete('${ApiConstants.transactions}$id/');
    } on DioException catch (e) {
      throw _errors.map(e);
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
      throw _errors.map(e);
    }
  }
}
