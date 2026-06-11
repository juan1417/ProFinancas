import '../entities/category.dart';
import '../repositories/category_repository.dart';

class UpdateCategoryUseCase {
  const UpdateCategoryUseCase(this._repository);
  final CategoryRepository _repository;

  Future<Category> call({
    required int id,
    required String name,
    required String type,
    String? description,
    bool? isActive,
  }) =>
      _repository.update(
        id: id,
        name: name,
        type: type,
        description: description,
        isActive: isActive,
      );
}
