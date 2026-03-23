import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/transaction_provider.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../../../core/utils/currency_formatter.dart';
import '../../../../core/utils/date_formatter.dart';
import '../../../../core/widgets/app_card.dart';
import '../../../../core/widgets/pro_app_bar.dart';
import '../../../../core/widgets/segmented_tabs.dart';

class TransactionsScreen extends StatefulWidget {
  const TransactionsScreen({super.key});

  @override
  State<TransactionsScreen> createState() => _TransactionsScreenState();
}

class _TransactionsScreenState extends State<TransactionsScreen> {
  final _searchCtrl = TextEditingController();
  String _selectedFilter = 'All';
  final List<String> _filters = ['All', 'Income', 'Expenses'];

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<TransactionProvider>().loadTransactions();
    });
  }

  @override
  void dispose() {
    _searchCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<TransactionProvider>();
    final query = _searchCtrl.text.toLowerCase();

    final filtered = provider.transactions.where((tx) {
      final matchesQuery = query.isEmpty ||
          tx.description.toLowerCase().contains(query) ||
          tx.category.name.toLowerCase().contains(query);
      final matchesFilter = _selectedFilter == 'All' ||
          (_selectedFilter == 'Income' && tx.type == 'income') ||
          (_selectedFilter == 'Expenses' && tx.type == 'expense');
      return matchesQuery && matchesFilter;
    }).toList();

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: const ProAppBar(title: 'Expense Manager'),
      floatingActionButton: FloatingActionButton(
        onPressed: () => _showAddTransactionSheet(context),
        backgroundColor: AppColors.secondary,
        child: const Icon(Icons.add, color: AppColors.white),
      ),
      body: Column(
        children: [
          // Summary strip
          _SummaryStrip(provider: provider),

          // Search + filters
          Padding(
            padding:
                const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
            child: Column(
              children: [
                TextField(
                  controller: _searchCtrl,
                  onChanged: (_) => setState(() {}),
                  decoration: InputDecoration(
                    hintText: 'Search transactions…',
                    prefixIcon: const Icon(Icons.search,
                        color: AppColors.neutral500, size: 20),
                    suffixIcon: _searchCtrl.text.isNotEmpty
                        ? GestureDetector(
                            onTap: () {
                              _searchCtrl.clear();
                              setState(() {});
                            },
                            child: const Icon(Icons.close,
                                size: 18,
                                color: AppColors.neutral500),
                          )
                        : null,
                  ),
                ),
                const SizedBox(height: 12),
                SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  child: SegmentedTabs(
                    options: _filters,
                    selected: _selectedFilter,
                    onSelect: (v) => setState(() => _selectedFilter = v),
                  ),
                ),
              ],
            ),
          ),

          // Transactions list
          Expanded(
            child: provider.isLoading
                ? const Center(
                    child: CircularProgressIndicator(
                        color: AppColors.primary))
                : filtered.isEmpty
                    ? Center(
                        child: Column(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            const Icon(Icons.receipt_long_outlined,
                                size: 48, color: AppColors.neutral400),
                            const SizedBox(height: 12),
                            Text('No transactions found',
                                style: AppTextStyles.bodySecondary),
                          ],
                        ),
                      )
                    : ListView.separated(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 24, vertical: 4),
                        itemCount: filtered.length,
                        separatorBuilder: (_, __) =>
                            const SizedBox(height: 8),
                        itemBuilder: (context, i) =>
                            _TransactionTile(transaction: filtered[i]),
                      ),
          ),
        ],
      ),
    );
  }

  void _showAddTransactionSheet(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => const _AddTransactionSheet(),
    );
  }
}

// ── Summary strip ─────────────────────────────────────────────────────────────
class _SummaryStrip extends StatelessWidget {
  const _SummaryStrip({required this.provider});
  final TransactionProvider provider;

  @override
  Widget build(BuildContext context) {
    final income =
        (provider.summary?['total_income'] as num?)?.toDouble() ?? 0.0;
    final expense =
        (provider.summary?['total_expense'] as num?)?.toDouble() ?? 0.0;

    return Container(
      color: AppColors.white,
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
      child: Row(
        children: [
          Expanded(
            child: _StripStat(
              label: 'INCOME',
              value: CurrencyFormatter.format(income),
              color: AppColors.income,
            ),
          ),
          Container(width: 1, height: 40, color: AppColors.divider),
          Expanded(
            child: _StripStat(
              label: 'EXPENSES',
              value: CurrencyFormatter.format(expense),
              color: AppColors.expense,
            ),
          ),
          Container(width: 1, height: 40, color: AppColors.divider),
          Expanded(
            child: _StripStat(
              label: 'COUNT',
              value: '${provider.transactions.length}',
              color: AppColors.primary,
            ),
          ),
        ],
      ),
    );
  }
}

class _StripStat extends StatelessWidget {
  const _StripStat(
      {required this.label, required this.value, required this.color});

  final String label;
  final String value;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Text(label, style: AppTextStyles.label),
        const SizedBox(height: 4),
        Text(value,
            style: AppTextStyles.amountSmall.copyWith(color: color)),
      ],
    );
  }
}

// ── Transaction tile ──────────────────────────────────────────────────────────
class _TransactionTile extends StatelessWidget {
  const _TransactionTile({required this.transaction});
  final dynamic transaction;

  @override
  Widget build(BuildContext context) {
    final isExpense = transaction.type == 'expense';
    final categoryColor = isExpense ? AppColors.tertiary : AppColors.secondary;

    return AppCard(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Row(
        children: [
          Container(
            width: 44,
            height: 44,
            decoration: BoxDecoration(
              color: categoryColor.withOpacity(0.12),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(
              isExpense ? Icons.remove_circle_outline : Icons.add_circle_outline,
              color: categoryColor,
              size: 20,
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  transaction.description,
                  style: AppTextStyles.bodyMedium,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
                const SizedBox(height: 2),
                Text(
                  '${transaction.category.name} · ${DateFormatter.short(transaction.date)}',
                  style: AppTextStyles.bodySecondary,
                ),
              ],
            ),
          ),
          const SizedBox(width: 8),
          Text(
            '${isExpense ? '−' : '+'} ${CurrencyFormatter.format(transaction.amount)}',
            style: AppTextStyles.amountSmall
                .copyWith(color: categoryColor),
          ),
        ],
      ),
    );
  }
}

// ── Add transaction bottom sheet ──────────────────────────────────────────────
class _AddTransactionSheet extends StatefulWidget {
  const _AddTransactionSheet();

  @override
  State<_AddTransactionSheet> createState() => _AddTransactionSheetState();
}

class _AddTransactionSheetState extends State<_AddTransactionSheet> {
  final _formKey = GlobalKey<FormState>();
  final _descCtrl = TextEditingController();
  final _amountCtrl = TextEditingController();
  String _type = 'expense';
  int? _categoryId;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<TransactionProvider>().loadCategories();
    });
  }

  @override
  void dispose() {
    _descCtrl.dispose();
    _amountCtrl.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate() || _categoryId == null) return;
    final provider = context.read<TransactionProvider>();
    await provider.createTransaction(
      description: _descCtrl.text.trim(),
      amount: double.parse(_amountCtrl.text),
      type: _type,
      categoryId: _categoryId!,
      date: DateTime.now(),
    );
    if (mounted) Navigator.pop(context);
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<TransactionProvider>();

    return Container(
      margin: const EdgeInsets.all(16),
      padding: EdgeInsets.only(
        left: 24,
        right: 24,
        top: 24,
        bottom: MediaQuery.of(context).viewInsets.bottom + 24,
      ),
      decoration: BoxDecoration(
        color: AppColors.white,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Form(
        key: _formKey,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('New Transaction', style: AppTextStyles.headlineSmall),
                IconButton(
                  onPressed: () => Navigator.pop(context),
                  icon: const Icon(Icons.close),
                ),
              ],
            ),
            const SizedBox(height: 16),
            // Type toggle
            Row(
              children: ['expense', 'income'].map((t) {
                final active = _type == t;
                return Expanded(
                  child: Padding(
                    padding: EdgeInsets.only(right: t == 'expense' ? 8 : 0),
                    child: GestureDetector(
                      onTap: () => setState(() => _type = t),
                      child: Container(
                        padding: const EdgeInsets.symmetric(vertical: 12),
                        decoration: BoxDecoration(
                          color: active
                              ? (t == 'expense'
                                  ? AppColors.tertiary100
                                  : AppColors.secondary100)
                              : AppColors.neutral100,
                          borderRadius: BorderRadius.circular(10),
                          border: active
                              ? Border.all(
                                  color: t == 'expense'
                                      ? AppColors.tertiary
                                      : AppColors.secondary,
                                  width: 1.5)
                              : null,
                        ),
                        child: Center(
                          child: Text(
                            t == 'expense' ? 'Expense' : 'Income',
                            style: AppTextStyles.bodyMedium.copyWith(
                              color: active
                                  ? (t == 'expense'
                                      ? AppColors.tertiary
                                      : AppColors.secondary)
                                  : AppColors.neutral700,
                            ),
                          ),
                        ),
                      ),
                    ),
                  ),
                );
              }).toList(),
            ),
            const SizedBox(height: 16),
            TextFormField(
              controller: _descCtrl,
              decoration: const InputDecoration(labelText: 'Description'),
              validator: (v) =>
                  v == null || v.isEmpty ? 'Required' : null,
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _amountCtrl,
              keyboardType:
                  const TextInputType.numberWithOptions(decimal: true),
              decoration: const InputDecoration(
                  labelText: 'Amount', prefixText: '\$ '),
              validator: (v) {
                if (v == null || v.isEmpty) return 'Required';
                if (double.tryParse(v) == null) return 'Invalid number';
                return null;
              },
            ),
            const SizedBox(height: 12),
            DropdownButtonFormField<int>(
              decoration:
                  const InputDecoration(labelText: 'Category'),
              value: _categoryId,
              items: provider.categories
                  .map((c) => DropdownMenuItem(
                      value: c.id, child: Text(c.name)))
                  .toList(),
              onChanged: (v) => setState(() => _categoryId = v),
              validator: (v) =>
                  v == null ? 'Select a category' : null,
            ),
            const SizedBox(height: 24),
            FilledButton(
              onPressed: provider.isLoading ? null : _submit,
              child: provider.isLoading
                  ? const SizedBox(
                      height: 20,
                      width: 20,
                      child: CircularProgressIndicator(
                          strokeWidth: 2, color: Colors.white))
                  : const Text('Save Transaction'),
            ),
          ],
        ),
      ),
    );
  }
}
