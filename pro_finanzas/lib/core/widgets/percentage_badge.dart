import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_text_styles.dart';

class PercentageBadge extends StatelessWidget {
  const PercentageBadge(this.text, {super.key, this.positive = true});

  final String text;
  final bool positive;

  @override
  Widget build(BuildContext context) {
    final color = positive ? AppColors.secondary : AppColors.tertiary;
    final bgColor = positive ? AppColors.secondary100 : AppColors.tertiary100;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: bgColor,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            positive ? Icons.trending_up : Icons.trending_down,
            size: 12,
            color: color,
          ),
          const SizedBox(width: 3),
          Text(
            text,
            style: AppTextStyles.label.copyWith(color: color),
          ),
        ],
      ),
    );
  }
}
