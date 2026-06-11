import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../../../core/utils/currency_formatter.dart';
import '../../../../core/widgets/app_card.dart';
import '../../../../core/widgets/pro_app_bar.dart';
import '../../../transactions/domain/entities/category.dart';
import '../../../transactions/presentation/providers/transaction_provider.dart';

class ScannerScreen extends StatefulWidget {
  const ScannerScreen({super.key});

  @override
  State<ScannerScreen> createState() => _ScannerScreenState();
}

class _ScannerScreenState extends State<ScannerScreen> {
  // Stub data shown in the "Extracted Data" card. In a real flow this would
  // come from `InvoiceScannerService.extractTotalFromImage`. Kept here so
  // the UI flow (including "save as transaction") is end-to-end testable
  // without a camera or real image.
  static const double _extractedTotal = 142.50;
  static const String _extractedMerchant = 'Sample Merchant Inc.';
  static final DateTime _extractedDate = DateTime(2026, 3, 23);

  bool _scanned = false;

  @override
  void initState() {
    super.initState();
    // Pre-load expense categories so the "save as transaction" picker has
    // data to show even if the user lands here directly.
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<TransactionProvider>().loadCategories(type: 'EXPENSE');
    });
  }

  Future<void> _saveAsTransaction() async {
    final provider = context.read<TransactionProvider>();
    // Make sure categories are loaded in case the post-frame callback above
    // ran before the provider was ready (e.g. the user opened the tab
    // before login completed).
    if (provider.categories.isEmpty) {
      await provider.loadCategories(type: 'EXPENSE');
    }
    if (!mounted) return;

    final expenseCategories = provider.categories
        .where((c) => c.type.toUpperCase() == 'EXPENSE')
        .toList();

    if (expenseCategories.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text(
            'No expense categories found. Create one in the categories screen first.',
          ),
        ),
      );
      return;
    }

    final selected = await showModalBottomSheet<Category>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => _CategoryPickerSheet(categories: expenseCategories),
    );
    if (selected == null || !mounted) return;

    final ok = await provider.addTransaction(
      categoryId: selected.id,
      type: 'EXPENSE',
      amount: _extractedTotal,
      description: _extractedMerchant,
      transactionDate: _extractedDate,
    );
    if (!mounted) return;

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(ok
            ? 'Saved invoice as expense in ${selected.name}'
            : provider.error ?? 'Could not save the transaction.'),
        backgroundColor: ok ? AppColors.income : AppColors.expense,
      ),
    );
    if (ok) setState(() => _scanned = false);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: const ProAppBar(title: 'Invoice Scanner'),
      body: ListView(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
        children: [
          // Camera viewfinder
          Container(
            height: 260,
            decoration: BoxDecoration(
              color: AppColors.black,
              borderRadius: BorderRadius.circular(20),
            ),
            child: Stack(
              children: [
                // Simulated viewfinder
                Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Icon(Icons.camera_alt,
                          color: Colors.white54, size: 56),
                      const SizedBox(height: 12),
                      Text(
                        'Point camera at an invoice',
                        style: AppTextStyles.body
                            .copyWith(color: Colors.white70),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        'Auto-capture when detected',
                        style: AppTextStyles.bodySecondary
                            .copyWith(color: Colors.white38),
                      ),
                    ],
                  ),
                ),
                // Corner markers
                ..._buildCorners(),
                // Scan line animation placeholder
                Positioned(
                  left: 40,
                  right: 40,
                  top: 80,
                  child: Container(
                    height: 2,
                    decoration: const BoxDecoration(
                      gradient: LinearGradient(
                        colors: [
                          Colors.transparent,
                          AppColors.primary300,
                          Colors.transparent
                        ],
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),

          // Action row
          Row(
            children: [
              Expanded(
                child: FilledButton.icon(
                  style: FilledButton.styleFrom(
                    backgroundColor: AppColors.primary,
                    minimumSize: const Size(0, 52),
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12)),
                  ),
                  onPressed: () => setState(() => _scanned = true),
                  icon: const Icon(Icons.camera_alt, size: 18),
                  label: const Text('Scan Invoice'),
                ),
              ),
              const SizedBox(width: 12),
              OutlinedButton(
                style: OutlinedButton.styleFrom(
                  minimumSize: const Size(52, 52),
                  padding: EdgeInsets.zero,
                  side: const BorderSide(color: AppColors.neutral400),
                  shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12)),
                ),
                onPressed: () {},
                child: const Icon(Icons.photo_library_outlined,
                    color: AppColors.black, size: 22),
              ),
            ],
          ),

          if (_scanned) ...[
            const SizedBox(height: 24),
            Text('EXTRACTED DATA', style: AppTextStyles.label),
            const SizedBox(height: 8),
            AppCard(
              child: Column(
                children: [
                  _DataRow('Merchant', _extractedMerchant),
                  const Divider(height: 24),
                  _DataRow('Date', 'Mar 23, 2026'),
                  const Divider(height: 24),
                  _DataRow('Total', CurrencyFormatter.format(_extractedTotal)),
                  const Divider(height: 24),
                  _DataRow('Type', 'Expense'),
                  const SizedBox(height: 20),
                  Row(children: [
                    Expanded(
                      child: FilledButton(
                        style: FilledButton.styleFrom(
                          backgroundColor: AppColors.secondary,
                          minimumSize: const Size(0, 48),
                          shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(10)),
                        ),
                        onPressed: _saveAsTransaction,
                        child: const Text('Confirm & Save'),
                      ),
                    ),
                    const SizedBox(width: 12),
                    OutlinedButton(
                      style: OutlinedButton.styleFrom(
                        minimumSize: const Size(48, 48),
                        padding: EdgeInsets.zero,
                        side: const BorderSide(color: AppColors.neutral400),
                        shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(10)),
                      ),
                      onPressed: () => setState(() => _scanned = false),
                      child: const Icon(Icons.close,
                          color: AppColors.neutral700),
                    ),
                  ]),
                ],
              ),
            ),
          ],

          const SizedBox(height: 24),
          Text('RECENT SCANS', style: AppTextStyles.label),
          const SizedBox(height: 8),
          ...List.generate(
            3,
            (i) => Padding(
              padding: const EdgeInsets.only(bottom: 8),
              child: AppCard(
                padding:
                    const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
                child: Row(
                  children: [
                    Container(
                      width: 40,
                      height: 40,
                      decoration: BoxDecoration(
                        color: AppColors.primary50,
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: const Icon(Icons.receipt,
                          color: AppColors.primary, size: 20),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Invoice #${1000 + i}',
                              style: AppTextStyles.bodyMedium),
                          Text('Mar ${20 - i}, 2026',
                              style: AppTextStyles.bodySecondary),
                        ],
                      ),
                    ),
                    Text(
                      '\$${(80 + i * 30).toStringAsFixed(2)}',
                      style: AppTextStyles.amountSmall
                          .copyWith(color: AppColors.expense),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  List<Widget> _buildCorners() {
    const size = 20.0;
    const thickness = 3.0;
    const color = AppColors.primary300;
    const r = 4.0;

    return [
      Positioned(
        top: 16,
        left: 16,
        child: _Corner(size: size, t: thickness, c: color, r: r,
            top: true, left: true),
      ),
      Positioned(
        top: 16,
        right: 16,
        child: _Corner(size: size, t: thickness, c: color, r: r,
            top: true, left: false),
      ),
      Positioned(
        bottom: 16,
        left: 16,
        child: _Corner(size: size, t: thickness, c: color, r: r,
            top: false, left: true),
      ),
      Positioned(
        bottom: 16,
        right: 16,
        child: _Corner(size: size, t: thickness, c: color, r: r,
            top: false, left: false),
      ),
    ];
  }
}

class _Corner extends StatelessWidget {
  const _Corner(
      {required this.size,
      required this.t,
      required this.c,
      required this.r,
      required this.top,
      required this.left});

  final double size, t, r;
  final Color c;
  final bool top, left;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: size,
      height: size,
      child: CustomPaint(
        painter: _CornerPainter(
            thickness: t, color: c, top: top, left: left, radius: r),
      ),
    );
  }
}

class _CornerPainter extends CustomPainter {
  const _CornerPainter(
      {required this.thickness,
      required this.color,
      required this.top,
      required this.left,
      required this.radius});

  final double thickness, radius;
  final Color color;
  final bool top, left;

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..strokeWidth = thickness
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round;

    final path = Path();
    if (top && left) {
      path
        ..moveTo(0, size.height)
        ..lineTo(0, radius)
        ..arcToPoint(Offset(radius, 0), radius: Radius.circular(radius))
        ..lineTo(size.width, 0);
    } else if (top && !left) {
      path
        ..moveTo(0, 0)
        ..lineTo(size.width - radius, 0)
        ..arcToPoint(Offset(size.width, radius),
            radius: Radius.circular(radius))
        ..lineTo(size.width, size.height);
    } else if (!top && left) {
      path
        ..moveTo(size.width, size.height)
        ..lineTo(radius, size.height)
        ..arcToPoint(Offset(0, size.height - radius),
            radius: Radius.circular(radius))
        ..lineTo(0, 0);
    } else {
      path
        ..moveTo(0, size.height)
        ..lineTo(size.width - radius, size.height)
        ..arcToPoint(Offset(size.width, size.height - radius),
            radius: Radius.circular(radius))
        ..lineTo(size.width, 0);
    }
    canvas.drawPath(path, paint);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class _DataRow extends StatelessWidget {
  const _DataRow(this.label, this.value);
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(label, style: AppTextStyles.bodySecondary),
        Text(value, style: AppTextStyles.bodyMedium),
      ],
    );
  }
}

// ── Category picker (used by "Confirm & Save") ────────────────────────────────
class _CategoryPickerSheet extends StatelessWidget {
  const _CategoryPickerSheet({required this.categories});
  final List<Category> categories;

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
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('Pick a category', style: AppTextStyles.headlineSmall),
              IconButton(
                onPressed: () => Navigator.pop(context),
                icon: const Icon(Icons.close),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            'Choose the expense category for this invoice.',
            style: AppTextStyles.bodySecondary,
          ),
          const SizedBox(height: 16),
          Flexible(
            child: ListView.separated(
              shrinkWrap: true,
              itemCount: categories.length,
              separatorBuilder: (_, __) => const Divider(height: 1),
              itemBuilder: (_, i) {
                final c = categories[i];
                return ListTile(
                  contentPadding: EdgeInsets.zero,
                  title: Text(c.name, style: AppTextStyles.bodyMedium),
                  trailing: const Icon(Icons.chevron_right,
                      color: AppColors.neutral500),
                  onTap: () => Navigator.pop(context, c),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
