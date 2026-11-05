import io
import datetime

from django.contrib.auth import get_user_model
from rest_framework import status
from rest_framework.test import APITestCase

from transactions.models import Transaction

User = get_user_model()


class IngestionAPITest(APITestCase):
    def setUp(self):
        self.user = User.objects.create_user(
            email='t@t.com', username='tuser', password='TestPass123!',
        )
        self.client.force_authenticate(user=self.user)

    def test_preview_parses_csv(self):
        csv = "fecha,descripcion,monto,tipo\n2026-07-15,Supermercado,45.50,EXPENSE\n"
        f = io.BytesIO(csv.encode('utf-8'))
        f.name = 'extracto.csv'
        response = self.client.post('/api/ingestion/preview/', {'file': f}, format='multipart')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data['rows']), 1)
        self.assertEqual(response.data['rows'][0]['type'], 'EXPENSE')

    def test_confirm_creates_transaction_and_dedups(self):
        csv = "fecha,descripcion,monto,tipo\n2026-07-15,Supermercado,45.50,EXPENSE\n"
        f = io.BytesIO(csv.encode('utf-8'))
        f.name = 'extracto.csv'
        preview_resp = self.client.post('/api/ingestion/preview/', {'file': f}, format='multipart')
        rows = preview_resp.data['rows']
        response = self.client.post('/api/ingestion/confirm/', {'rows': rows}, format='json')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['created'], 1)
        self.assertEqual(Transaction.objects.filter(user=self.user).count(), 1)

        # Reimportar la misma fila no crea duplicado.
        response2 = self.client.post('/api/ingestion/confirm/', {'rows': rows}, format='json')
        self.assertEqual(response2.data['created'], 0)
        self.assertEqual(response2.data['skipped'], 1)
        self.assertEqual(Transaction.objects.filter(user=self.user).count(), 1)
