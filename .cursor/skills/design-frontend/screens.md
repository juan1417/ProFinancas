# Screen Layout Reference

## Authentication Screen

- White card centered on `neutral100` background
- **"Profiancas"** headline (`headlineLarge`) + red bottom border accent (`tertiary`, 2dp)
- Tagline in `bodySecondary`
- Tab row: **Login** (underlined `primary`) | **Register** (plain `neutral700`)
- `ALL CAPS` field labels in `label` style
- Text fields: `OutlineInputBorder` radius 8, border `neutral400`
- Primary CTA: full-width `FilledButton` "Enter Workspace →"
- "OR BIOMETRIC SECURE" divider with centered text
- Touch ID / Face ID: two equal `OutlinedButton` cards side by side
- Bottom link "Need assistance? Contact our **Private Support**" (bold `primary`)

---

## Dashboard Screen

Sections top → bottom:

1. **App Bar** (avatar + title + bell)
2. **Portfolio Valuation card** (white card, full width)
   - `PORTFOLIO VALUATION` label (ALL CAPS)
   - Giant amount `$142,580.00` in `amount` style
   - `_Badge` widget: green chip "+12.4%" + `bodySecondary` comparison text
3. **Monthly Budget card**
   - `MONTHLY BUDGET` label
   - `$4,200 / $5,000` + percentage in `tertiary`
   - `LinearProgressIndicator` green → red gradient
   - Warning text in `bodySecondary`
4. **Expense Allocation card**
   - `EXPENSE ALLOCATION` label
   - Donut chart (use `fl_chart`) with center text `$3.2k TOTAL OUT`
   - FAB "+" overlaid bottom-right corner (`primary` circle)
5. **Bottom Navigation** (5-tab with center FAB)

---

## Expense Manager Screen

1. **App Bar**
2. Screen title "Expenses" (`headlineLarge`) + subtitle (`bodySecondary`)
3. **Search bar** — rounded `TextFormField` with search icon prefix
4. **Segmented tabs**: Today | This week | This month
   - Active: `FilledButton` style `primary`
   - Inactive: `OutlinedButton` style neutral
5. **Section header row**: "RECENT MOVEMENTS" (`label`) + "View Reports" (`primary` text link)
6. **Transaction list** — `ListView` of transaction list items (see SKILL.md)
   - Each item has category icon circle, merchant, date, amount

---

## Invoice Scanner Screen

1. **App Bar**
2. Full-width camera preview (`ClipRRect` radius 16)
   - Centered camera icon + "Analyzing Receipt..." overlay
3. **Review card** (white card):
   - "Review Scan" heading + "HIGH CONFIDENCE" green chip
   - Field rows: label (`label` style) + value (`body`) + edit icon
     - MERCHANT: Whole Foods Market
     - DATE: Oct 24, 2023
4. **Bottom Navigation**

---

## Spreadsheet Analysis Screen

1. **App Bar**
2. `ANALYTICAL LEDGER` label (ALL CAPS) + "Pro Analysis" headline
3. **Action row**: "Export CSV" (`OutlinedButton`) + "Add Transaction" (`FilledButton`)
4. **Stat rows** (inside white card, divider between each):
   - Label (ALL CAPS) + large number + status icon (check = green, warning = red)
5. **Search + Filters row**: `TextFormField` search + "Advanced Filters" text button
6. **Bottom Navigation** (ANALYTICS tab active)

---

## Wallet Management Screen

1. **App Bar**
2. "Your Wallet." (`headlineLarge`, bold)
3. **"Link New Bank"** full-width `OutlinedButton` with bank icon
4. **Active Cards section**:
   - Section title + `<` `>` navigation arrows
   - Bank card widget: dark navy `Container` radius 20, card number masked, holder name, NFC icon, toggle switch
5. **Card Usage section** (white card):
   - Title + subtitle
   - Toggle row: WEEKLY | MONTHLY (same pattern as segmented tabs)
   - Usage chart placeholder
6. **Bottom Navigation** (WALLETS tab active)

---

## Shared Patterns

### Section Label
```dart
Text('SECTION TITLE', style: AppTextStyles.label)
```

### Badge / Chip
```dart
Container(
  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
  decoration: BoxDecoration(color: AppColors.secondary100,
    borderRadius: BorderRadius.circular(20)),
  child: Text('+12.4%', style: AppTextStyles.label.copyWith(color: AppColors.secondary)),
)
```

### Segmented Tabs (Today / This week / This month)
```dart
Row(children: periods.map((p) => _SegmentTab(label: p, isActive: selected == p)).toList())

// _SegmentTab: if active → FilledButton(primary), else → OutlinedButton(neutral)
```

### Stat Row (Analytics)
```dart
Padding(
  padding: const EdgeInsets.symmetric(vertical: 16),
  child: Row(children: [
    Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      Text('TOTAL VOLUME', style: AppTextStyles.label),
      const SizedBox(height: 4),
      Text('\$428,950.00', style: AppTextStyles.amount),
    ])),
    Icon(statusIcon, color: statusColor),
  ]),
)
```
