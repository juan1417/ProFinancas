class Category {
  const Category({
    required this.id,
    required this.name,
    required this.type,
    this.description,
    this.isActive = true,
    this.transactionsCount = 0,
  });

  final int id;
  final String name;
  final String type; // 'INCOME' | 'EXPENSE'
  final String? description;
  final bool isActive;
  final int transactionsCount;

  bool get isIncome => type == 'INCOME';
  bool get isExpense => type == 'EXPENSE';
}
