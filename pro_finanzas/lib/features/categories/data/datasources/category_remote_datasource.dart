import 'package:dio/dio.dart';
import '../../../../core/api/api_client.dart';
import '../../../../core/api/api_constants.dart';
import '../../../../core/errors/dio_error_mapper.dart';
import '../../../transactions/data/models/category_model.dart';

/// Talks to the backend categories endpoints.
///
/// Reuses the [CategoryModel] that lives in the transactions feature since
/// the wire format is identical. When categories get extra fields (icon,
/// color, ordering, etc.) this is the only place that needs to grow.
class CategoryRemoteDatasource {
  CategoryRemoteDatasource(this._client)
      : _errors = DioErrorMapper(ApiConstants.baseUrl);

  final ApiClient _client;
  final DioErrorMapper _errors;

  Future<List<CategoryModel>> list({String? type, bool? isActive}) async {
    try {
      final response = await _client.get(
        ApiConstants.categories,
        queryParameters: {
          if (type != null) 'type': type,
          if (isActive != null) 'is_active': isActive,
          'ordering': 'name',
        },
      );
      return (response.data as List)
          .map((e) => CategoryModel.fromJson(e as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      throw _errors.map(e);
    }
  }

  Future<CategoryModel> create(Map<String, dynamic> body) async {
    try {
      final response = await _client.post(ApiConstants.categories, data: body);
      return CategoryModel.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw _errors.map(e);
    }
  }

  Future<CategoryModel> update(int id, Map<String, dynamic> body) async {
    try {
      final response = await _client.patch(
        '${ApiConstants.categories}$id/',
        data: body,
      );
      return CategoryModel.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw _errors.map(e);
    }
  }

  Future<void> delete(int id) async {
    try {
      await _client.delete('${ApiConstants.categories}$id/');
    } on DioException catch (e) {
      throw _errors.map(e);
    }
  }
}
