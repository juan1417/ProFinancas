# Component Code Reference

## AppTheme setup

```dart
// lib/core/theme/app_theme.dart
ThemeData buildAppTheme() => ThemeData(
  useMaterial3: true,
  fontFamily: GoogleFonts.inter().fontFamily,
  colorScheme: ColorScheme.fromSeed(
    seedColor: AppColors.primary,
    primary: AppColors.primary,
    secondary: AppColors.secondary,
    error: AppColors.tertiary,
    surface: AppColors.white,
    background: AppColors.background,
  ),
  scaffoldBackgroundColor: AppColors.background,
  appBarTheme: const AppBarTheme(
    backgroundColor: AppColors.white,
    elevation: 0,
    iconTheme: IconThemeData(color: AppColors.black),
  ),
  cardTheme: CardTheme(
    color: AppColors.white,
    elevation: 0,
    shape: RoundedRectangleBorder(
      borderRadius: BorderRadius.circular(16),
      side: BorderSide(color: AppColors.neutral400, width: 0.5),
    ),
    margin: const EdgeInsets.symmetric(vertical: 8),
  ),
  inputDecorationTheme: InputDecorationTheme(
    filled: true,
    fillColor: AppColors.white,
    border: OutlineInputBorder(
      borderRadius: BorderRadius.circular(12),
      borderSide: BorderSide(color: AppColors.neutral400),
    ),
    enabledBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(12),
      borderSide: BorderSide(color: AppColors.neutral400),
    ),
  ),
);
```

## ProFinancasAppBar

```dart
class ProFinancasAppBar extends StatelessWidget implements PreferredSizeWidget {
  const ProFinancasAppBar({super.key, this.avatarUrl});
  final String? avatarUrl;

  @override
  Widget build(BuildContext context) => AppBar(
    leading: Padding(
      padding: const EdgeInsets.all(8),
      child: CircleAvatar(
        backgroundImage: avatarUrl != null ? NetworkImage(avatarUrl!) : null,
        backgroundColor: AppColors.primary100,
        child: avatarUrl == null ? const Icon(Icons.person, color: AppColors.primary) : null,
      ),
    ),
    title: Text('Profiancas',
        style: AppTextStyles.body.copyWith(fontWeight: FontWeight.w700)),
    actions: [
      IconButton(
        icon: const Icon(Icons.notifications_outlined),
        onPressed: () {},
      ),
    ],
  );

  @override
  Size get preferredSize => const Size.fromHeight(kToolbarHeight);
}
```

## ProBottomNavBar (5-tab with center FAB)

```dart
class ProBottomNavBar extends StatelessWidget {
  const ProBottomNavBar({super.key, required this.currentIndex, required this.onTap});
  final int currentIndex;
  final ValueChanged<int> onTap;

  @override
  Widget build(BuildContext context) {
    return Stack(
      clipBehavior: Clip.none,
      children: [
        BottomNavigationBar(
          currentIndex: currentIndex == 2 ? 0 : currentIndex,
          onTap: onTap,
          type: BottomNavigationBarType.fixed,
          selectedItemColor: AppColors.primary,
          unselectedItemColor: AppColors.neutral400,
          backgroundColor: AppColors.white,
          items: const [
            BottomNavigationBarItem(icon: Icon(Icons.home_outlined), activeIcon: Icon(Icons.home), label: 'HOME'),
            BottomNavigationBarItem(icon: Icon(Icons.receipt_long_outlined), label: 'EXPENSES'),
            BottomNavigationBarItem(icon: SizedBox.shrink(), label: ''),  // placeholder for FAB
            BottomNavigationBarItem(icon: Icon(Icons.analytics_outlined), label: 'ANALYTICS'),
            BottomNavigationBarItem(icon: Icon(Icons.account_balance_wallet_outlined), label: 'WALLETS'),
          ],
        ),
        Positioned(
          top: -20,
          left: 0,
          right: 0,
          child: Center(
            child: FloatingActionButton(
              backgroundColor: AppColors.primary,
              onPressed: () => onTap(2),
              child: const Icon(Icons.camera_alt, color: Colors.white),
            ),
          ),
        ),
      ],
    );
  }
}
```

## AppCard (reusable white card)

```dart
class AppCard extends StatelessWidget {
  const AppCard({super.key, required this.child, this.padding});
  final Widget child;
  final EdgeInsets? padding;

  @override
  Widget build(BuildContext context) => Container(
    width: double.infinity,
    padding: padding ?? const EdgeInsets.all(20),
    decoration: BoxDecoration(
      color: AppColors.white,
      borderRadius: BorderRadius.circular(16),
      border: Border.all(color: AppColors.neutral400, width: 0.5),
    ),
    child: child,
  );
}
```

## SegmentedTabs (Today / This week / This month)

```dart
class SegmentedTabs extends StatelessWidget {
  const SegmentedTabs({super.key, required this.options, required this.selected, required this.onSelect});
  final List<String> options;
  final String selected;
  final ValueChanged<String> onSelect;

  @override
  Widget build(BuildContext context) => Row(
    children: options.map((o) {
      final active = o == selected;
      return Padding(
        padding: const EdgeInsets.only(right: 8),
        child: active
          ? FilledButton(
              style: FilledButton.styleFrom(backgroundColor: AppColors.primary,
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8)),
              onPressed: () => onSelect(o),
              child: Text(o))
          : OutlinedButton(
              style: OutlinedButton.styleFrom(
                side: const BorderSide(color: AppColors.neutral400),
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8)),
              onPressed: () => onSelect(o),
              child: Text(o, style: const TextStyle(color: AppColors.black))),
      );
    }).toList(),
  );
}
```

## PercentageBadge (e.g. "+12.4%")

```dart
class PercentageBadge extends StatelessWidget {
  const PercentageBadge(this.text, {super.key, this.positive = true});
  final String text;
  final bool positive;

  @override
  Widget build(BuildContext context) => Container(
    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
    decoration: BoxDecoration(
      color: positive ? AppColors.secondary100 : AppColors.tertiary100,
      borderRadius: BorderRadius.circular(20),
    ),
    child: Row(mainAxisSize: MainAxisSize.min, children: [
      Icon(positive ? Icons.trending_up : Icons.trending_down,
          size: 12, color: positive ? AppColors.secondary : AppColors.tertiary),
      const SizedBox(width: 4),
      Text(text, style: AppTextStyles.label.copyWith(
          color: positive ? AppColors.secondary : AppColors.tertiary)),
    ]),
  );
}
```

## BankCard widget

```dart
class BankCard extends StatelessWidget {
  const BankCard({super.key, required this.cardNumber, required this.holderName, required this.cardName});
  final String cardNumber;
  final String holderName;
  final String cardName;

  @override
  Widget build(BuildContext context) => Container(
    height: 190,
    decoration: BoxDecoration(
      color: AppColors.primary,
      borderRadius: BorderRadius.circular(20),
    ),
    padding: const EdgeInsets.all(24),
    child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
        Text(cardName, style: AppTextStyles.label.copyWith(color: Colors.white70)),
        const Icon(Icons.wifi_tethering, color: Colors.white70),
      ]),
      const Spacer(),
      Text(cardNumber, style: AppTextStyles.body.copyWith(color: Colors.white, letterSpacing: 4)),
      const SizedBox(height: 8),
      Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
        Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text('CARD HOLDER', style: AppTextStyles.label.copyWith(color: Colors.white54)),
          Text(holderName, style: AppTextStyles.body.copyWith(color: Colors.white, fontWeight: FontWeight.w600)),
        ]),
        Switch(value: true, onChanged: (_) {}, activeColor: AppColors.secondary),
      ]),
    ]),
  );
}
```
