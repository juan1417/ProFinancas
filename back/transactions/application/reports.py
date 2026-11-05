"""Reportes y agregados de transacciones (absorbe el antiguo TransactionService)."""
from django.db.models import Sum, Count
from django.utils import timezone
from datetime import timedelta

from ..domain.dates import parse_day
from ..domain.errors import DomainError


def _coerce_bound(value, *, end_of_day: bool):
    """Compat: delega en la aplicación y traduce errores de dominio a ValueError.

    Se mantiene con esta firma para no romper llamadas externas; el código
    interno usa `application.dates.aware_bound`.
    """
    try:
        from .dates import aware_bound
        return aware_bound(value, end_of_day=end_of_day)
    except DomainError as exc:
        raise ValueError(str(exc))


class CategoryReportService:
    @staticmethod
    def get_by_type(queryset, category_type=None):
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


class TransactionReportService:
    @staticmethod
    def get_summary(queryset, period='month', start_date=None, end_date=None):
        if start_date and end_date:
            from .dates import aware_bound
            scoped = queryset.filter(
                transaction_date__gte=aware_bound(start_date, end_of_day=False),
                transaction_date__lte=aware_bound(end_date, end_of_day=True),
            )
        else:
            periods = {'week': 7, 'year': 365}
            days = periods.get(period, 30)
            scoped = queryset.filter(
                transaction_date__gte=timezone.now() - timedelta(days=days)
            )

        income_total = scoped.filter(type='INCOME').aggregate(total=Sum('amount'))['total'] or 0
        expense_total = scoped.filter(type='EXPENSE').aggregate(total=Sum('amount'))['total'] or 0

        by_category = list(
            scoped.values('category__id', 'category__name', 'category__type')
            .annotate(total_amount=Sum('amount'), transaction_count=Count('id'))
            .order_by('-total_amount')
        )

        return {
            'period': period,
            'total_income': float(income_total),
            'total_expenses': float(expense_total),
            'balance': float(income_total - expense_total),
            'transaction_count': scoped.count(),
            'income_count': scoped.filter(type='INCOME').count(),
            'expense_count': scoped.filter(type='EXPENSE').count(),
            'by_category': by_category,
        }

    @staticmethod
    def get_by_category(queryset, transaction_type=None, start_date=None, end_date=None):
        start = None
        end = None
        if start_date:
            from .dates import aware_bound
            start = aware_bound(start_date, end_of_day=False)
        if end_date:
            from .dates import aware_bound
            end = aware_bound(end_date, end_of_day=True)

        if start and end and start > end:
            raise DomainError(
                'start_date must be on or before end_date. '
                f'Got start_date={start_date}, end_date={end_date}.'
            )

        if transaction_type in ['INCOME', 'EXPENSE']:
            queryset = queryset.filter(type=transaction_type)
        if start:
            queryset = queryset.filter(transaction_date__gte=start)
        if end:
            queryset = queryset.filter(transaction_date__lte=end)

        return queryset.values(
            'category__id', 'category__name', 'category__type',
        ).annotate(
            total_amount=Sum('amount'),
            transaction_count=Count('id'),
        ).order_by('-total_amount')
