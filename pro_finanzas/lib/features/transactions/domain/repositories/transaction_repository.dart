import '../entities/transaction.dart';

/// Abstract contract the presentation layer talks to. Hides whether the data
/// comes from the backend, a local cache, or a mock implementation.
///
/// Note: this repo only deals with transactions + summary. Categories
/// live in their own repo at lib/features/categories/.../category_repository.dart.
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
}
