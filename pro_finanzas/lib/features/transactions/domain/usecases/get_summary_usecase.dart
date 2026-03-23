import '../repositories/transaction_repository.dart';

class GetSummaryUseCase {
  const GetSummaryUseCase(this._repository);
  final TransactionRepository _repository;

  Future<Map<String, dynamic>> call({
    String period = 'month',
    String? startDate,
    String? endDate,
  }) =>
      _repository.getSummary(period: period, startDate: startDate, endDate: endDate);
}
