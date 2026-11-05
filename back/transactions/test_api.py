import pytest
from datetime import datetime, time, timedelta
from rest_framework.test import APITestCase, APIClient
from rest_framework import status
from django.utils import timezone
from decimal import Decimal

from usuarios.models import User
from transactions.models import Category, Transaction


@pytest.mark.django_db
class TestCategoryAPI(APITestCase):
    """Tests para la API de categorías"""

    def setUp(self):
        """Configuración inicial para cada test"""
        self.client = APIClient()
        self.user = User.objects.create_user(
            username='testuser',
            email='test@example.com',
            password='testpass123'
        )
        # El backend exige autenticacion en todos los endpoints
        # (DEFAULT_PERMISSION_CLASSES = IsAuthenticated), por lo que
        # sin esto todos los requests rebotaban con 401. Ver
        # transactions/views.py:35 y 77 — get_queryset() filtra por
        # request.user, asi que cualquier test de la API sin auth
        # no estaba probando nada real.
        self.client.force_authenticate(user=self.user)
    
    def test_create_category(self):
        """Test: Crear una categoría a través de la API"""
        url = '/api/categories/'
        data = {
            'name': 'Alimentación',
            'type': 'EXPENSE',
            'description': 'Gastos en comida',
            'user': self.user.id,
            'is_active': True
        }
        
        response = self.client.post(url, data, format='json')
        
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertEqual(response.data['name'], 'Alimentación')
        self.assertEqual(response.data['type'], 'EXPENSE')
        self.assertTrue(response.data['is_active'])
    
    def test_list_categories(self):
        """Test: Listar categorías"""
        # Crear categorías de prueba
        Category.objects.create(
            name='Salario',
            type='INCOME',
            user=self.user
        )
        Category.objects.create(
            name='Transporte',
            type='EXPENSE',
            user=self.user
        )
        
        url = '/api/categories/'
        response = self.client.get(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 2)
    
    def test_get_category_detail(self):
        """Test: Obtener detalle de una categoría"""
        category = Category.objects.create(
            name='Servicios',
            type='EXPENSE',
            user=self.user
        )
        
        url = f'/api/categories/{category.id}/'
        response = self.client.get(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['name'], 'Servicios')
    
    def test_update_category(self):
        """Test: Actualizar una categoría"""
        category = Category.objects.create(
            name='Educación',
            type='EXPENSE',
            user=self.user
        )
        
        url = f'/api/categories/{category.id}/'
        data = {
            'name': 'Educación y Formación',
            'type': 'EXPENSE',
            'user': self.user.id,
            'is_active': True
        }
        
        response = self.client.put(url, data, format='json')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['name'], 'Educación y Formación')
    
    def test_delete_category_without_transactions(self):
        """Test: Eliminar una categoría sin transacciones"""
        category = Category.objects.create(
            name='Otros',
            type='EXPENSE',
            user=self.user
        )
        
        url = f'/api/categories/{category.id}/'
        response = self.client.delete(url)
        
        self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)
        self.assertFalse(Category.objects.filter(id=category.id).exists())
    
    def test_filter_categories_by_type(self):
        """Test: Filtrar categorías por tipo"""
        Category.objects.create(name='Salario', type='INCOME', user=self.user)
        Category.objects.create(name='Freelance', type='INCOME', user=self.user)
        Category.objects.create(name='Comida', type='EXPENSE', user=self.user)
        
        url = '/api/categories/?type=INCOME'
        response = self.client.get(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 2)
        for category in response.data:
            self.assertEqual(category['type'], 'INCOME')
    
    def test_search_categories(self):
        """Test: Buscar categorías por nombre"""
        Category.objects.create(name='Alimentos', type='EXPENSE', user=self.user)
        Category.objects.create(name='Alimentación', type='EXPENSE', user=self.user)
        Category.objects.create(name='Transporte', type='EXPENSE', user=self.user)
        
        url = '/api/categories/?search=Alimen'
        response = self.client.get(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 2)
    
    def test_categories_by_type_endpoint(self):
        """Test: Endpoint personalizado para categorías por tipo"""
        Category.objects.create(name='Salario', type='INCOME', user=self.user, is_active=True)
        Category.objects.create(name='Comida', type='EXPENSE', user=self.user, is_active=True)
        Category.objects.create(name='Transporte', type='EXPENSE', user=self.user, is_active=True)
        
        url = '/api/categories/by_type/'
        response = self.client.get(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn('income', response.data)
        self.assertIn('expense', response.data)
        self.assertEqual(len(response.data['income']), 1)
        self.assertEqual(len(response.data['expense']), 2)


@pytest.mark.django_db
class TestTransactionAPI(APITestCase):
    """Tests para la API de transacciones"""
    
    def setUp(self):
        """Configuración inicial para cada test"""
        self.client = APIClient()
        self.user = User.objects.create_user(
            username='testuser',
            email='test@example.com',
            password='testpass123'
        )
        
        self.income_category = Category.objects.create(
            name='Salario',
            type='INCOME',
            user=self.user
        )
        
        self.expense_category = Category.objects.create(
            name='Alimentación',
            type='EXPENSE',
            user=self.user
        )
        # Ver nota en TestCategoryAPI.setUp sobre por que esto
        # es necesario.
        self.client.force_authenticate(user=self.user)
    
    def test_create_transaction(self):
        """Test: Crear una transacción a través de la API"""
        url = '/api/transactions/'
        data = {
            'user': self.user.id,
            'category': self.expense_category.id,
            'type': 'EXPENSE',
            'amount': '150.50',
            'description': 'Compra de supermercado',
            'transaction_date': timezone.now().isoformat()
        }
        
        response = self.client.post(url, data, format='json')
        
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertEqual(response.data['amount'], '150.50')
        self.assertEqual(response.data['description'], 'Compra de supermercado')
    
    def test_create_transaction_invalid_category_type(self):
        """Test: No se puede crear transacción con categoría de tipo incorrecto"""
        url = '/api/transactions/'
        data = {
            'user': self.user.id,
            'category': self.expense_category.id,
            'type': 'INCOME',  # Tipo incorrecto
            'amount': '100.00',
            'description': 'Test',
            'transaction_date': timezone.now().isoformat()
        }
        
        response = self.client.post(url, data, format='json')
        
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
    
    def test_create_transaction_negative_amount(self):
        """Test: No se puede crear transacción con monto negativo"""
        url = '/api/transactions/'
        data = {
            'user': self.user.id,
            'category': self.expense_category.id,
            'type': 'EXPENSE',
            'amount': '-50.00',
            'description': 'Test negativo',
            'transaction_date': timezone.now().isoformat()
        }
        
        response = self.client.post(url, data, format='json')
        
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
    
    def test_list_transactions(self):
        """Test: Listar transacciones"""
        Transaction.objects.create(
            user=self.user,
            category=self.income_category,
            type='INCOME',
            amount=Decimal('2000.00'),
            description='Salario',
            transaction_date=timezone.now()
        )
        
        Transaction.objects.create(
            user=self.user,
            category=self.expense_category,
            type='EXPENSE',
            amount=Decimal('500.00'),
            description='Renta',
            transaction_date=timezone.now()
        )
        
        url = '/api/transactions/'
        response = self.client.get(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 2)
    
    def test_get_transaction_detail(self):
        """Test: Obtener detalle de una transacción"""
        transaction = Transaction.objects.create(
            user=self.user,
            category=self.expense_category,
            type='EXPENSE',
            amount=Decimal('75.00'),
            description='Gasolina',
            transaction_date=timezone.now()
        )
        
        url = f'/api/transactions/{transaction.id}/'
        response = self.client.get(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['description'], 'Gasolina')
        self.assertEqual(response.data['category_name'], 'Alimentación')
    
    def test_update_transaction(self):
        """Test: Actualizar una transacción"""
        transaction = Transaction.objects.create(
            user=self.user,
            category=self.expense_category,
            type='EXPENSE',
            amount=Decimal('100.00'),
            description='Compra original',
            transaction_date=timezone.now()
        )
        
        url = f'/api/transactions/{transaction.id}/'
        data = {
            'user': self.user.id,
            'category': self.expense_category.id,
            'type': 'EXPENSE',
            'amount': '125.00',
            'description': 'Compra actualizada',
            'transaction_date': timezone.now().isoformat()
        }
        
        response = self.client.put(url, data, format='json')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['amount'], '125.00')
        self.assertEqual(response.data['description'], 'Compra actualizada')
    
    def test_delete_transaction(self):
        """Test: Eliminar una transacción"""
        transaction = Transaction.objects.create(
            user=self.user,
            category=self.expense_category,
            type='EXPENSE',
            amount=Decimal('50.00'),
            description='Test',
            transaction_date=timezone.now()
        )
        
        url = f'/api/transactions/{transaction.id}/'
        response = self.client.delete(url)
        
        self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)
        self.assertFalse(Transaction.objects.filter(id=transaction.id).exists())
    
    def test_filter_transactions_by_type(self):
        """Test: Filtrar transacciones por tipo"""
        Transaction.objects.create(
            user=self.user,
            category=self.income_category,
            type='INCOME',
            amount=Decimal('2000.00'),
            description='Salario',
            transaction_date=timezone.now()
        )
        
        Transaction.objects.create(
            user=self.user,
            category=self.expense_category,
            type='EXPENSE',
            amount=Decimal('500.00'),
            description='Renta',
            transaction_date=timezone.now()
        )
        
        url = '/api/transactions/?type=EXPENSE'
        response = self.client.get(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 1)
        self.assertEqual(response.data[0]['type'], 'EXPENSE')
    
    def test_transaction_summary_endpoint(self):
        """Test: Endpoint de resumen financiero"""
        Transaction.objects.create(
            user=self.user,
            category=self.income_category,
            type='INCOME',
            amount=Decimal('3000.00'),
            description='Salario',
            transaction_date=timezone.now()
        )
        
        Transaction.objects.create(
            user=self.user,
            category=self.expense_category,
            type='EXPENSE',
            amount=Decimal('1000.00'),
            description='Gastos',
            transaction_date=timezone.now()
        )
        
        url = '/api/transactions/summary/'
        response = self.client.get(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['total_income'], 3000.0)
        self.assertEqual(response.data['total_expenses'], 1000.0)
        self.assertEqual(response.data['balance'], 2000.0)
        self.assertEqual(response.data['transaction_count'], 2)
        # /summary/ also includes a per-category breakdown so the
        # dashboard's pie/bar charts have data without a second request.
        self.assertIn('by_category', response.data)
        self.assertEqual(len(response.data['by_category']), 2)
    
    def test_transactions_by_category_endpoint(self):
        """Test: Endpoint de transacciones agrupadas por categoría"""
        Transaction.objects.create(
            user=self.user,
            category=self.expense_category,
            type='EXPENSE',
            amount=Decimal('100.00'),
            description='Compra 1',
            transaction_date=timezone.now()
        )
        
        Transaction.objects.create(
            user=self.user,
            category=self.expense_category,
            type='EXPENSE',
            amount=Decimal('150.00'),
            description='Compra 2',
            transaction_date=timezone.now()
        )
        
        url = '/api/transactions/by_category/'
        response = self.client.get(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 1)
        self.assertEqual(float(response.data[0]['total_amount']), 250.0)
        self.assertEqual(response.data[0]['transaction_count'], 2)


@pytest.mark.django_db
class TestTransactionRegressionsF0(APITestCase):
    """Regresiones de la auditoría, escritas ANTES de tocar producción.

    Con el código actual estos tests fallan exactamente en los defectos
    conocidos (PATCH que desincroniza type/category, end_date exclusivo,
    fecha malformada que devuelve 500). Deben pasar tras el arreglo.
    """

    def setUp(self):
        self.client = APIClient()
        self.user = User.objects.create_user(
            username='f0user',
            email='f0@example.com',
            password='testpass123',
        )
        self.expense_category = Category.objects.create(
            name='Mercado', type='EXPENSE', user=self.user,
        )
        self.income_category = Category.objects.create(
            name='Nomina', type='INCOME', user=self.user,
        )
        self.client.force_authenticate(user=self.user)

    def _create_expense(self, amount='40.00', when=None):
        return Transaction.objects.create(
            user=self.user,
            category=self.expense_category,
            type='EXPENSE',
            amount=Decimal(amount),
            description='Compra de prueba',
            transaction_date=when or timezone.now(),
        )

    def test_patch_category_only_cannot_desync_type(self):
        """Cambiar solo la categoría debe evaluar el type ya guardado."""
        transaction = self._create_expense()
        url = f'/api/transactions/{transaction.id}/'

        response = self.client.patch(
            url, {'category': self.income_category.id}, format='json',
        )

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertIn('type', response.data)
        transaction.refresh_from_db()
        self.assertEqual(transaction.category_id, self.expense_category.id)
        self.assertEqual(transaction.type, 'EXPENSE')

    def test_patch_type_only_mismatch_still_rejected(self):
        """Comportamiento que ya funciona y no debe romperse."""
        transaction = self._create_expense()
        url = f'/api/transactions/{transaction.id}/'

        response = self.client.patch(url, {'type': 'INCOME'}, format='json')

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertIn('type', response.data)

    def test_summary_end_date_includes_the_whole_last_day(self):
        """Un movimiento a las 23:59 del end_date debe contar."""
        target_day = timezone.localdate() - timedelta(days=1)
        late_transaction = timezone.make_aware(
            datetime.combine(target_day, time(23, 59)),
        )
        self._create_expense(amount='40.00', when=late_transaction)

        url = (
            '/api/transactions/summary/'
            f'?start_date={target_day.isoformat()}'
            f'&end_date={target_day.isoformat()}'
        )
        response = self.client.get(url)

        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['transaction_count'], 1)
        self.assertEqual(response.data['total_expenses'], 40.0)

    def test_by_category_end_date_includes_the_whole_last_day(self):
        """Mismo criterio inclusivo en el agrupado por categoría."""
        target_day = timezone.localdate() - timedelta(days=1)
        late_transaction = timezone.make_aware(
            datetime.combine(target_day, time(23, 59)),
        )
        self._create_expense(amount='40.00', when=late_transaction)

        url = (
            '/api/transactions/by_category/'
            f'?start_date={target_day.isoformat()}'
            f'&end_date={target_day.isoformat()}'
        )
        response = self.client.get(url)

        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 1)
        self.assertEqual(float(response.data[0]['total_amount']), 40.0)
        self.assertEqual(response.data[0]['transaction_count'], 1)

    def test_by_category_malformed_start_date_returns_400(self):
        """Una fecha inválida es error del cliente, no un 500."""
        self.client.raise_request_exception = False

        response = self.client.get(
            '/api/transactions/by_category/?start_date=not-a-date',
        )

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

    def test_summary_malformed_start_date_returns_400(self):
        """Comportamiento que ya funciona y no debe romperse."""
        self.client.raise_request_exception = False

        response = self.client.get(
            '/api/transactions/summary/?start_date=not-a-date',
        )

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
