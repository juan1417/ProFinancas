import 'package:flutter/material.dart';
import '../../domain/entities/transaction.dart';
import '../../../../core/utils/currency_formatter.dart';
import '../../../../core/utils/date_formatter.dart';

class TransactionCard extends StatelessWidget {
  const TransactionCard({super.key, required this.transaction, this.onTap});

  final Transaction transaction;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final isIncome = transaction.isIncome;
    final color = isIncome ? Colors.green : Colors.red;
    final icon = isIncome ? Icons.arrow_downward : Icons.arrow_upward;

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: ListTile(
        onTap: onTap,
        leading: CircleAvatar(
          backgroundColor: color.withOpacity(0.15),
          child: Icon(icon, color: color, size: 20),
        ),
        title: Text(transaction.description,
            maxLines: 1, overflow: TextOverflow.ellipsis),
        subtitle: Text(
          '${transaction.categoryName} · ${DateFormatter.relative(transaction.transactionDate)}',
          style: Theme.of(context).textTheme.bodySmall,
        ),
        trailing: Text(
          CurrencyFormatter.formatSigned(
              isIncome ? transaction.amount : -transaction.amount),
          style: TextStyle(
            color: color,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
    );
  }
}
