import pytest
from decimal import Decimal
from datetime import date, timedelta
from rest_framework.test import APITestCase, APIClient
from rest_framework import status
from django.utils import timezone

from usuarios.models import User
from transactions.models import Category, Transaction
from budgets.models import Budget


@pytest.mark.django_db
class BudgetAlertsTest(APITestCase):
    def setUp(self):
        self.client = APIClient()
        self.user = User.objects.create_user(
            email='al@t.com', username='alerts', password='TestPass123!'
        )
        self.client.force_authenticate(user=self.user)
        today = timezone.localdate()
        self.cat = Category.objects.create(name='Compras', type='EXPENSE', user=self.user)
        self.budget = Budget.objects.create(
            user=self.user, category=self.cat, amount=Decimal('100.00'),
            period='monthly', start_date=today - timedelta(days=5), end_date=today + timedelta(days=5),
        )
        self.url = '/api/budgets/alerts/'

    def _add_expense(self, amount):
        Transaction.objects.create(
            user=self.user, category=self.cat, type='EXPENSE',
            amount=Decimal(amount), description='g',
            transaction_date=timezone.now(),
        )

    def _alert_for_budget(self, data):
        return next(a for a in data if a['budget_id'] == self.budget.id)

    def test_ok_bajo_umbral(self):
        self._add_expense('10.00')  # 10% usado
        res = self.client.get(self.url)
        self.assertEqual(res.status_code, status.HTTP_200_OK)
        a = self._alert_for_budget(res.data)
        self.assertEqual(a['status'], 'ok')
        self.assertAlmostEqual(a['spent'], 10.0, places=2)

    def test_warning_sobre_80(self):
        self._add_expense('85.00')
        res = self.client.get(self.url)
        a = self._alert_for_budget(res.data)
        self.assertEqual(a['status'], 'warning')

    def test_exceeded_sobre_100(self):
        self._add_expense('150.00')
        res = self.client.get(self.url)
        a = self._alert_for_budget(res.data)
        self.assertEqual(a['status'], 'exceeded')

    def test_solo_presupuestos_propios(self):
        other = User.objects.create_user(email='o2@t.com', username='o2', password='TestPass123!')
        c2 = APIClient(); c2.force_authenticate(user=other)
        res = c2.get(self.url)
        self.assertEqual(res.status_code, status.HTTP_200_OK)
        self.assertEqual(len(res.data), 0)
