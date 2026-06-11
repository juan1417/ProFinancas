import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../transactions/presentation/providers/transaction_provider.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../../../core/utils/currency_formatter.dart';
import '../../../../core/widgets/app_card.dart';
import '../../../../core/widgets/pro_app_bar.dart';

class WalletScreen extends StatelessWidget {
  const WalletScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<TransactionProvider>();
    final balance =
        (provider.summary?['balance'] as num?)?.toDouble() ?? 0.0;
    final income =
        (provider.summary?['total_income'] as num?)?.toDouble() ?? 0.0;
    // Backend key is `total_expenses` (plural). Keep fallback for mock data.
    final expense = ((provider.summary?['total_expenses']
                ?? provider.summary?['total_expense']) as num?)
            ?.toDouble() ??
        0.0;

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: const ProAppBar(title: 'Wallets'),
      body: ListView(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
        children: [
          // Premium bank card
          _BankCard(balance: balance),
          const SizedBox(height: 24),

          // Quick actions
          Row(
            children: [
              _QuickAction(
                  icon: Icons.add, label: 'Add Funds', color: AppColors.secondary),
              const SizedBox(width: 12),
              _QuickAction(
                  icon: Icons.send,
                  label: 'Transfer',
                  color: AppColors.primary),
              const SizedBox(width: 12),
              _QuickAction(
                  icon: Icons.swap_horiz,
                  label: 'Exchange',
                  color: AppColors.primary500),
              const SizedBox(width: 12),
              _QuickAction(
                  icon: Icons.more_horiz,
                  label: 'More',
                  color: AppColors.neutral700),
            ],
          ),
          const SizedBox(height: 24),

          // Card usage stats
          Text('CARD USAGE', style: AppTextStyles.label),
          const SizedBox(height: 8),
          AppCard(
            child: Column(
              children: [
                _UsageStat(
                  label: 'Total Income',
                  value: CurrencyFormatter.format(income),
                  icon: Icons.arrow_upward,
                  color: AppColors.income,
                  pct: income == 0 ? 0 : 0.75,
                ),
                const SizedBox(height: 16),
                _UsageStat(
                  label: 'Total Expenses',
                  value: CurrencyFormatter.format(expense),
                  icon: Icons.arrow_downward,
                  color: AppColors.expense,
                  pct: income == 0 ? 0 : (expense / (income == 0 ? 1 : income)).clamp(0.0, 1.0),
                ),
                const SizedBox(height: 16),
                _UsageStat(
                  label: 'Savings Rate',
                  value: income == 0
                      ? '0%'
                      : '${((income - expense) / income * 100).toStringAsFixed(1)}%',
                  icon: Icons.savings_outlined,
                  color: AppColors.primary,
                  pct: income == 0 ? 0 : ((income - expense) / income).clamp(0.0, 1.0),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),

          // Cards list
          Text('MY CARDS', style: AppTextStyles.label),
          const SizedBox(height: 8),
          ..._buildCardsList(),
          const SizedBox(height: 16),
          OutlinedButton.icon(
            style: OutlinedButton.styleFrom(
              foregroundColor: AppColors.primary,
              side: const BorderSide(color: AppColors.primary),
              minimumSize: const Size(double.infinity, 52),
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12)),
            ),
            onPressed: () {},
            icon: const Icon(Icons.add, size: 18),
            label: const Text('Add New Card'),
          ),
          const SizedBox(height: 24),
        ],
      ),
    );
  }

  List<Widget> _buildCardsList() {
    final cards = [
      ('Visa Platinum', '**** 4821', AppColors.primary, '03/28'),
      ('Mastercard Gold', '**** 9012', AppColors.neutral700, '11/27'),
    ];

    return cards.map((c) {
      return Padding(
        padding: const EdgeInsets.only(bottom: 8),
        child: AppCard(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
          child: Row(
            children: [
              Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  color: c.$3.withOpacity(0.12),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(Icons.credit_card, color: c.$3, size: 22),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(c.$1, style: AppTextStyles.bodyMedium),
                    Text(c.$2, style: AppTextStyles.bodySecondary),
                  ],
                ),
              ),
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text('Expires', style: AppTextStyles.label),
                  Text(c.$4, style: AppTextStyles.bodyMedium),
                ],
              ),
            ],
          ),
        ),
      );
    }).toList();
  }
}

// ── Bank card widget ──────────────────────────────────────────────────────────
class _BankCard extends StatelessWidget {
  const _BankCard({required this.balance});
  final double balance;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 190,
      width: double.infinity,
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(20),
        gradient: const LinearGradient(
          colors: [Color(0xFF1A237E), Color(0xFF283593), Color(0xFF3949AB)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        boxShadow: [
          BoxShadow(
            color: AppColors.primary.withOpacity(0.4),
            blurRadius: 20,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Stack(
        children: [
          // Decorative circle
          Positioned(
            right: -30,
            top: -30,
            child: Container(
              width: 130,
              height: 130,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: Colors.white.withOpacity(0.06),
              ),
            ),
          ),
          Positioned(
            right: 20,
            top: 40,
            child: Container(
              width: 80,
              height: 80,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: Colors.white.withOpacity(0.06),
              ),
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text('PROFIANCAS',
                      style: AppTextStyles.label
                          .copyWith(color: Colors.white70)),
                  const Icon(Icons.contactless,
                      color: Colors.white54, size: 22),
                ],
              ),
              const Spacer(),
              Text(
                CurrencyFormatter.format(balance),
                style: AppTextStyles.amount.copyWith(
                    color: AppColors.white, fontSize: 30),
              ),
              const SizedBox(height: 4),
              Text('Available Balance',
                  style: AppTextStyles.bodySecondary
                      .copyWith(color: Colors.white54)),
              const SizedBox(height: 12),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text('**** **** **** 4821',
                      style: AppTextStyles.body
                          .copyWith(color: Colors.white70, letterSpacing: 2)),
                  Text('03/28',
                      style: AppTextStyles.bodySecondary
                          .copyWith(color: Colors.white54)),
                ],
              ),
            ],
          ),
        ],
      ),
    );
  }
}

// ── Quick action button ───────────────────────────────────────────────────────
class _QuickAction extends StatelessWidget {
  const _QuickAction(
      {required this.icon, required this.label, required this.color});

  final IconData icon;
  final String label;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: GestureDetector(
        onTap: () {},
        child: Column(
          children: [
            Container(
              width: 52,
              height: 52,
              decoration: BoxDecoration(
                color: color.withOpacity(0.1),
                borderRadius: BorderRadius.circular(14),
              ),
              child: Icon(icon, color: color, size: 22),
            ),
            const SizedBox(height: 6),
            Text(label,
                style: AppTextStyles.label.copyWith(fontSize: 10),
                textAlign: TextAlign.center),
          ],
        ),
      ),
    );
  }
}

// ── Usage stat row ────────────────────────────────────────────────────────────
class _UsageStat extends StatelessWidget {
  const _UsageStat({
    required this.label,
    required this.value,
    required this.icon,
    required this.color,
    required this.pct,
  });

  final String label;
  final String value;
  final IconData icon;
  final Color color;
  final double pct;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Row(
          children: [
            Container(
              padding: const EdgeInsets.all(6),
              decoration: BoxDecoration(
                color: color.withOpacity(0.1),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Icon(icon, color: color, size: 14),
            ),
            const SizedBox(width: 10),
            Expanded(
                child:
                    Text(label, style: AppTextStyles.bodyMedium)),
            Text(value,
                style: AppTextStyles.amountSmall
                    .copyWith(color: color)),
          ],
        ),
        const SizedBox(height: 8),
        ClipRRect(
          borderRadius: BorderRadius.circular(6),
          child: LinearProgressIndicator(
            value: pct,
            backgroundColor: AppColors.neutral200,
            color: color,
            minHeight: 6,
          ),
        ),
      ],
    );
  }
}
