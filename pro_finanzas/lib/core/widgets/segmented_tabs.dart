import 'package:flutter/material.dart';
import '../theme/app_colors.dart';

class SegmentedTabs extends StatelessWidget {
  const SegmentedTabs({
    super.key,
    required this.options,
    required this.selected,
    required this.onSelect,
  });

  final List<String> options;
  final String selected;
  final ValueChanged<String> onSelect;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: options.map((option) {
        final active = option == selected;
        return Padding(
          padding: const EdgeInsets.only(right: 8),
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 180),
            child: active
                ? FilledButton(
                    style: FilledButton.styleFrom(
                      backgroundColor: AppColors.primary,
                      padding: const EdgeInsets.symmetric(
                          horizontal: 18, vertical: 10),
                      minimumSize: Size.zero,
                      tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                      shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(20)),
                    ),
                    onPressed: () => onSelect(option),
                    child: Text(option,
                        style: const TextStyle(
                            fontSize: 13, fontWeight: FontWeight.w600)),
                  )
                : OutlinedButton(
                    style: OutlinedButton.styleFrom(
                      foregroundColor: AppColors.black,
                      side: const BorderSide(color: AppColors.neutral400),
                      padding: const EdgeInsets.symmetric(
                          horizontal: 18, vertical: 10),
                      minimumSize: Size.zero,
                      tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                      shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(20)),
                    ),
                    onPressed: () => onSelect(option),
                    child: Text(option,
                        style: const TextStyle(fontSize: 13)),
                  ),
          ),
        );
      }).toList(),
    );
  }
}
