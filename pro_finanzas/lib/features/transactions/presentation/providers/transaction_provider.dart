import 'package:flutter/foundation.dart' hide Category;
import '../../domain/entities/transaction.dart';
import '../../domain/entities/category.dart';
import '../../domain/repositories/transaction_repository.dart';
import '../../domain/usecases/get_transactions_usecase.dart';
import '../../domain/usecases/create_transaction_usecase.dart';
import '../../domain/usecases/get_summary_usecase.dart';

class TransactionProvider extends ChangeNotifier {
  TransactionProvider({
    required GetTransactionsUseCase getTransactions,
    required CreateTransactionUseCase createTransaction,
    required GetSummaryUseCase getSummary,
    TransactionRepository? repository,
  })  : _getTransactions = getTransactions,
        _createTransaction = createTransaction,
        _getSummary = getSummary,
        _repository = repository;

  final GetTransactionsUseCase _getTransactions;
  final CreateTransactionUseCase _createTransaction;
  final GetSummaryUseCase _getSummary;
  final TransactionRepository? _repository;

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

  Future<bool> createTransaction({
    required int categoryId,
    required String type,
    required double amount,
    required String description,
    required DateTime date,
    String? notes,
  }) =>
      addTransaction(
        categoryId: categoryId,
        type: type.toUpperCase(),
        amount: amount,
        description: description,
        transactionDate: date,
        notes: notes,
      );

  Future<void> loadCategories({String? type}) async {
    final repo = _repository;
    if (repo == null) return;
    try {
      categories = await repo.getCategories(type: type, isActive: true);
      notifyListeners();
    } catch (e) {
      error = e.toString();
    }
  }

  /// Normalized "spend by category" breakdown.
  ///
  /// The backend (`/transactions/by_category/`) returns rows shaped as
  ///   `{ category__id, category__name, category__type, total_amount, transaction_count }`
  /// but the UI wants `{ name, total, type }` for donut/bar charts. This getter
  /// accepts either shape and returns a stable list the screens can render
  /// without knowing where the data came from.
  List<Map<String, dynamic>> get byCategoryBreakdown {
    final raw = summary?['by_category'] as List<dynamic>?;
    if (raw == null) return const [];
    return raw.map((row) {
      final map = row as Map<String, dynamic>;
      // Backend shape (snake_case nested keys).
      final name = map['category__name'] as String? ?? map['name'] as String? ?? '';
      final total = (map['total_amount'] as num?) ?? (map['total'] as num?) ?? 0;
      final type = map['category__type'] as String? ?? map['type'] as String? ?? 'EXPENSE';
      return <String, dynamic>{'name': name, 'total': total, 'type': type};
    }).toList();
  }

  /// Returns the last [months] months of income/expense aggregated by
  /// calendar month, ordered oldest-to-newest. The first element of
  /// the returned list is [months] months back; the last one is the
  /// current month.
  ///
  /// The previous version of the analytics bar chart hardcoded the
  /// shape of the data (`income * (0.6 + i * 0.08)`) so the chart
  /// didn't reflect the user's actual finances. This getter computes
  /// the real per-month totals from the loaded transactions list.
  ///
  /// Each item is `{ month: int (1-12), year: int, label: 'Jan', income: double, expense: double }`.
  List<Map<String, dynamic>> monthlyTotals({int months = 6}) {
    assert(months > 0, 'months must be positive');
    final now = DateTime.now();
    // Build the bucket list oldest-to-newest.
    final buckets = <DateTime>[];
    for (var i = months - 1; i >= 0; i--) {
      final d = DateTime(now.year, now.month - i, 1);
      buckets.add(d);
    }
    final result = buckets.map((d) {
      return <String, dynamic>{
        'month': d.month,
        'year': d.year,
        'label': _monthLabel(d.month),
        'income': 0.0,
        'expense': 0.0,
      };
    }).toList();

    for (final tx in transactions) {
      final m = tx.transactionDate.month;
      final y = tx.transactionDate.year;
      for (final r in result) {
        if (r['month'] == m && r['year'] == y) {
          if (tx.isIncome) {
            r['income'] = (r['income'] as double) + tx.amount;
          } else if (tx.isExpense) {
            r['expense'] = (r['expense'] as double) + tx.amount;
          }
          break;
        }
      }
    }
    return result;
  }

  String _monthLabel(int m) {
    const labels = [
      'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
      'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec',
    ];
    return labels[(m - 1).clamp(0, 11)];
  }

  void _setLoading(bool value) {
    isLoading = value;
    notifyListeners();
  }
}
