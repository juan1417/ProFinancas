import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../../../core/widgets/pro_app_bar.dart';
import '../../../../core/widgets/segmented_tabs.dart';
import '../../domain/entities/category.dart';
import '../providers/category_provider.dart';
import '../widgets/category_form_sheet.dart';
import '../widgets/category_tile.dart';

/// Full-screen CRUD for categories.
///
/// Layout:
///   - Top: Income / Expense / All segmented tabs
///   - Body: scrollable list of [CategoryTile]s
///   - FAB: opens [CategoryFormSheet] in "create" mode for the currently
///     selected tab's type
///
/// Long-tap a tile to delete with confirmation. Tap the edit icon to open
/// the form in "edit" mode.
class CategoriesScreen extends StatefulWidget {
  const CategoriesScreen({super.key});

  @override
  State<CategoriesScreen> createState() => _CategoriesScreenState();
}

class _CategoriesScreenState extends State<CategoriesScreen> {
  // 'All' | 'Income' | 'Expense'
  String _selected = 'All';

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<CategoryProvider>().loadAll();
    });
  }

  List<Category> _visibleCategories(CategoryProvider p) {
    switch (_selected) {
      case 'Income':
        return p.incomeCategories;
      case 'Expense':
        return p.expenseCategories;
      default:
        return p.all;
    }
  }

  String get _fabType => _selected == 'Income' ? 'INCOME' : 'EXPENSE';

  Future<void> _openCreateSheet() async {
    final result = await CategoryFormSheet.show(context);
    if (result == null || !mounted) return;
    final created = await context.read<CategoryProvider>().create(
          name: result.name,
          type: result.type,
          description: result.description,
        );
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          created != null
              ? 'Category "${created.name}" created'
              : context.read<CategoryProvider>().error ?? 'Could not create',
        ),
        backgroundColor:
            created != null ? AppColors.income : AppColors.expense,
      ),
    );
  }

  Future<void> _openEditSheet(Category existing) async {
    final result = await CategoryFormSheet.show(context, existing: existing);
    if (result == null || !mounted) return;
    final updated = await context.read<CategoryProvider>().update(
          id: existing.id,
          name: result.name,
          type: result.type,
          description: result.description,
        );
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          updated != null
              ? 'Category updated'
              : context.read<CategoryProvider>().error ?? 'Could not update',
        ),
        backgroundColor:
            updated != null ? AppColors.income : AppColors.expense,
      ),
    );
  }

  Future<void> _confirmDelete(Category c) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Delete category?'),
        content: Text(
          c.transactionsCount > 0
              ? '"${c.name}" has ${c.transactionsCount} transactions. '
                  'Deleting it will fail on the server — you\'ll need to '
                  'reassign or delete the transactions first.'
              : '"${c.name}" will be permanently removed.',
        ),
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
    if (confirmed != true || !mounted) return;
    final ok = await context.read<CategoryProvider>().delete(c.id);
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          ok
              ? 'Category removed'
              : context.read<CategoryProvider>().error ?? 'Could not delete',
        ),
        backgroundColor: ok ? AppColors.income : AppColors.expense,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<CategoryProvider>();
    final visible = _visibleCategories(provider);

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: const ProAppBar(title: 'Categories'),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _openCreateSheet,
        backgroundColor: AppColors.primary,
        foregroundColor: AppColors.white,
        icon: const Icon(Icons.add),
        label: Text('New ${_fabType == 'INCOME' ? 'income' : 'expense'}'),
      ),
      body: RefreshIndicator(
        color: AppColors.primary,
        onRefresh: () => context.read<CategoryProvider>().loadAll(),
        child: ListView(
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
          children: [
            SegmentedTabs(
              options: const ['All', 'Income', 'Expense'],
              selected: _selected,
              onSelect: (v) => setState(() => _selected = v),
            ),
            const SizedBox(height: 16),
            if (provider.isLoading && provider.all.isEmpty)
              const Padding(
                padding: EdgeInsets.symmetric(vertical: 48),
                child: Center(
                  child: CircularProgressIndicator(color: AppColors.primary),
                ),
              )
            else if (visible.isEmpty)
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 48),
                child: Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Icon(Icons.label_outline,
                          size: 48, color: AppColors.neutral400),
                      const SizedBox(height: 12),
                      Text(
                        _selected == 'All'
                            ? 'No categories yet'
                            : 'No $_selected categories',
                        style: AppTextStyles.bodySecondary,
                      ),
                      const SizedBox(height: 4),
                      Text(
                        'Tap the button below to add one',
                        style: AppTextStyles.bodySecondary
                            .copyWith(color: AppColors.neutral400),
                      ),
                    ],
                  ),
                ),
              )
            else
              ...visible.map(
                (c) => CategoryTile(
                  category: c,
                  onEdit: () => _openEditSheet(c),
                  onDelete: () => _confirmDelete(c),
                ),
              ),
            const SizedBox(height: 80), // breathing room for the FAB
          ],
        ),
      ),
    );
  }
}
