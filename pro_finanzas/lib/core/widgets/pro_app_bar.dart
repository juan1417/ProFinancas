import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_text_styles.dart';

class ProAppBar extends StatelessWidget implements PreferredSizeWidget {
  const ProAppBar({super.key, this.avatarUrl, this.title = 'Profiancas'});

  final String? avatarUrl;
  final String title;

  @override
  Widget build(BuildContext context) {
    return AppBar(
      backgroundColor: AppColors.white,
      elevation: 0,
      leading: Padding(
        padding: const EdgeInsets.only(left: 16),
        child: CircleAvatar(
          radius: 18,
          backgroundColor: AppColors.primary100,
          backgroundImage:
              avatarUrl != null ? NetworkImage(avatarUrl!) : null,
          child: avatarUrl == null
              ? const Icon(Icons.person, color: AppColors.primary, size: 18)
              : null,
        ),
      ),
      title: Text(title,
          style: AppTextStyles.bodyMedium
              .copyWith(fontWeight: FontWeight.w700, fontSize: 16)),
      actions: [
        IconButton(
          icon: const Icon(Icons.notifications_outlined,
              color: AppColors.black, size: 22),
          onPressed: () {},
        ),
        const SizedBox(width: 8),
      ],
    );
  }

  @override
  Size get preferredSize => const Size.fromHeight(kToolbarHeight);
}
