---
name: design-frontend
description: Apply the ProFinancas UI design system to Flutter screens and widgets. Use when building any new screen, widget, or UI component — enforces the exact color palette, typography, button variants, card styles, bottom navigation, and screen layout patterns from the design mockups.
---

# ProFinancas — Frontend Design System

Apply this design system to every Flutter screen and widget in `pro_finanzas/`.

- Screen-by-screen layout reference → [screens.md](screens.md)
- Component code examples → [components.md](components.md)

---

## Color Tokens

Define once in `lib/core/theme/app_colors.dart`:

```dart
class AppColors {
  // Primary — deep navy blue
  static const primary    = Color(0xFF1A237E);
  static const primary700 = Color(0xFF283593);
  static const primary500 = Color(0xFF3949AB);
  static const primary300 = Color(0xFF7986CB);
  static const primary100 = Color(0xFFC5CAE9);

  // Secondary — deep green (income)
  static const secondary    = Color(0xFF2E7D32);
  static const secondary500 = Color(0xFF43A047);
  static const secondary100 = Color(0xFFC8E6C9);

  // Tertiary — deep red (expense / error)
  static const tertiary    = Color(0xFFC62828);
  static const tertiary500 = Color(0xFFE53935);
  static const tertiary100 = Color(0xFFFFCDD2);

  // Neutral
  static const neutral    = Color(0xFFF5F5F5);
  static const neutral700 = Color(0xFF616161);
  static const neutral400 = Color(0xFFBDBDBD);
  static const neutral100 = Color(0xFFF5F5F5);
  static const black      = Color(0xFF212121);
  static const white      = Color(0xFFFFFFFF);

  // Semantic aliases
  static const income  = secondary;
  static const expense = tertiary;
  static const surface = white;
  static const background = neutral100;
}
```

---

## Typography

Font: **Inter** (add to `pubspec.yaml` via `google_fonts: ^6.x`).
Three scales used across all screens:

| Scale | Size | Weight | Usage |
|---|---|---|---|
| Headline | 28–32 sp | Bold (700) | Screen titles, large numbers |
| Body | 14–16 sp | Regular (400) | List items, descriptions |
| Label | 11–12 sp | Medium (500) | Section headers (ALL CAPS), chips, captions |

```dart
// lib/core/theme/app_text_styles.dart
class AppTextStyles {
  static final headline = GoogleFonts.inter(fontSize: 28, fontWeight: FontWeight.w700, color: AppColors.black);
  static final headlineLarge = GoogleFonts.inter(fontSize: 32, fontWeight: FontWeight.w800, color: AppColors.black);
  static final body = GoogleFonts.inter(fontSize: 15, fontWeight: FontWeight.w400, color: AppColors.black);
  static final bodySecondary = GoogleFonts.inter(fontSize: 13, fontWeight: FontWeight.w400, color: AppColors.neutral700);
  static final label = GoogleFonts.inter(fontSize: 11, fontWeight: FontWeight.w500, color: AppColors.neutral700, letterSpacing: 1.2);
  static final amount = GoogleFonts.inter(fontSize: 34, fontWeight: FontWeight.w800, color: AppColors.black);
  static final amountSmall = GoogleFonts.inter(fontSize: 16, fontWeight: FontWeight.w700);
}
```

---

## Button Variants

4 variants — never invent others:

| Variant | Flutter widget | Style |
|---|---|---|
| Primary | `FilledButton` | bg `primary`, text white |
| Secondary | `OutlinedButton` | border `primary`, text `primary` |
| Inverted | `FilledButton` | bg `black`, text white |
| Outlined | `OutlinedButton` | border `neutral400`, text `black` |

```dart
// Primary (default for main CTAs)
FilledButton(
  style: FilledButton.styleFrom(backgroundColor: AppColors.primary,
    minimumSize: const Size(double.infinity, 52),
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12))),
  onPressed: onPressed,
  child: Text(label, style: AppTextStyles.body.copyWith(color: Colors.white)),
)
```

FAB-style icon buttons use `CircleAvatar` + `IconButton` in the matching color:
- Edit: `primary` bg
- Add/Save: `secondary` bg
- Tag: `tertiary` bg
- Delete: `tertiary500` bg

---

## Cards

All cards share: `color: white`, `borderRadius: 16`, `elevation: 0`, border `neutral400` at 0.5 width.

```dart
Container(
  decoration: BoxDecoration(
    color: AppColors.white,
    borderRadius: BorderRadius.circular(16),
    border: Border.all(color: AppColors.neutral400, width: 0.5),
  ),
  padding: const EdgeInsets.all(20),
  child: child,
)
```

---

## Bottom Navigation

5 tabs: **HOME · EXPENSES · SCANNER · ANALYTICS · WALLETS**

- Center tab (SCANNER) is an elevated `FloatingActionButton` with `primary` background and camera icon
- Active non-center tab: icon + label in `primary`, icon wrapped in filled `primary100` circle
- Inactive: `neutral400`

```dart
BottomNavigationBar(
  type: BottomNavigationBarType.fixed,
  selectedItemColor: AppColors.primary,
  unselectedItemColor: AppColors.neutral400,
  backgroundColor: AppColors.white,
  // index 2 = Scanner rendered as FAB via Stack overlay
)
```

---

## Transaction List Item

Icon (category circle) · Merchant name + date/type · Amount (colored)

```dart
ListTile(
  leading: CircleAvatar(backgroundColor: categoryColor.withOpacity(0.15),
    child: Icon(categoryIcon, color: categoryColor)),
  title: Text(merchantName, style: AppTextStyles.body),
  subtitle: Text('$date · $paymentType', style: AppTextStyles.bodySecondary),
  trailing: Text(
    isExpense ? '− $amount' : '+ $amount',
    style: AppTextStyles.amountSmall.copyWith(
      color: isExpense ? AppColors.expense : AppColors.income)),
)
```

---

## Stat / KPI Card

Used on Dashboard and Analytics screens:

```dart
Column(children: [
  Text('SECTION LABEL', style: AppTextStyles.label),
  const SizedBox(height: 4),
  Text('\$142,580.00', style: AppTextStyles.amount),
  Row(children: [
    _Badge('+12.4%', color: AppColors.secondary),
    Text('Vs. last month', style: AppTextStyles.bodySecondary),
  ]),
])
```

---

## App Bar

Consistent across all authenticated screens:

```dart
AppBar(
  backgroundColor: AppColors.white,
  elevation: 0,
  leading: CircleAvatar(/* user avatar or logo */),
  title: Text('Profiancas', style: AppTextStyles.body.copyWith(fontWeight: FontWeight.w700)),
  actions: [IconButton(icon: const Icon(Icons.notifications_outlined), color: AppColors.black, onPressed: () {})],
)
```

---

## Layout Rules

- Background: `AppColors.background` (`#F5F5F5`) on all scaffold bodies
- Horizontal padding: `24` dp on all screens
- Card gap: `16` dp
- Section label → content gap: `8` dp
- All monetary amounts: use `CurrencyFormatter.format()` from `lib/core/utils/`
- Income amounts: `AppColors.income` (green)
- Expense amounts: `AppColors.expense` (red)
