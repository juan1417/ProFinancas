import '../../domain/entities/transaction.dart';

class TransactionModel extends Transaction {
  const TransactionModel({
    required super.id,
    required super.type,
    required super.amount,
    required super.description,
    required super.categoryId,
    required super.categoryName,
    required super.transactionDate,
    super.notes,
  });

  factory TransactionModel.fromJson(Map<String, dynamic> json) => TransactionModel(
        id: json['id'] as int,
        type: json['type'] as String,
        amount: double.parse(json['amount'].toString()),
        description: json['description'] as String,
        categoryId: json['category'] as int? ?? 0,
        categoryName: json['category_name'] as String? ?? '',
        transactionDate: DateTime.parse(json['transaction_date'] as String),
        notes: json['notes'] as String?,
      );

  Map<String, dynamic> toJson() => {
        'type': type,
        'amount': amount.toStringAsFixed(2),
        'description': description,
        'category': categoryId,
        'transaction_date': transactionDate.toIso8601String(),
        if (notes != null) 'notes': notes,
      };
}
