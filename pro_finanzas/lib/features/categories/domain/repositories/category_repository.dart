import '../../../transactions/domain/entities/category.dart';

/// Abstract contract the presentation layer talks to. Hides whether the data
/// comes from the backend, a local cache, or a mock implementation.
abstract class CategoryRepository {
  Future<List<Category>> list({String? type, bool? isActive});

  Future<Category> create({
    required String name,
    required String type,
    String? description,
  });

  Future<Category> update({
    required int id,
    required String name,
    required String type,
    String? description,
    bool? isActive,
  });

  Future<void> delete(int id);
}
