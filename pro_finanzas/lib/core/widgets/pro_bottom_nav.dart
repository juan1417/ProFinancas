import 'package:flutter/material.dart';
import '../theme/app_colors.dart';

class ProBottomNav extends StatelessWidget {
  const ProBottomNav({
    super.key,
    required this.currentIndex,
    required this.onTap,
  });

  final int currentIndex;
  final ValueChanged<int> onTap;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 70,
      child: Stack(
        clipBehavior: Clip.none,
        children: [
          Container(
            decoration: const BoxDecoration(
              color: AppColors.white,
              border: Border(
                  top: BorderSide(color: AppColors.neutral200, width: 1)),
            ),
            child: Row(
              children: [
                _NavItem(
                    icon: Icons.home_outlined,
                    activeIcon: Icons.home,
                    label: 'HOME',
                    index: 0,
                    currentIndex: currentIndex,
                    onTap: onTap),
                _NavItem(
                    icon: Icons.receipt_long_outlined,
                    activeIcon: Icons.receipt_long,
                    label: 'EXPENSES',
                    index: 1,
                    currentIndex: currentIndex,
                    onTap: onTap),
                const Expanded(child: SizedBox()), // space for center FAB
                _NavItem(
                    icon: Icons.analytics_outlined,
                    activeIcon: Icons.analytics,
                    label: 'ANALYTICS',
                    index: 3,
                    currentIndex: currentIndex,
                    onTap: onTap),
                _NavItem(
                    icon: Icons.account_balance_wallet_outlined,
                    activeIcon: Icons.account_balance_wallet,
                    label: 'WALLETS',
                    index: 4,
                    currentIndex: currentIndex,
                    onTap: onTap),
              ],
            ),
          ),
          // Center FAB — Scanner
          Positioned(
            top: -20,
            left: 0,
            right: 0,
            child: Center(
              child: GestureDetector(
                onTap: () => onTap(2),
                child: Container(
                  width: 56,
                  height: 56,
                  decoration: BoxDecoration(
                    color: currentIndex == 2
                        ? AppColors.primary700
                        : AppColors.primary,
                    shape: BoxShape.circle,
                    boxShadow: [
                      BoxShadow(
                        color: AppColors.primary.withOpacity(0.35),
                        blurRadius: 12,
                        offset: const Offset(0, 4),
                      ),
                    ],
                  ),
                  child: const Icon(Icons.camera_alt,
                      color: AppColors.white, size: 24),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _NavItem extends StatelessWidget {
  const _NavItem({
    required this.icon,
    required this.activeIcon,
    required this.label,
    required this.index,
    required this.currentIndex,
    required this.onTap,
  });

  final IconData icon;
  final IconData activeIcon;
  final String label;
  final int index;
  final int currentIndex;
  final ValueChanged<int> onTap;

  @override
  Widget build(BuildContext context) {
    final active = index == currentIndex;
    return Expanded(
      child: GestureDetector(
        behavior: HitTestBehavior.opaque,
        onTap: () => onTap(index),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            active
                ? Container(
                    padding: const EdgeInsets.all(6),
                    decoration: BoxDecoration(
                      color: AppColors.primary50,
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Icon(activeIcon,
                        color: AppColors.primary, size: 20),
                  )
                : Icon(icon, color: AppColors.neutral500, size: 20),
            const SizedBox(height: 4),
            Text(
              label,
              style: TextStyle(
                fontSize: 9,
                fontWeight:
                    active ? FontWeight.w600 : FontWeight.w400,
                color: active ? AppColors.primary : AppColors.neutral500,
                letterSpacing: 0.5,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
