"""Consultas de presupuestos (absorbe budgets/services.py)."""
from django.db.models import Sum


def spent_for_budget(budget) -> float:
    from transactions.models import Transaction
    txs = Transaction.objects.filter(
        user=budget.user,
        type='EXPENSE',
        transaction_date__date__gte=budget.start_date,
        transaction_date__date__lte=budget.end_date,
    )
    if budget.category:
        txs = txs.filter(category=budget.category)
    result = txs.aggregate(total=Sum('amount'))['total']
    return float(result) if result else 0.0
