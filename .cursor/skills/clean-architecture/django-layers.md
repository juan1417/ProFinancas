# Django Service Layer Examples

## services.py

Business logic lives here. Raise `ValidationError` or custom exceptions — never return HTTP responses.

```python
from django.core.exceptions import ValidationError
from .models import Transaction, Category


class TransactionService:

    @staticmethod
    def create(data: dict, user) -> Transaction:
        category: Category = data['category']

        if category.user != user:
            raise ValidationError({'category': 'La categoría no pertenece a este usuario.'})

        if not category.is_active:
            raise ValidationError({'category': 'La categoría está inactiva.'})

        if category.type != data['type']:
            raise ValidationError({'type': 'El tipo debe coincidir con el de la categoría.'})

        return Transaction.objects.create(**data, user=user)

    @staticmethod
    def get_summary(user, period: str = 'month') -> dict:
        from django.db.models import Sum
        from django.utils import timezone
        from datetime import timedelta

        periods = {'week': 7, 'month': 30, 'year': 365}
        days = periods.get(period, 30)
        since = timezone.now() - timedelta(days=days)

        qs = Transaction.objects.filter(user=user, transaction_date__gte=since)
        income = qs.filter(type='INCOME').aggregate(t=Sum('amount'))['t'] or 0
        expense = qs.filter(type='EXPENSE').aggregate(t=Sum('amount'))['t'] or 0

        return {
            'period': period,
            'total_income': float(income),
            'total_expenses': float(expense),
            'balance': float(income - expense),
        }
```

## views.py (thin — delegates to service)

```python
from .services import TransactionService

class TransactionViewSet(viewsets.ModelViewSet):

    def perform_create(self, serializer):
        TransactionService.create(serializer.validated_data, user=self.request.user)

    @action(detail=False, methods=['get'])
    def summary(self, request):
        period = request.query_params.get('period', 'month')
        data = TransactionService.get_summary(user=request.user, period=period)
        return Response(data)
```

## tests/test_services.py

Test services directly — no HTTP overhead.

```python
from django.test import TestCase
from django.core.exceptions import ValidationError
from transactions.services import TransactionService


class TransactionServiceTest(TestCase):

    def test_create_fails_when_category_belongs_to_other_user(self):
        # arrange: two users, category owned by user2
        ...
        with self.assertRaises(ValidationError):
            TransactionService.create(data, user=user1)

    def test_get_summary_returns_correct_balance(self):
        ...
        result = TransactionService.get_summary(user, period='month')
        self.assertEqual(result['balance'], expected_balance)
```
