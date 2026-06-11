import '../entities/category.dart';
import '../repositories/category_repository.dart';

class CreateCategoryUseCase {
  const CreateCategoryUseCase(this._repository);
  final CategoryRepository _repository;

  Future<Category> call({
    required String name,
    required String type,
    String? description,
  }) =>
      _repository.create(name: name, type: type, description: description);
}
