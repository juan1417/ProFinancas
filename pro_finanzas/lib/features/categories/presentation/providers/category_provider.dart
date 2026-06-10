// `flutter/foundation.dart` exports a `Category` annotation, which collides
// with our domain `Category` entity. Hide the foundation one.
import 'package:flutter/foundation.dart' hide Category;
import '../../domain/entities/category.dart';
import '../../domain/usecases/create_category_usecase.dart';
import '../../domain/usecases/delete_category_usecase.dart';
import '../../domain/usecases/list_categories_usecase.dart';
import '../../domain/usecases/update_category_usecase.dart';

/// State container for the categories feature.
///
/// Holds the full list (both INCOME and EXPENSE) and exposes
/// [incomeCategories] / [expenseCategories] getters so the UI doesn't have
/// to filter every build.
///
/// CRUD operations optimistically update the local list on success so the
/// screen reflects changes immediately without a re-fetch.
class CategoryProvider extends ChangeNotifier {
  CategoryProvider({
    required ListCategoriesUseCase listCategories,
    required CreateCategoryUseCase createCategory,
    required UpdateCategoryUseCase updateCategory,
    required DeleteCategoryUseCase deleteCategory,
  })  : _list = listCategories,
        _create = createCategory,
        _update = updateCategory,
        _delete = deleteCategory;

  final ListCategoriesUseCase _list;
  final CreateCategoryUseCase _create;
  final UpdateCategoryUseCase _update;
  final DeleteCategoryUseCase _delete;

  List<Category> _all = const [];
  bool isLoading = false;
  String? error;

  List<Category> get all => _all;
  List<Category> get incomeCategories =>
      _all.where((c) => c.isIncome).toList(growable: false);
  List<Category> get expenseCategories =>
      _all.where((c) => c.isExpense).toList(growable: false);

  Future<void> loadAll({String? type, bool? isActive}) async {
    _setLoading(true);
    try {
      _all = await _list(type: type, isActive: isActive);
      error = null;
    } catch (e) {
      error = e.toString();
    } finally {
      _setLoading(false);
    }
  }

  Future<Category?> create({
    required String name,
    required String type,
    String? description,
  }) async {
    _setLoading(true);
    try {
      final created = await _create(
        name: name,
        type: type,
        description: description,
      );
      _all = [..._all, created];
      error = null;
      return created;
    } catch (e) {
      error = e.toString();
      return null;
    } finally {
      _setLoading(false);
    }
  }

  Future<Category?> update({
    required int id,
    required String name,
    required String type,
    String? description,
    bool? isActive,
  }) async {
    _setLoading(true);
    try {
      final updated = await _update(
        id: id,
        name: name,
        type: type,
        description: description,
        isActive: isActive,
      );
      _all = _all.map((c) => c.id == id ? updated : c).toList(growable: false);
      error = null;
      return updated;
    } catch (e) {
      error = e.toString();
      return null;
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> delete(int id) async {
    _setLoading(true);
    try {
      await _delete(id);
      _all = _all.where((c) => c.id != id).toList(growable: false);
      error = null;
      return true;
    } catch (e) {
      error = e.toString();
      return false;
    } finally {
      _setLoading(false);
    }
  }

  void _setLoading(bool value) {
    isLoading = value;
    notifyListeners();
  }
}
