import pytest
from decimal import Decimal
from datetime import date, timedelta
from rest_framework.test import APITestCase, APIClient
from rest_framework import status

from usuarios.models import User
from transactions.models import Category, Transaction
from budgets.models import Budget


@pytest.mark.django_db
class BudgetModelTest(APITestCase):

    def setUp(self):
        self.client = APIClient()
        self.user = User.objects.create_user(
            username='budgetuser',
            email='budget@test.com',
            password='testpass123',
        )
        self.client.force_authenticate(user=self.user)
        self.expense_cat = Category.objects.create(
            name='Alimentación', type='EXPENSE', user=self.user,
        )
        self.income_cat = Category.objects.create(
            name='Salario', type='INCOME', user=self.user,
        )

    def test_create_budget_success(self):
        budget = Budget.objects.create(
            user=self.user,
            category=self.expense_cat,
            amount=Decimal('500000.00'),
            period='monthly',
            start_date=date.today().replace(day=1),
            end_date=date.today(),
        )
        self.assertEqual(budget.user, self.user)
        self.assertEqual(budget.amount, Decimal('500000.00'))
        self.assertTrue(budget.is_active)

    def test_budget_str_with_category(self):
        budget = Budget.objects.create(
            user=self.user,
            category=self.expense_cat,
            amount=Decimal('200000.00'),
            period='monthly',
            start_date=date.today().replace(day=1),
            end_date=date.today(),
        )
        self.assertIn('Alimentación', str(budget))
        self.assertIn('Mensual', str(budget))

    def test_budget_str_general(self):
        budget = Budget.objects.create(
            user=self.user,
            category=None,
            amount=Decimal('1000000.00'),
            period='monthly',
            start_date=date.today().replace(day=1),
            end_date=date.today(),
        )
        self.assertIn('General', str(budget))

    def test_budget_different_users_same_category(self):
        user2 = User.objects.create_user(
            username='user2', email='user2@test.com', password='pass123',
        )
        Budget.objects.create(
            user=self.user, category=self.expense_cat,
            amount=Decimal('500000.00'), period='monthly',
            start_date=date.today().replace(day=1), end_date=date.today(),
        )
        budget2 = Budget.objects.create(
            user=user2, category=self.expense_cat,
            amount=Decimal('300000.00'), period='monthly',
            start_date=date.today().replace(day=1), end_date=date.today(),
        )
        self.assertIsNotNone(budget2.id)


@pytest.mark.django_db
class TestBudgetAPI(APITestCase):

    def setUp(self):
        self.client = APIClient()
        self.user = User.objects.create_user(
            username='apitest', email='api@test.com', password='testpass123',
        )
        self.client.force_authenticate(user=self.user)
        self.expense_cat = Category.objects.create(
            name='Transporte', type='EXPENSE', user=self.user,
        )

    def test_create_budget(self):
        url = '/api/budgets/'
        data = {
            'category': self.expense_cat.id,
            'amount': '300000.00',
            'period': 'monthly',
            'start_date': date.today().replace(day=1).isoformat(),
            'end_date': date.today().isoformat(),
        }
        response = self.client.post(url, data, format='json')
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertEqual(float(response.data['amount']), 300000.0)
        self.assertIn('spent', response.data)

    def test_list_budgets(self):
        Budget.objects.create(
            user=self.user, category=self.expense_cat,
            amount=Decimal('200000.00'), period='monthly',
            start_date=date.today().replace(day=1), end_date=date.today(),
        )
        url = '/api/budgets/'
        response = self.client.get(url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 1)

    def test_get_budget_detail(self):
        budget = Budget.objects.create(
            user=self.user, category=self.expense_cat,
            amount=Decimal('200000.00'), period='monthly',
            start_date=date.today().replace(day=1), end_date=date.today(),
        )
        url = f'/api/budgets/{budget.id}/'
        response = self.client.get(url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn('spent', response.data)

    def test_update_budget(self):
        budget = Budget.objects.create(
            user=self.user, category=self.expense_cat,
            amount=Decimal('200000.00'), period='monthly',
            start_date=date.today().replace(day=1), end_date=date.today(),
        )
        url = f'/api/budgets/{budget.id}/'
        data = {
            'amount': '250000.00',
            'period': 'monthly',
            'start_date': date.today().replace(day=1).isoformat(),
            'end_date': date.today().isoformat(),
        }
        response = self.client.put(url, data, format='json')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(float(response.data['amount']), 250000.0)

    def test_delete_budget(self):
        budget = Budget.objects.create(
            user=self.user, category=self.expense_cat,
            amount=Decimal('200000.00'), period='monthly',
            start_date=date.today().replace(day=1), end_date=date.today(),
        )
        url = f'/api/budgets/{budget.id}/'
        response = self.client.delete(url)
        self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)
        self.assertFalse(Budget.objects.filter(id=budget.id).exists())

    def test_budget_spent_calculation(self):
        today = date.today()
        budget = Budget.objects.create(
            user=self.user, category=self.expense_cat,
            amount=Decimal('1000.00'), period='monthly',
            start_date=today.replace(day=1),
            end_date=today,
        )
        Transaction.objects.create(
            user=self.user, category=self.expense_cat, type='EXPENSE',
            amount=Decimal('250.00'), description='Gasto test',
            transaction_date=today,
        )
        Transaction.objects.create(
            user=self.user, category=self.expense_cat, type='EXPENSE',
            amount=Decimal('150.00'), description='Gasto test 2',
            transaction_date=today,
        )
        url = f'/api/budgets/{budget.id}/'
        response = self.client.get(url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['spent'], 400.0)

    def test_budget_filter_by_period(self):
        Budget.objects.create(
            user=self.user, category=self.expense_cat,
            amount=Decimal('50000.00'), period='weekly',
            start_date=date.today() - timedelta(days=7),
            end_date=date.today(),
        )
        Budget.objects.create(
            user=self.user, category=self.expense_cat,
            amount=Decimal('200000.00'), period='monthly',
            start_date=date.today().replace(day=1),
            end_date=date.today(),
        )
        url = '/api/budgets/?period=monthly'
        response = self.client.get(url)
        self.assertEqual(len(response.data), 1)
        self.assertEqual(response.data[0]['period'], 'monthly')

    def test_budget_end_date_before_start_date(self):
        url = '/api/budgets/'
        data = {
            'category': self.expense_cat.id,
            'amount': '100000.00',
            'period': 'monthly',
            'start_date': date.today().isoformat(),
            'end_date': (date.today() - timedelta(days=10)).isoformat(),
        }
        response = self.client.post(url, data, format='json')
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

    def test_budget_only_own_budgets(self):
        user2 = User.objects.create_user(
            username='other', email='other@test.com', password='pass123',
        )
        Budget.objects.create(
            user=user2, category=self.expense_cat,
            amount=Decimal('100000.00'), period='monthly',
            start_date=date.today().replace(day=1), end_date=date.today(),
        )
        url = '/api/budgets/'
        response = self.client.get(url)
        self.assertEqual(len(response.data), 0)

    def test_budget_inactive_still_visible(self):
        Budget.objects.create(
            user=self.user, category=self.expense_cat,
            amount=Decimal('200000.00'), period='monthly',
            start_date=date.today().replace(day=1), end_date=date.today(),
            is_active=False,
        )
        url = '/api/budgets/'
        response = self.client.get(url)
        self.assertEqual(len(response.data), 1)
        self.assertFalse(response.data[0]['is_active'])

    def test_budget_duplicate_active_rejected_via_api(self):
        url = '/api/budgets/'
        data = {
            'category': self.expense_cat.id,
            'amount': '200000.00',
            'period': 'monthly',
            'start_date': date(2026, 1, 1).isoformat(),
            'end_date': date(2026, 1, 31).isoformat(),
        }
        self.client.post(url, data, format='json')
        data['amount'] = '300000.00'
        data['start_date'] = date(2026, 1, 15).isoformat()
        data['end_date'] = date(2026, 2, 15).isoformat()
        response = self.client.post(url, data, format='json')
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

    def test_budget_in_income_category_rejected(self):
        income_cat = Category.objects.create(
            name='Salario', type='INCOME', user=self.user,
        )
        url = '/api/budgets/'
        data = {
            'category': income_cat.id,
            'amount': '100000.00',
            'period': 'monthly',
            'start_date': date.today().replace(day=1).isoformat(),
            'end_date': date.today().isoformat(),
        }
        response = self.client.post(url, data, format='json')
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

    def test_budget_general_no_category(self):
        url = '/api/budgets/'
        data = {
            'amount': '500000.00',
            'period': 'monthly',
            'start_date': date.today().replace(day=1).isoformat(),
            'end_date': date.today().isoformat(),
        }
        response = self.client.post(url, data, format='json')
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertIsNone(response.data['category'])

    def test_budget_with_another_users_category_is_rejected(self):
        """No se puede crear un presupuesto con la categoría de otro usuario."""
        other = User.objects.create_user(
            username='owner', email='owner@test.com', password='pass123',
        )
        other_category = Category.objects.create(
            name='Ajena', type='EXPENSE', user=other,
        )
        url = '/api/budgets/'
        data = {
            'category': other_category.id,
            'amount': '100000.00',
            'period': 'monthly',
            'start_date': date.today().replace(day=1).isoformat(),
            'end_date': date.today().isoformat(),
        }

        response = self.client.post(url, data, format='json')

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertIn('category', response.data)
        self.assertEqual(Budget.objects.filter(user=self.user).count(), 0)
