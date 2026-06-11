import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:fl_chart/fl_chart.dart';
import '../../../transactions/presentation/providers/transaction_provider.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../../../core/utils/currency_formatter.dart';
import '../../../../core/widgets/app_card.dart';
import '../../../../core/widgets/pro_app_bar.dart';
import '../../../../core/widgets/percentage_badge.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  int _touchedPieIndex = -1;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<TransactionProvider>().loadSummary();
    });
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<TransactionProvider>();

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: const ProAppBar(),
      body: RefreshIndicator(
        color: AppColors.primary,
        onRefresh: () => context.read<TransactionProvider>().loadSummary(),
        child: ListView(
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
          children: [
            _PortfolioCard(provider: provider),
            const SizedBox(height: 16),
            _BudgetCard(provider: provider),
            const SizedBox(height: 16),
            _ExpenseAllocationCard(
              provider: provider,
              touchedIndex: _touchedPieIndex,
              onTouch: (i) => setState(() => _touchedPieIndex = i),
            ),
            const SizedBox(height: 16),
            _RecentTransactionsCard(provider: provider),
            const SizedBox(height: 24),
          ],
        ),
      ),
    );
  }
}

// ── Portfolio / Valuation card ────────────────────────────────────────────────
class _PortfolioCard extends StatelessWidget {
  const _PortfolioCard({required this.provider});
  final TransactionProvider provider;

  @override
  Widget build(BuildContext context) {
    // Backend returns `total_expenses` (plural). Keep `total_expense` as a
    // fallback so mock data and any older payload still work.
    final balance = (provider.summary?['balance'] as num?)?.toDouble() ?? 0.0;
    final income = (provider.summary?['total_income'] as num?)?.toDouble() ?? 0.0;
    final expenses = ((provider.summary?['total_expenses']
                ?? provider.summary?['total_expense']) as num?)
            ?.toDouble() ??
        0.0;

    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [AppColors.primary, AppColors.primary500],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('TOTAL PORTFOLIO',
                  style: AppTextStyles.label
                      .copyWith(color: AppColors.white.withValues(alpha: 0.7))),
              Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: AppColors.white.withValues(alpha: 0.15),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Text('2026 Q1',
                    style: AppTextStyles.label
                        .copyWith(color: AppColors.white)),
              ),
            ],
          ),
          const SizedBox(height: 12),
          provider.isLoading
              ? const CircularProgressIndicator(color: Colors.white)
              : Text(
                  CurrencyFormatter.format(balance),
                  style: AppTextStyles.amount
                      .copyWith(color: AppColors.white, fontSize: 38),
                ),
          const SizedBox(height: 4),
          const PercentageBadge('+12.4%', positive: true),
          const SizedBox(height: 24),
          Row(
            children: [
              Expanded(
                child: _MiniStat(
                  label: 'INCOME',
                  value: CurrencyFormatter.format(income),
                  icon: Icons.arrow_upward,
                  color: AppColors.secondary100,
                ),
              ),
              Container(width: 1, height: 40, color: Colors.white24),
              Expanded(
                child: _MiniStat(
                  label: 'EXPENSES',
                  value: CurrencyFormatter.format(expenses),
                  icon: Icons.arrow_downward,
                  color: AppColors.tertiary100,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _MiniStat extends StatelessWidget {
  const _MiniStat(
      {required this.label,
      required this.value,
      required this.icon,
      required this.color});

  final String label;
  final String value;
  final IconData icon;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 12),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(6),
            decoration: BoxDecoration(
              color: color.withValues(alpha: 0.25),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Icon(icon, color: color, size: 14),
          ),
          const SizedBox(width: 10),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(label,
                  style: AppTextStyles.label
                      .copyWith(color: AppColors.white.withValues(alpha: 0.7))),
              Text(value,
                  style: AppTextStyles.bodyMedium.copyWith(
                      color: AppColors.white, fontSize: 13)),
            ],
          ),
        ],
      ),
    );
  }
}

// ── Budget progress card ──────────────────────────────────────────────────────
class _BudgetCard extends StatelessWidget {
  const _BudgetCard({required this.provider});
  final TransactionProvider provider;

  @override
  Widget build(BuildContext context) {
    final expenses = ((provider.summary?['total_expenses']
                ?? provider.summary?['total_expense']) as num?)
            ?.toDouble() ??
        0.0;
    // TODO(audit): hardcoded budget until a Budget model lands in the
    // backend. When it does, expose it as a `BudgetProvider` (or
    // extend TransactionProvider) and read from there. For now this
    // is a placeholder so the "MONTHLY BUDGET" card has something
    // to show.
    const budget = 5000.0;
    final progress = (expenses / budget).clamp(0.0, 1.0);
    final remaining = budget - expenses;

    return AppCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('MONTHLY BUDGET',
                  style: AppTextStyles.label),
              Text(CurrencyFormatter.format(budget),
                  style: AppTextStyles.bodyMedium),
            ],
          ),
          const SizedBox(height: 16),
          ClipRRect(
            borderRadius: BorderRadius.circular(6),
            child: LinearProgressIndicator(
              value: progress,
              backgroundColor: AppColors.neutral200,
              color: progress > 0.85 ? AppColors.tertiary : AppColors.primary,
              minHeight: 8,
            ),
          ),
          const SizedBox(height: 10),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                '${(progress * 100).toStringAsFixed(0)}% used',
                style: AppTextStyles.bodySecondary,
              ),
              Text(
                '${CurrencyFormatter.format(remaining)} left',
                style: AppTextStyles.bodySecondary.copyWith(
                  color: remaining < 0 ? AppColors.expense : AppColors.income,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

// ── Expense allocation donut chart ────────────────────────────────────────────
class _ExpenseAllocationCard extends StatelessWidget {
  const _ExpenseAllocationCard({
    required this.provider,
    required this.touchedIndex,
    required this.onTouch,
  });

  final TransactionProvider provider;
  final int touchedIndex;
  final ValueChanged<int> onTouch;

  static const _sliceColors = [
    AppColors.primary,
    AppColors.primary500,
    AppColors.secondary,
    AppColors.tertiary500,
    AppColors.primary300,
  ];

  @override
  Widget build(BuildContext context) {
    final byCategory = provider.byCategoryBreakdown;

    final sections = byCategory.isEmpty
        ? [PieChartSectionData(value: 1, color: AppColors.neutral200, radius: 28, showTitle: false)]
        : byCategory.asMap().entries.map((e) {
            final i = e.key;
            final item = e.value as Map<String, dynamic>;
            final isTouched = i == touchedIndex;
            return PieChartSectionData(
              value: (item['total'] as num?)?.toDouble() ?? 0,
              color: _sliceColors[i % _sliceColors.length],
              radius: isTouched ? 34 : 28,
              showTitle: isTouched,
              title: '${item['name']}',
              titleStyle: const TextStyle(
                  fontSize: 10,
                  color: Colors.white,
                  fontWeight: FontWeight.w600),
            );
          }).toList();

    return AppCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('EXPENSE ALLOCATION', style: AppTextStyles.label),
              Text('This month',
                  style: AppTextStyles.bodySecondary),
            ],
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              SizedBox(
                height: 120,
                width: 120,
                child: PieChart(
                  PieChartData(
                    sections: sections,
                    centerSpaceRadius: 30,
                    pieTouchData: PieTouchData(
                      touchCallback: (_, response) {
                        final idx = response?.touchedSection
                            ?.touchedSectionIndex ?? -1;
                        onTouch(idx);
                      },
                    ),
                  ),
                ),
              ),
              const SizedBox(width: 20),
              Expanded(
                child: byCategory.isEmpty
                    ? Text('No data yet', style: AppTextStyles.bodySecondary)
                    : Column(
                        children: byCategory.asMap().entries.map((e) {
                          final i = e.key;
                          final item = e.value as Map<String, dynamic>;
                          return Padding(
                            padding: const EdgeInsets.only(bottom: 8),
                            child: Row(children: [
                              Container(
                                width: 10,
                                height: 10,
                                decoration: BoxDecoration(
                                  color: _sliceColors[i % _sliceColors.length],
                                  shape: BoxShape.circle,
                                ),
                              ),
                              const SizedBox(width: 8),
                              Expanded(
                                child: Text(
                                  item['name'] as String? ?? '',
                                  style: AppTextStyles.bodySecondary,
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                              Text(
                                CurrencyFormatter.format(
                                    (item['total'] as num?)?.toDouble() ?? 0),
                                style: AppTextStyles.bodyMedium
                                    .copyWith(fontSize: 12),
                              ),
                            ]),
                          );
                        }).toList(),
                      ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

// ── Recent transactions ───────────────────────────────────────────────────────
class _RecentTransactionsCard extends StatelessWidget {
  const _RecentTransactionsCard({required this.provider});
  final TransactionProvider provider;

  @override
  Widget build(BuildContext context) {
    final transactions = provider.transactions.take(5).toList();

    return AppCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('RECENT TRANSACTIONS', style: AppTextStyles.label),
              TextButton(
                style: TextButton.styleFrom(
                    padding: EdgeInsets.zero,
                    minimumSize: Size.zero,
                    tapTargetSize: MaterialTapTargetSize.shrinkWrap),
                onPressed: () {},
                child: Text('View all',
                    style: AppTextStyles.bodySecondary
                        .copyWith(color: AppColors.primary)),
              ),
            ],
          ),
          const SizedBox(height: 8),
          if (transactions.isEmpty)
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 16),
              child: Center(
                  child: Text('No transactions yet',
                      style: AppTextStyles.bodySecondary)),
            )
          else
            ...transactions.map((tx) {
              final isExpense = tx.isExpense;
              return Padding(
                padding: const EdgeInsets.symmetric(vertical: 6),
                child: Row(
                  children: [
                    Container(
                      width: 40,
                      height: 40,
                      decoration: BoxDecoration(
                        color: isExpense
                            ? AppColors.tertiary100
                            : AppColors.secondary100,
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Icon(
                        isExpense
                            ? Icons.arrow_downward
                            : Icons.arrow_upward,
                        color: isExpense
                            ? AppColors.expense
                            : AppColors.income,
                        size: 18,
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(tx.description,
                              style: AppTextStyles.body,
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis),
                          Text(tx.categoryName,
                              style: AppTextStyles.bodySecondary),
                        ],
                      ),
                    ),
                    Text(
                      '${isExpense ? '−' : '+'} ${CurrencyFormatter.format(tx.amount)}',
                      style: AppTextStyles.amountSmall.copyWith(
                          color: isExpense
                              ? AppColors.expense
                              : AppColors.income),
                    ),
                  ],
                ),
              );
            }),
        ],
      ),
    );
  }
}
