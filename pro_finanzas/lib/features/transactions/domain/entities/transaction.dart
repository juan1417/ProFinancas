class Transaction {
  const Transaction({
    required this.id,
    required this.type,
    required this.amount,
    required this.description,
    required this.categoryId,
    required this.categoryName,
    required this.transactionDate,
    this.notes,
  });

  final int id;
  final String type; // 'INCOME' | 'EXPENSE'
  final double amount;
  final String description;
  final int categoryId;
  final String categoryName;
  final DateTime transactionDate;
  final String? notes;

  bool get isIncome => type == 'INCOME';
  bool get isExpense => type == 'EXPENSE';
}
