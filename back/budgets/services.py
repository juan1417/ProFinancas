from django.db.models import Sum
from transactions.models import Transaction


class BudgetService:

    @staticmethod
    def get_summary(budget):
        """
        Returns spent amount and remaining for a given budget.
        """
        txs = Transaction.objects.filter(
            user=budget.user,
            type='EXPENSE',
            transaction_date__date__gte=budget.start_date,
            transaction_date__date__lte=budget.end_date,
        )
        if budget.category:
            txs = txs.filter(category=budget.category)

        spent = txs.aggregate(total=Sum('amount'))['total'] or 0
        return {
            'budget_id': budget.id,
            'amount': float(budget.amount),
            'spent': float(spent),
            'remaining': float(budget.amount) - float(spent),
            'percentage': round(float(spent) / float(budget.amount) * 100, 1) if budget.amount else 0,
        }
