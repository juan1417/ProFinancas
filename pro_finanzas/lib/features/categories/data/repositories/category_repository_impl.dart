import '../../domain/entities/category.dart';
import '../../domain/repositories/category_repository.dart';
import '../datasources/category_remote_datasource.dart';

class CategoryRepositoryImpl implements CategoryRepository {
  const CategoryRepositoryImpl(this._datasource);
  final CategoryRemoteDatasource _datasource;

  @override
  Future<List<Category>> list({String? type, bool? isActive}) =>
      _datasource.list(type: type, isActive: isActive);

  @override
  Future<Category> create({
    required String name,
    required String type,
    String? description,
  }) =>
      _datasource.create({
        'name': name,
        'type': type,
        if (description != null && description.isNotEmpty)
          'description': description,
        'is_active': true,
      });

  @override
  Future<Category> update({
    required int id,
    required String name,
    required String type,
    String? description,
    bool? isActive,
  }) =>
      _datasource.update(id, {
        'name': name,
        'type': type,
        if (description != null) 'description': description,
        if (isActive != null) 'is_active': isActive,
      });

  @override
  Future<void> delete(int id) => _datasource.delete(id);
}
