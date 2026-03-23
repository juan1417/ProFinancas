# Flutter Layer Examples

## Entity (domain/entities/transaction.dart)

Pure Dart — no JSON, no Flutter imports.

```dart
class Transaction {
  final String id;
  final String type; // 'INCOME' | 'EXPENSE'
  final double amount;
  final String description;
  final String categoryName;
  final DateTime transactionDate;

  const Transaction({
    required this.id,
    required this.type,
    required this.amount,
    required this.description,
    required this.categoryName,
    required this.transactionDate,
  });
}
```

## Model (data/models/transaction_model.dart)

Extends entity, adds JSON serialization.

```dart
class TransactionModel extends Transaction {
  const TransactionModel({...}) : super(...);

  factory TransactionModel.fromJson(Map<String, dynamic> json) {
    return TransactionModel(
      id: json['id'].toString(),
      type: json['type'] as String,
      amount: double.parse(json['amount'].toString()),
      description: json['description'] as String,
      categoryName: json['category_name'] as String,
      transactionDate: DateTime.parse(json['transaction_date'] as String),
    );
  }

  Map<String, dynamic> toJson() => {
    'type': type,
    'amount': amount,
    'description': description,
    'category': categoryId,
    'transaction_date': transactionDate.toIso8601String(),
  };
}
```

## Repository Interface (domain/repositories/transaction_repository.dart)

```dart
abstract class TransactionRepository {
  Future<List<Transaction>> getTransactions();
  Future<Transaction> createTransaction(Map<String, dynamic> data);
  Future<Map<String, dynamic>> getSummary({String period = 'month'});
}
```

## UseCase (domain/usecases/get_transactions_usecase.dart)

One class, one `call()` method.

```dart
class GetTransactionsUseCase {
  final TransactionRepository _repository;
  const GetTransactionsUseCase(this._repository);

  Future<List<Transaction>> call() => _repository.getTransactions();
}
```

## Remote Datasource (data/datasources/transaction_remote_datasource.dart)

```dart
class TransactionRemoteDatasource {
  final ApiClient _client;
  const TransactionRemoteDatasource(this._client);

  Future<List<TransactionModel>> getTransactions() async {
    final response = await _client.get('/transactions/');
    return (response.data as List)
        .map((e) => TransactionModel.fromJson(e))
        .toList();
  }
}
```

## Repository Impl (data/repositories/transaction_repository_impl.dart)

```dart
class TransactionRepositoryImpl implements TransactionRepository {
  final TransactionRemoteDatasource _datasource;
  const TransactionRepositoryImpl(this._datasource);

  @override
  Future<List<Transaction>> getTransactions() =>
      _datasource.getTransactions();
}
```

## Provider (presentation/providers/transaction_provider.dart)

```dart
class TransactionProvider extends ChangeNotifier {
  final GetTransactionsUseCase _getTransactions;

  List<Transaction> transactions = [];
  bool isLoading = false;
  String? error;

  TransactionProvider(this._getTransactions);

  Future<void> load() async {
    isLoading = true;
    error = null;
    notifyListeners();
    try {
      transactions = await _getTransactions();
    } catch (e) {
      error = e.toString();
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }
}
```
