import '../entities/transaction.dart';
import '../entities/category.dart';

abstract class TransactionRepository {
  Future<List<Transaction>> getTransactions({
    String? type,
    int? categoryId,
    String? search,
  });

  Future<Transaction> createTransaction({
    required int categoryId,
    required String type,
    required double amount,
    required String description,
    required DateTime transactionDate,
    String? notes,
  });

  Future<void> deleteTransaction(int id);

  Future<Map<String, dynamic>> getSummary({
    String period = 'month',
    String? startDate,
    String? endDate,
  });

  Future<List<Category>> getCategories({String? type, bool? isActive});

  Future<Category> createCategory({
    required String name,
    required String type,
    String? description,
  });
}
