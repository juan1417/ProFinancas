import '../../domain/entities/category.dart';

class CategoryModel extends Category {
  const CategoryModel({
    required super.id,
    required super.name,
    required super.type,
    super.description,
    super.isActive,
    super.transactionsCount,
  });

  factory CategoryModel.fromJson(Map<String, dynamic> json) => CategoryModel(
        id: json['id'] as int,
        name: json['name'] as String,
        type: json['type'] as String,
        description: json['description'] as String?,
        isActive: json['is_active'] as bool? ?? true,
        transactionsCount: json['transactions_count'] as int? ?? 0,
      );

  Map<String, dynamic> toJson() => {
        'name': name,
        'type': type,
        'description': description,
        'is_active': isActive,
      };
}
