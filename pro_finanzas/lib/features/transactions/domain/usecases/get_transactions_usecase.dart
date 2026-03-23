import '../entities/transaction.dart';
import '../repositories/transaction_repository.dart';

class GetTransactionsUseCase {
  const GetTransactionsUseCase(this._repository);
  final TransactionRepository _repository;

  Future<List<Transaction>> call({String? type, int? categoryId, String? search}) =>
      _repository.getTransactions(type: type, categoryId: categoryId, search: search);
}
