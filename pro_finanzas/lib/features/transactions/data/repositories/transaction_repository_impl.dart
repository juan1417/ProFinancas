import '../../domain/entities/transaction.dart';
import '../../domain/repositories/transaction_repository.dart';
import '../datasources/transaction_remote_datasource.dart';
import '../models/transaction_model.dart';

class TransactionRepositoryImpl implements TransactionRepository {
  const TransactionRepositoryImpl(this._datasource);
  final TransactionRemoteDatasource _datasource;

  @override
  Future<List<Transaction>> getTransactions({
    String? type,
    int? categoryId,
    String? search,
  }) =>
      _datasource.getTransactions(type: type, categoryId: categoryId, search: search);

  @override
  Future<Transaction> createTransaction({
    required int categoryId,
    required String type,
    required double amount,
    required String description,
    required DateTime transactionDate,
    String? notes,
  }) =>
      _datasource.createTransaction(
        TransactionModel(
          id: 0,
          categoryId: categoryId,
          type: type,
          amount: amount,
          description: description,
          categoryName: '',
          transactionDate: transactionDate,
          notes: notes,
        ).toJson(),
      );

  @override
  Future<void> deleteTransaction(int id) => _datasource.deleteTransaction(id);

  @override
  Future<Map<String, dynamic>> getSummary({
    String period = 'month',
    String? startDate,
    String? endDate,
  }) =>
      _datasource.getSummary(period: period, startDate: startDate, endDate: endDate);
}
