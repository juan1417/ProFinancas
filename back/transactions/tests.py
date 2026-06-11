import pytest
from django.test import TestCase
from django.core.exceptions import ValidationError
from django.utils import timezone
from datetime import timedelta
from decimal import Decimal

from usuarios.models import User
from transactions.models import Category, Transaction


@pytest.mark.django_db
class TestCategoryModel(TestCase):
    """Tests para el modelo Category"""
    
    def setUp(self):
        """Configuración inicial para cada test"""
        self.user = User.objects.create_user(
            username='testuser',
            email='test@example.com',
            password='testpass123'
        )
    
    def test_create_category_success(self):
        """Test: Crear una categoría exitosamente"""
        category = Category.objects.create(
            name='Alimentación',
            type='EXPENSE',
            description='Gastos en comida',
            user=self.user
        )
        
        self.assertEqual(category.name, 'Alimentación')
        self.assertEqual(category.type, 'EXPENSE')
        self.assertTrue(category.is_active)
        self.assertIsNotNone(category.created_at)
        self.assertIsNotNone(category.updated_at)
    
    def test_category_string_representation(self):
        """Test: Representación en string de la categoría"""
        category = Category.objects.create(
            name='Salario',
            type='INCOME',
            user=self.user
        )
        
        self.assertEqual(str(category), 'Ingreso: Salario')
    
    def test_category_unique_together(self):
        """Test: No se pueden crear categorías duplicadas para el mismo usuario"""
        Category.objects.create(
            name='Transporte',
            type='EXPENSE',
            user=self.user
        )
        
        # Intentar crear una categoría duplicada
        with self.assertRaises(Exception):
            Category.objects.create(
                name='Transporte',
                type='EXPENSE',
                user=self.user
            )
    
    def test_category_different_users_same_name(self):
        """Test: Usuarios diferentes pueden tener categorías con el mismo nombre"""
        user2 = User.objects.create_user(
            username='user2',
            email='user2@example.com',
            password='pass123'
        )
        
        category1 = Category.objects.create(
            name='Entretenimiento',
            type='EXPENSE',
            user=self.user
        )
        
        category2 = Category.objects.create(
            name='Entretenimiento',
            type='EXPENSE',
            user=user2
        )
        
        self.assertNotEqual(category1.id, category2.id)
    
    def test_category_same_name_different_type(self):
        """Test: Mismo nombre pero diferente tipo es permitido"""
        category1 = Category.objects.create(
            name='Inversiones',
            type='INCOME',
            user=self.user
        )
        
        category2 = Category.objects.create(
            name='Inversiones',
            type='EXPENSE',
            user=self.user
        )
        
        self.assertNotEqual(category1.id, category2.id)
        self.assertEqual(category1.name, category2.name)
        self.assertNotEqual(category1.type, category2.type)


@pytest.mark.django_db
class TestTransactionModel(TestCase):
    """Tests para el modelo Transaction"""
    
    def setUp(self):
        """Configuración inicial para cada test"""
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
    
    def test_create_transaction_success(self):
        """Test: Crear una transacción exitosamente"""
        transaction = Transaction.objects.create(
            user=self.user,
            category=self.expense_category,
            type='EXPENSE',
            amount=Decimal('50.00'),
            description='Compra de supermercado',
            transaction_date=timezone.now()
        )
        
        self.assertEqual(transaction.user, self.user)
        self.assertEqual(transaction.amount, Decimal('50.00'))
        self.assertEqual(transaction.type, 'EXPENSE')
        self.assertIsNotNone(transaction.created_at)
    
    def test_transaction_string_representation(self):
        """Test: Representación en string de la transacción"""
        transaction = Transaction.objects.create(
            user=self.user,
            category=self.income_category,
            type='INCOME',
            amount=Decimal('1000.00'),
            description='Pago de salario',
            transaction_date=timezone.now()
        )
        
        self.assertIn('Ingreso', str(transaction))
        self.assertIn('1000', str(transaction))
    
    def test_transaction_type_category_mismatch(self):
        """Test: Validar que el tipo de transacción coincida con el tipo de categoría.

        Esta validacion ahora vive en TransactionSerializer.validate()
        (ver serializers.py) en vez de en el modelo, porque el
        modelo.save() con full_clean() era una validacion duplicada
        que se rompia en updates parciales via DRF. La regla de
        negocio sigue activa — solo se ejecuta en otro lugar."""
        from rest_framework.exceptions import ValidationError as DRFValidation
        from transactions.serializers import TransactionSerializer
        serializer = TransactionSerializer(data={
            'user': self.user.id,
            'category': self.expense_category.id,
            'type': 'INCOME',  # Tipo incorrecto
            'amount': '100.00',
            'description': 'Test',
            'transaction_date': timezone.now().isoformat(),
        })
        self.assertFalse(serializer.is_valid())
        self.assertIn('type', serializer.errors)
    
    def test_transaction_negative_amount(self):
        """Test: El monto debe ser positivo"""
        with self.assertRaises(ValidationError):
            transaction = Transaction(
                user=self.user,
                category=self.expense_category,
                type='EXPENSE',
                amount=Decimal('-50.00'),  # Monto negativo
                description='Test negativo',
                transaction_date=timezone.now()
            )
            transaction.full_clean()
    
    def test_transaction_zero_amount(self):
        """Test: El monto debe ser mayor que cero"""
        with self.assertRaises(ValidationError):
            transaction = Transaction(
                user=self.user,
                category=self.expense_category,
                type='EXPENSE',
                amount=Decimal('0.00'),  # Monto cero
                description='Test cero',
                transaction_date=timezone.now()
            )
            transaction.full_clean()
    
    def test_transaction_ordering(self):
        """Test: Las transacciones se ordenan por fecha descendente"""
        now = timezone.now()
        
        transaction1 = Transaction.objects.create(
            user=self.user,
            category=self.expense_category,
            type='EXPENSE',
            amount=Decimal('50.00'),
            description='Primera',
            transaction_date=now - timedelta(days=2)
        )
        
        transaction2 = Transaction.objects.create(
            user=self.user,
            category=self.expense_category,
            type='EXPENSE',
            amount=Decimal('75.00'),
            description='Segunda',
            transaction_date=now - timedelta(days=1)
        )
        
        transaction3 = Transaction.objects.create(
            user=self.user,
            category=self.expense_category,
            type='EXPENSE',
            amount=Decimal('100.00'),
            description='Tercera',
            transaction_date=now
        )
        
        transactions = Transaction.objects.all()
        self.assertEqual(transactions[0].id, transaction3.id)
        self.assertEqual(transactions[1].id, transaction2.id)
        self.assertEqual(transactions[2].id, transaction1.id)
    
    def test_transaction_with_notes(self):
        """Test: Transacción con notas adicionales"""
        transaction = Transaction.objects.create(
            user=self.user,
            category=self.expense_category,
            type='EXPENSE',
            amount=Decimal('150.00'),
            description='Compra especial',
            notes='Esta compra incluye artículos de limpieza',
            transaction_date=timezone.now()
        )
        
        self.assertEqual(transaction.notes, 'Esta compra incluye artículos de limpieza')
    
    def test_category_protect_on_delete(self):
        """Test: No se puede eliminar una categoría con transacciones asociadas"""
        Transaction.objects.create(
            user=self.user,
            category=self.expense_category,
            type='EXPENSE',
            amount=Decimal('50.00'),
            description='Test',
            transaction_date=timezone.now()
        )
        
        # Intentar eliminar la categoría debe fallar
        from django.db.models import ProtectedError
        with self.assertRaises(ProtectedError):
            self.expense_category.delete()


@pytest.mark.django_db
class TestTransactionBusinessLogic(TestCase):
    """Tests para lógica de negocio de transacciones"""
    
    def setUp(self):
        """Configuración inicial"""
        self.user = User.objects.create_user(
            username='testuser',
            email='test@example.com',
            password='testpass123'
        )
        
        self.income_cat = Category.objects.create(
            name='Salario',
            type='INCOME',
            user=self.user
        )
        
        self.expense_cat = Category.objects.create(
            name='Gastos',
            type='EXPENSE',
            user=self.user
        )
    
    def test_calculate_balance(self):
        """Test: Calcular balance entre ingresos y gastos"""
        # Crear ingresos
        Transaction.objects.create(
            user=self.user,
            category=self.income_cat,
            type='INCOME',
            amount=Decimal('2000.00'),
            description='Salario',
            transaction_date=timezone.now()
        )
        
        # Crear gastos
        Transaction.objects.create(
            user=self.user,
            category=self.expense_cat,
            type='EXPENSE',
            amount=Decimal('500.00'),
            description='Renta',
            transaction_date=timezone.now()
        )
        
        Transaction.objects.create(
            user=self.user,
            category=self.expense_cat,
            type='EXPENSE',
            amount=Decimal('300.00'),
            description='Comida',
            transaction_date=timezone.now()
        )
        
        # Calcular balance
        from django.db.models import Sum
        income_total = Transaction.objects.filter(
            user=self.user, 
            type='INCOME'
        ).aggregate(Sum('amount'))['amount__sum'] or Decimal('0')
        
        expense_total = Transaction.objects.filter(
            user=self.user, 
            type='EXPENSE'
        ).aggregate(Sum('amount'))['amount__sum'] or Decimal('0')
        
        balance = income_total - expense_total
        
        self.assertEqual(income_total, Decimal('2000.00'))
        self.assertEqual(expense_total, Decimal('800.00'))
        self.assertEqual(balance, Decimal('1200.00'))
