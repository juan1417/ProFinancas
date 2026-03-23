import '../entities/transaction.dart';
import '../repositories/transaction_repository.dart';

class CreateTransactionUseCase {
  const CreateTransactionUseCase(this._repository);
  final TransactionRepository _repository;

  Future<Transaction> call({
    required int categoryId,
    required String type,
    required double amount,
    required String description,
    required DateTime transactionDate,
    String? notes,
  }) =>
      _repository.createTransaction(
        categoryId: categoryId,
        type: type,
        amount: amount,
        description: description,
        transactionDate: transactionDate,
        notes: notes,
      );
}
