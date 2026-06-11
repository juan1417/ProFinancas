import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../../core/services/card_detector_service.dart';
import '../../../../core/services/card_storage_service.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../../../core/utils/currency_formatter.dart';
import '../../../../core/widgets/app_card.dart';
import '../../../../core/widgets/pro_app_bar.dart';
import '../../../transactions/presentation/providers/transaction_provider.dart';
import '../providers/cards_provider.dart';

class WalletScreen extends StatelessWidget {
  const WalletScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final txProvider = context.watch<TransactionProvider>();
    final cardsProvider = context.watch<CardsProvider>();
    final balance = (txProvider.summary?['balance'] as num?)?.toDouble() ?? 0.0;
    final income = (txProvider.summary?['total_income'] as num?)?.toDouble() ?? 0.0;
    // Backend key is `total_expenses` (plural). Keep fallback for mock data.
    final expense = ((txProvider.summary?['total_expenses']
                ?? txProvider.summary?['total_expense']) as num?)
            ?.toDouble() ??
        0.0;

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: const ProAppBar(title: 'Wallets'),
      body: ListView(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
        children: [
          _BankCard(balance: balance),
          const SizedBox(height: 24),

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
                    pct: income == 0 ? 0 : 0.75),
                const SizedBox(height: 16),
                _UsageStat(
                  label: 'Total Expenses',
                  value: CurrencyFormatter.format(expense),
                  icon: Icons.arrow_downward,
                  color: AppColors.expense,
                  pct: income == 0
                      ? 0
                      : (expense / (income == 0 ? 1 : income)).clamp(0.0, 1.0),
                ),
                const SizedBox(height: 16),
                _UsageStat(
                  label: 'Savings Rate',
                  value: income == 0
                      ? '0%'
                      : '${((income - expense) / income * 100).toStringAsFixed(1)}%',
                  icon: Icons.savings_outlined,
                  color: AppColors.primary,
                  pct: income == 0
                      ? 0
                      : ((income - expense) / income).clamp(0.0, 1.0),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),

          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('MY CARDS', style: AppTextStyles.label),
              Text('${cardsProvider.cards.length} saved',
                  style: AppTextStyles.bodySecondary),
            ],
          ),
          const SizedBox(height: 8),
          if (cardsProvider.isLoading && cardsProvider.cards.isEmpty)
            const Padding(
              padding: EdgeInsets.symmetric(vertical: 24),
              child: Center(
                child: CircularProgressIndicator(color: AppColors.primary),
              ),
            )
          else if (cardsProvider.cards.isEmpty)
            const _EmptyCards()
          else
            ...cardsProvider.cards.map(
              (c) => Padding(
                key: ValueKey(c.id),
                padding: const EdgeInsets.only(bottom: 8),
                child: AppCard(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
                  child: Row(
                    children: [
                      Container(
                        width: 44,
                        height: 44,
                        decoration: BoxDecoration(
                          color: Color(c.color).withValues(alpha: 0.12),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Icon(Icons.credit_card,
                            color: Color(c.color), size: 22),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(c.cardholderName.isNotEmpty
                                ? c.cardholderName
                                : '${c.brand} ${c.bank}'),
                            Text('${c.brand} • **** ${c.last4}',
                                style: AppTextStyles.bodySecondary),
                          ],
                        ),
                      ),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.end,
                        children: [
                          Text('Expires', style: AppTextStyles.label),
                          Text(c.expiry, style: AppTextStyles.bodyMedium),
                        ],
                      ),
                      IconButton(
                        icon: const Icon(Icons.delete_outline,
                            color: AppColors.expense, size: 20),
                        onPressed: () => _confirmDelete(context, c),
                        visualDensity: VisualDensity.compact,
                      ),
                    ],
                  ),
                ),
              ),
            ),
          const SizedBox(height: 16),
          OutlinedButton.icon(
            style: OutlinedButton.styleFrom(
              foregroundColor: AppColors.primary,
              side: const BorderSide(color: AppColors.primary),
              minimumSize: const Size(double.infinity, 52),
              shape:
                  RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            ),
            onPressed: () => _showAddCardSheet(context),
            icon: const Icon(Icons.add, size: 18),
            label: const Text('Add New Card'),
          ),
          const SizedBox(height: 24),
        ],
      ),
    );
  }

  Future<void> _confirmDelete(BuildContext context, StoredCard c) async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Delete card?'),
        content: Text('${c.brand} • **** ${c.last4} will be removed.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            style: FilledButton.styleFrom(backgroundColor: AppColors.expense),
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Delete'),
          ),
        ],
      ),
    );
    if (ok != true || !context.mounted) return;
    final removed =
        await context.read<CardsProvider>().removeCard(c.id);
    if (!context.mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(removed ? 'Card removed' : 'Could not remove card'),
        backgroundColor: removed ? AppColors.income : AppColors.expense,
      ),
    );
  }

  void _showAddCardSheet(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => const _AddCardSheet(),
    );
  }
}

class _EmptyCards extends StatelessWidget {
  const _EmptyCards();

  @override
  Widget build(BuildContext context) {
    return AppCard(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            const Icon(Icons.credit_card_off_outlined,
                size: 40, color: AppColors.neutral400),
            const SizedBox(height: 8),
            Text('No cards yet', style: AppTextStyles.bodyMedium),
            const SizedBox(height: 4),
            Text(
              'Add one to track spending and see it on the dashboard.',
              style: AppTextStyles.bodySecondary,
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
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
            color: AppColors.primary.withValues(alpha: 0.4),
            blurRadius: 20,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Stack(
        children: [
          Positioned(
            right: -30,
            top: -30,
            child: Container(
              width: 130,
              height: 130,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: Colors.white.withValues(alpha: 0.06),
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
                color: Colors.white.withValues(alpha: 0.06),
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
                style: AppTextStyles.amount
                    .copyWith(color: AppColors.white, fontSize: 30),
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
                      style: AppTextStyles.body.copyWith(
                          color: Colors.white70, letterSpacing: 2)),
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
                color: color.withValues(alpha: 0.1),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Icon(icon, color: color, size: 14),
            ),
            const SizedBox(width: 10),
            Expanded(child: Text(label, style: AppTextStyles.bodyMedium)),
            Text(value,
                style: AppTextStyles.amountSmall.copyWith(color: color)),
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

// ── Add Card Sheet ───────────────────────────────────────────────────────────
class _AddCardSheet extends StatefulWidget {
  const _AddCardSheet();

  @override
  State<_AddCardSheet> createState() => _AddCardSheetState();
}

class _AddCardSheetState extends State<_AddCardSheet> {
  final _formKey = GlobalKey<FormState>();
  final _cardNumberCtrl = TextEditingController();
  final _expiryCtrl = TextEditingController();
  final _nameCtrl = TextEditingController();
  String? _detectedBrand;
  String? _detectedBank;

  // Brand → accent color used for the card icon. A real product would have
  // its own design tokens per brand.
  static const Map<String, int> _brandColors = {
    'Visa': 0xFF1A237E,
    'Mastercard': 0xFFFFA000,
    'Amex': 0xFF006FCF,
    'Other': 0xFF616161,
  };

  @override
  void initState() {
    super.initState();
    _cardNumberCtrl.addListener(_onCardNumberChanged);
  }

  void _onCardNumberChanged() {
    final result = CardDetectorService.instance.detectCard(_cardNumberCtrl.text);
    if (!mounted) return;
    setState(() {
      _detectedBrand = result?.type;
      _detectedBank = result?.bank;
    });
  }

  @override
  void dispose() {
    _cardNumberCtrl.removeListener(_onCardNumberChanged);
    _cardNumberCtrl.dispose();
    _expiryCtrl.dispose();
    _nameCtrl.dispose();
    super.dispose();
  }

  String? _validateRequired(String? v, {int minLength = 1}) {
    if (v == null || v.trim().length < minLength) return 'Required';
    return null;
  }

  String? _validateCardNumber(String? v) {
    final digits = v?.replaceAll(RegExp(r'\D'), '') ?? '';
    if (digits.length < 13 || digits.length > 19) {
      return 'Enter a valid card number';
    }
    return null;
  }

  String? _validateExpiry(String? v) {
    if (v == null || !RegExp(r'^\d{2}/\d{2}$').hasMatch(v.trim())) {
      return 'Use MM/YY format';
    }
    return null;
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    final digits = _cardNumberCtrl.text.replaceAll(RegExp(r'\D'), '');
    final last4 = digits.length >= 4
        ? digits.substring(digits.length - 4)
        : digits.padLeft(4, '0');
    final brand = _detectedBrand ?? 'Other';
    final bank = _detectedBank ?? 'Generic';
    final color = _brandColors[brand] ?? _brandColors['Other']!;

    final newCard = StoredCard(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      brand: brand,
      bank: bank,
      last4: last4,
      expiry: _expiryCtrl.text.trim(),
      cardholderName: _nameCtrl.text.trim(),
      color: color,
    );

    final ok = await context.read<CardsProvider>().addCard(newCard);
    if (!mounted) return;
    if (ok) {
      Navigator.pop(context);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('$brand card added successfully')),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Could not save the card')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
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
                Text('Add New Card', style: AppTextStyles.headlineSmall),
                IconButton(
                  onPressed: () => Navigator.pop(context),
                  icon: const Icon(Icons.close),
                ),
              ],
            ),
            const SizedBox(height: 16),
            if (_detectedBrand != null) ...[
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                decoration: BoxDecoration(
                  color: AppColors.income.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Icon(Icons.credit_card,
                        size: 16, color: AppColors.income),
                    const SizedBox(width: 8),
                    Text(
                      '$_detectedBrand — $_detectedBank',
                      style: const TextStyle(
                          color: AppColors.income,
                          fontWeight: FontWeight.w600,
                          fontSize: 13),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),
            ],
            TextFormField(
              controller: _cardNumberCtrl,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(
                labelText: 'Card number',
                hintText: '1234 5678 9012 3456',
              ),
              validator: _validateCardNumber,
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _expiryCtrl,
              keyboardType: TextInputType.datetime,
              decoration: const InputDecoration(
                labelText: 'Expiry',
                hintText: 'MM/YY',
              ),
              validator: _validateExpiry,
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _nameCtrl,
              textCapitalization: TextCapitalization.words,
              decoration: const InputDecoration(
                labelText: 'Cardholder name',
                hintText: 'Jane Doe',
              ),
              validator: (v) => _validateRequired(v, minLength: 2),
            ),
            const SizedBox(height: 24),
            FilledButton(
              onPressed: _submit,
              child: const Text('Add Card'),
            ),
          ],
        ),
      ),
    );
  }
}
