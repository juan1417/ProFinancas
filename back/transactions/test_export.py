import pytest
from decimal import Decimal
from datetime import datetime, timedelta
from rest_framework.test import APITestCase, APIClient
from rest_framework import status
from django.utils import timezone

from usuarios.models import User
from transactions.models import Category, Transaction


@pytest.mark.django_db
class TransactionsExportTest(APITestCase):
    def setUp(self):
        self.client = APIClient()
        self.user = User.objects.create_user(
            email='ex@t.com', username='expo', password='TestPass123!'
        )
        self.client.force_authenticate(user=self.user)
        self.cat = Category.objects.create(name='Compras', type='EXPENSE', user=self.user)
        self.url = '/api/transactions/export/'
        now = timezone.now()
        Transaction.objects.create(
            user=self.user, category=self.cat, type='EXPENSE', amount=Decimal('50.00'),
            description='Mercado', transaction_date=now,
        )

    def test_export_csv(self):
        res = self.client.get(self.url, {'export': 'csv'})
        self.assertEqual(res.status_code, status.HTTP_200_OK)
        self.assertIn('text/csv', res['Content-Type'])
        body = b''.join(res.streaming_content).decode() if hasattr(res, 'streaming_content') else res.content.decode()
        self.assertIn('description', body.lower())
        self.assertIn('Mercado', body)

    def test_export_json(self):
        res = self.client.get(self.url, {'export': 'json'})
        self.assertEqual(res.status_code, status.HTTP_200_OK)
        self.assertEqual(len(res.data), 1)
        self.assertEqual(res.data[0]['type'], 'EXPENSE')
        self.assertAlmostEqual(float(res.data[0]['amount']), 50.0, places=2)

    def test_export_rango_filtra(self):
        start = (timezone.localdate() - timedelta(days=2)).isoformat()
        end = (timezone.localdate() + timedelta(days=2)).isoformat()
        res = self.client.get(self.url, {'export': 'json', 'start_date': start, 'end_date': end})
        self.assertEqual(len(res.data), 1)
        # rango en el pasado no debería traer nada
        old = (timezone.localdate() - timedelta(days=30)).isoformat()
        res2 = self.client.get(self.url, {'export': 'json', 'start_date': old, 'end_date': old})
        self.assertEqual(len(res2.data), 0)

    def test_export_solo_propio(self):
        other = User.objects.create_user(email='o3@t.com', username='o3', password='TestPass123!')
        c2 = APIClient(); c2.force_authenticate(user=other)
        res = c2.get(self.url, {'export': 'json'})
        self.assertEqual(len(res.data), 0)
