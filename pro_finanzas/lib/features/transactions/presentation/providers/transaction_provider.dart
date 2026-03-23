import 'package:flutter/foundation.dart';
import '../../domain/entities/transaction.dart';
import '../../domain/entities/category.dart';
import '../../domain/usecases/get_transactions_usecase.dart';
import '../../domain/usecases/create_transaction_usecase.dart';
import '../../domain/usecases/get_summary_usecase.dart';

class TransactionProvider extends ChangeNotifier {
  TransactionProvider({
    required GetTransactionsUseCase getTransactions,
    required CreateTransactionUseCase createTransaction,
    required GetSummaryUseCase getSummary,
  })  : _getTransactions = getTransactions,
        _createTransaction = createTransaction,
        _getSummary = getSummary;

  final GetTransactionsUseCase _getTransactions;
  final CreateTransactionUseCase _createTransaction;
  final GetSummaryUseCase _getSummary;

  List<Transaction> transactions = [];
  List<Category> categories = [];
  Map<String, dynamic>? summary;
  bool isLoading = false;
  String? error;

  Future<void> loadTransactions({String? type}) async {
    _setLoading(true);
    try {
      transactions = await _getTransactions(type: type);
      error = null;
    } catch (e) {
      error = e.toString();
    } finally {
      _setLoading(false);
    }
  }

  Future<void> loadSummary({String period = 'month'}) async {
    _setLoading(true);
    try {
      summary = await _getSummary(period: period);
      error = null;
    } catch (e) {
      error = e.toString();
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> addTransaction({
    required int categoryId,
    required String type,
    required double amount,
    required String description,
    required DateTime transactionDate,
    String? notes,
  }) async {
    _setLoading(true);
    try {
      final tx = await _createTransaction(
        categoryId: categoryId,
        type: type,
        amount: amount,
        description: description,
        transactionDate: transactionDate,
        notes: notes,
      );
      transactions = [tx, ...transactions];
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
