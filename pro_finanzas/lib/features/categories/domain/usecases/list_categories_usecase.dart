import '../entities/category.dart';
import '../repositories/category_repository.dart';

class ListCategoriesUseCase {
  const ListCategoriesUseCase(this._repository);
  final CategoryRepository _repository;

  Future<List<Category>> call({String? type, bool? isActive}) =>
      _repository.list(type: type, isActive: isActive);
}
