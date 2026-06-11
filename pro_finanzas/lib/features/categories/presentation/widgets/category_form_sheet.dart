import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../domain/entities/category.dart';

/// Modal bottom sheet for creating or editing a category.
///
/// When [existing] is null, the sheet starts empty and saves a new row.
/// When [existing] is provided, fields are pre-filled and the title says
/// "Edit category". Returns the (name, type, description) the user entered,
/// or null if the user dismissed the sheet.
class CategoryFormSheet extends StatefulWidget {
  const CategoryFormSheet({super.key, this.existing});

  final Category? existing;

  /// Opens the sheet and resolves with the entered values on save, or null
  /// if the user dismissed it without saving.
  static Future<({String name, String type, String? description})?> show(
    BuildContext context, {
    Category? existing,
  }) {
    return showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => CategoryFormSheet(existing: existing),
    );
  }

  @override
  State<CategoryFormSheet> createState() => _CategoryFormSheetState();
}

class _CategoryFormSheetState extends State<CategoryFormSheet> {
  final _formKey = GlobalKey<FormState>();
  late final TextEditingController _nameCtrl;
  late final TextEditingController _descCtrl;
  late String _type;

  @override
  void initState() {
    super.initState();
    _nameCtrl = TextEditingController(text: widget.existing?.name ?? '');
    _descCtrl =
        TextEditingController(text: widget.existing?.description ?? '');
    _type = widget.existing?.type ?? 'EXPENSE';
  }

  @override
  void dispose() {
    _nameCtrl.dispose();
    _descCtrl.dispose();
    super.dispose();
  }

  void _submit() {
    if (!_formKey.currentState!.validate()) return;
    Navigator.pop(context, (
      name: _nameCtrl.text.trim(),
      type: _type,
      description: _descCtrl.text.trim().isEmpty ? null : _descCtrl.text.trim(),
    ));
  }

  @override
  Widget build(BuildContext context) {
    final isEdit = widget.existing != null;
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
                Text(isEdit ? 'Edit category' : 'New category',
                    style: AppTextStyles.headlineSmall),
                IconButton(
                  onPressed: () => Navigator.pop(context),
                  icon: const Icon(Icons.close),
                ),
              ],
            ),
            const SizedBox(height: 16),
            // Type toggle (only editable when creating; for edits the type is
            // tied to existing transactions and changing it would break them).
            Row(
              children: ['expense', 'income'].map((t) {
                final active = _type == t;
                return Expanded(
                  child: Padding(
                    padding: EdgeInsets.only(right: t == 'expense' ? 8 : 0),
                    child: GestureDetector(
                      onTap: isEdit
                          ? null
                          : () => setState(() {
                                _type = t;
                              }),
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
              controller: _nameCtrl,
              decoration: const InputDecoration(labelText: 'Name'),
              textCapitalization: TextCapitalization.sentences,
              validator: (v) {
                final t = v?.trim() ?? '';
                if (t.isEmpty) return 'Required';
                if (t.length < 3) return 'At least 3 characters';
                return null;
              },
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _descCtrl,
              decoration: const InputDecoration(
                labelText: 'Description (optional)',
              ),
              maxLines: 2,
              minLines: 1,
            ),
            const SizedBox(height: 24),
            FilledButton(
              onPressed: _submit,
              child: Text(isEdit ? 'Save changes' : 'Create category'),
            ),
          ],
        ),
      ),
    );
  }
}
