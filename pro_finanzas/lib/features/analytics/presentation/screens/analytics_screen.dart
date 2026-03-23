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
import '../../../../core/widgets/segmented_tabs.dart';

class AnalyticsScreen extends StatefulWidget {
  const AnalyticsScreen({super.key});

  @override
  State<AnalyticsScreen> createState() => _AnalyticsScreenState();
}

class _AnalyticsScreenState extends State<AnalyticsScreen> {
  String _period = 'Monthly';
  final _searchCtrl = TextEditingController();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<TransactionProvider>().loadSummary();
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
    final income =
        (provider.summary?['total_income'] as num?)?.toDouble() ?? 0.0;
    final expense =
        (provider.summary?['total_expense'] as num?)?.toDouble() ?? 0.0;
    final balance =
        (provider.summary?['balance'] as num?)?.toDouble() ?? 0.0;

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: const ProAppBar(title: 'Analytics'),
      body: ListView(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
        children: [
          // Period filter
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: SegmentedTabs(
              options: const ['Weekly', 'Monthly', 'Yearly'],
              selected: _period,
              onSelect: (v) => setState(() => _period = v),
            ),
          ),
          const SizedBox(height: 16),

          // KPI row
          Row(
            children: [
              Expanded(
                child: _KpiCard(
                  label: 'INCOME',
                  value: CurrencyFormatter.format(income),
                  badge: const PercentageBadge('+8.2%', positive: true),
                  color: AppColors.income,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: _KpiCard(
                  label: 'EXPENSES',
                  value: CurrencyFormatter.format(expense),
                  badge: const PercentageBadge('+3.1%', positive: false),
                  color: AppColors.expense,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          _KpiCard(
            label: 'NET BALANCE',
            value: CurrencyFormatter.format(balance),
            badge: const PercentageBadge('+12.4%', positive: true),
            color: balance >= 0 ? AppColors.income : AppColors.expense,
            wide: true,
          ),
          const SizedBox(height: 16),

          // Bar chart
          AppCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text('INCOME VS EXPENSES', style: AppTextStyles.label),
                    Text(_period, style: AppTextStyles.bodySecondary),
                  ],
                ),
                const SizedBox(height: 20),
                SizedBox(
                  height: 160,
                  child: _BarChartWidget(income: income, expense: expense),
                ),
                const SizedBox(height: 12),
                Row(children: [
                  _ChartLegend(
                      color: AppColors.primary, label: 'Income'),
                  const SizedBox(width: 16),
                  _ChartLegend(
                      color: AppColors.tertiary500, label: 'Expenses'),
                ]),
              ],
            ),
          ),
          const SizedBox(height: 16),

          // Category breakdown
          AppCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('BY CATEGORY', style: AppTextStyles.label),
                const SizedBox(height: 8),
                TextField(
                  controller: _searchCtrl,
                  onChanged: (_) => setState(() {}),
                  decoration: InputDecoration(
                    hintText: 'Search category…',
                    isDense: true,
                    prefixIcon: const Icon(Icons.search,
                        size: 18, color: AppColors.neutral500),
                    contentPadding:
                        const EdgeInsets.symmetric(vertical: 10),
                    border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(10),
                        borderSide: const BorderSide(
                            color: AppColors.neutral400)),
                    enabledBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(10),
                        borderSide: const BorderSide(
                            color: AppColors.neutral400)),
                  ),
                ),
                const SizedBox(height: 12),
                if (provider.isLoading)
                  const Center(
                    child: CircularProgressIndicator(
                        color: AppColors.primary),
                  )
                else
                  _buildCategoryRows(provider),
              ],
            ),
          ),
          const SizedBox(height: 24),
        ],
      ),
    );
  }

  Widget _buildCategoryRows(TransactionProvider provider) {
    final byCategory =
        (provider.summary?['by_category'] as List<dynamic>?) ?? [];
    final query = _searchCtrl.text.toLowerCase();

    final filtered = byCategory
        .where((c) =>
            query.isEmpty ||
            (c['name'] as String).toLowerCase().contains(query))
        .toList();

    if (filtered.isEmpty) {
      return Padding(
        padding: const EdgeInsets.symmetric(vertical: 12),
        child: Center(
          child: Text('No data available',
              style: AppTextStyles.bodySecondary),
        ),
      );
    }

    final maxVal = filtered
        .map((c) => (c['total'] as num?)?.toDouble() ?? 0.0)
        .fold(0.0, (a, b) => a > b ? a : b);

    const colors = [
      AppColors.primary,
      AppColors.secondary,
      AppColors.tertiary500,
      AppColors.primary300,
      AppColors.secondary500,
    ];

    return Column(
      children: filtered.asMap().entries.map((e) {
        final i = e.key;
        final item = e.value as Map<String, dynamic>;
        final val = (item['total'] as num?)?.toDouble() ?? 0.0;
        final pct = maxVal == 0 ? 0.0 : (val / maxVal).clamp(0.0, 1.0);
        final color = colors[i % colors.length];

        return Padding(
          padding: const EdgeInsets.only(bottom: 14),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(item['name'] as String? ?? '',
                      style: AppTextStyles.bodyMedium),
                  Text(CurrencyFormatter.format(val),
                      style: AppTextStyles.bodyMedium),
                ],
              ),
              const SizedBox(height: 6),
              ClipRRect(
                borderRadius: BorderRadius.circular(6),
                child: LinearProgressIndicator(
                  value: pct,
                  backgroundColor: AppColors.neutral200,
                  color: color,
                  minHeight: 7,
                ),
              ),
            ],
          ),
        );
      }).toList(),
    );
  }
}

// ── KPI card ──────────────────────────────────────────────────────────────────
class _KpiCard extends StatelessWidget {
  const _KpiCard({
    required this.label,
    required this.value,
    required this.badge,
    required this.color,
    this.wide = false,
  });

  final String label;
  final String value;
  final Widget badge;
  final Color color;
  final bool wide;

  @override
  Widget build(BuildContext context) {
    return AppCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: AppTextStyles.label),
          const SizedBox(height: 6),
          Text(value,
              style: wide
                  ? AppTextStyles.amount.copyWith(color: color)
                  : AppTextStyles.headlineSmall.copyWith(color: color)),
          const SizedBox(height: 8),
          Row(children: [
            badge,
            const SizedBox(width: 8),
            Text('vs last month',
                style: AppTextStyles.bodySecondary),
          ]),
        ],
      ),
    );
  }
}

// ── Bar chart widget ──────────────────────────────────────────────────────────
class _BarChartWidget extends StatelessWidget {
  const _BarChartWidget({required this.income, required this.expense});
  final double income;
  final double expense;

  @override
  Widget build(BuildContext context) {
    final maxY =
        [income, expense, 1.0].reduce((a, b) => a > b ? a : b) * 1.25;

    return BarChart(
      BarChartData(
        maxY: maxY,
        gridData: FlGridData(
          show: true,
          drawVerticalLine: false,
          getDrawingHorizontalLine: (_) => const FlLine(
            color: AppColors.neutral200, strokeWidth: 1),
        ),
        borderData: FlBorderData(show: false),
        titlesData: FlTitlesData(
          leftTitles: AxisTitles(
            sideTitles: SideTitles(
              showTitles: true,
              reservedSize: 50,
              getTitlesWidget: (v, _) => Text(
                '\$${(v / 1000).toStringAsFixed(0)}k',
                style: AppTextStyles.label,
              ),
            ),
          ),
          bottomTitles: AxisTitles(
            sideTitles: SideTitles(
              showTitles: true,
              getTitlesWidget: (v, _) {
                const months = [
                  'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'
                ];
                final i = v.toInt();
                if (i < 0 || i >= months.length) return const SizedBox();
                return Text(months[i], style: AppTextStyles.label);
              },
            ),
          ),
          topTitles: const AxisTitles(
              sideTitles: SideTitles(showTitles: false)),
          rightTitles: const AxisTitles(
              sideTitles: SideTitles(showTitles: false)),
        ),
        barGroups: List.generate(6, (i) {
          final iIncome = i == 2 ? income : income * (0.6 + i * 0.08);
          final iExpense = i == 2 ? expense : expense * (0.5 + i * 0.1);
          return BarChartGroupData(
            x: i,
            barRods: [
              BarChartRodData(
                toY: iIncome,
                color: AppColors.primary,
                width: 10,
                borderRadius: const BorderRadius.vertical(
                    top: Radius.circular(4)),
              ),
              BarChartRodData(
                toY: iExpense,
                color: AppColors.tertiary500,
                width: 10,
                borderRadius: const BorderRadius.vertical(
                    top: Radius.circular(4)),
              ),
            ],
            barsSpace: 4,
          );
        }),
      ),
    );
  }
}

class _ChartLegend extends StatelessWidget {
  const _ChartLegend({required this.color, required this.label});
  final Color color;
  final String label;

  @override
  Widget build(BuildContext context) {
    return Row(children: [
      Container(
        width: 10,
        height: 10,
        decoration: BoxDecoration(
            color: color, borderRadius: BorderRadius.circular(3)),
      ),
      const SizedBox(width: 6),
      Text(label, style: AppTextStyles.bodySecondary),
    ]);
  }
}
