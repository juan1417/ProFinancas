from django.db.models import Sum, Count
from django.utils import timezone
from datetime import timedelta


class CategoryService:

    @staticmethod
    def get_by_type(queryset, category_type: str | None = None) -> dict:
        """
        Returns categories filtered by type, or grouped by type if no filter given.
        """
        if category_type and category_type in ['INCOME', 'EXPENSE']:
            return {
                'filtered': queryset.filter(type=category_type, is_active=True),
                'grouped': False,
            }
        return {
            'income': queryset.filter(type='INCOME', is_active=True),
            'expense': queryset.filter(type='EXPENSE', is_active=True),
            'grouped': True,
        }


class TransactionService:

    @staticmethod
    def get_summary(queryset, period: str = 'month', start_date=None, end_date=None) -> dict:
        """
        Returns a financial summary dict for the given queryset and time range.
        """
        if start_date and end_date:
            queryset = queryset.filter(
                transaction_date__gte=start_date,
                transaction_date__lte=end_date,
            )
        else:
            periods = {'week': 7, 'year': 365}
            days = periods.get(period, 30)
            queryset = queryset.filter(
                transaction_date__gte=timezone.now() - timedelta(days=days)
            )

        income_total = queryset.filter(type='INCOME').aggregate(total=Sum('amount'))['total'] or 0
        expense_total = queryset.filter(type='EXPENSE').aggregate(total=Sum('amount'))['total'] or 0

        return {
            'period': period,
            'total_income': float(income_total),
            'total_expenses': float(expense_total),
            'balance': float(income_total - expense_total),
            'transaction_count': queryset.count(),
            'income_count': queryset.filter(type='INCOME').count(),
            'expense_count': queryset.filter(type='EXPENSE').count(),
        }

    @staticmethod
    def get_by_category(queryset, transaction_type=None, start_date=None, end_date=None):
        """
        Returns transactions grouped by category with totals.
        """
        # If both dates are provided, sanity-check that they are
        # ordered. Otherwise the result is silently empty and the
        # user has no idea their query was wrong.
        if start_date and end_date and start_date > end_date:
            raise ValueError(
                'start_date must be on or before end_date. '
                f'Got start_date={start_date}, end_date={end_date}.'
            )
        if transaction_type in ['INCOME', 'EXPENSE']:
            queryset = queryset.filter(type=transaction_type)
        if start_date:
            queryset = queryset.filter(transaction_date__gte=start_date)
        if end_date:
            queryset = queryset.filter(transaction_date__lte=end_date)

        return queryset.values(
            'category__id',
            'category__name',
            'category__type',
        ).annotate(
            total_amount=Sum('amount'),
            transaction_count=Count('id'),
        ).order_by('-total_amount')
