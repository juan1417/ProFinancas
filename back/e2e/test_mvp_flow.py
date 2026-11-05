"""E2E del MVP completo: registro -> categoria -> movimiento -> resumen ->
factura -> documento -> ingesta CSV (preview/confirm/dedup)."""
import io

from django.contrib.auth import get_user_model
from rest_framework import status
from rest_framework.test import APITestCase, APIClient

User = get_user_model()


class MvpEndToEndTest(APITestCase):
    def setUp(self):
        self.client = APIClient()
        self.csv = (
            "fecha,descripcion,monto,tipo\n"
            "2026-07-20,Mercado,35.00,EXPENSE\n"
        )

    def _auth(self):
        reg = self.client.post('/api/auth/register/', {
            'email': 'mvp@test.com',
            'username': 'mvpuser',
            'password': 'TestPass123!',
            'password_confirm': 'TestPass123!',
        }, format='json')
        self.assertEqual(reg.status_code, status.HTTP_201_CREATED)
        self.assertIn('access', reg.data)
        self.client.credentials(HTTP_AUTHORIZATION=f"Bearer {reg.data['access']}")
        return reg.data

    def test_full_mvp_flow(self):
        data = self._auth()

        # Categoria de gasto
        cat = self.client.post('/api/categories/', {
            'name': 'Alimentación', 'type': 'EXPENSE',
        }, format='json')
        self.assertEqual(cat.status_code, status.HTTP_201_CREATED)
        cat_id = cat.data['id']

        # Movimiento con fecha pasada
        tx = self.client.post('/api/transactions/', {
            'category': cat_id, 'type': 'EXPENSE', 'amount': '12.50',
            'description': 'Almuerzo', 'transaction_date': '2026-07-15T12:00:00-05:00',
        }, format='json')
        self.assertEqual(tx.status_code, status.HTTP_201_CREATED)

        # Resumen incluye el movimiento
        summary = self.client.get('/api/transactions/summary/?period=year')
        self.assertEqual(summary.status_code, status.HTTP_200_OK)
        self.assertEqual(summary.data['transaction_count'], 1)
        self.assertAlmostEqual(summary.data['total_expenses'], 12.5, places=2)

        # by_category incluye la categoria
        by_cat = self.client.get('/api/transactions/by_category/')
        self.assertEqual(by_cat.status_code, status.HTTP_200_OK)
        self.assertTrue(any(r['category__name'] == 'Alimentación' for r in by_cat.data))

        # Factura
        inv = self.client.post('/api/invoices/', {
            'provider': 'TEPCO', 'amount': '6420.00', 'status': 'PENDING',
            'category': 'Servicios', 'due_date': '2030-07-15',
            'registered_date': '2026-07-01',
        }, format='json')
        self.assertEqual(inv.status_code, status.HTTP_201_CREATED)
        self.assertIn('id', inv.data)
        self.assertEqual(inv.data['invoice_number'], 'FAC-001')

        # Documento PDF
        doc = self.client.get(f"/api/invoices/{inv.data['id']}/document/")
        self.assertEqual(doc.status_code, status.HTTP_200_OK)
        self.assertEqual(doc['Content-Type'], 'application/pdf')
        self.assertTrue(b''.join(doc.streaming_content).startswith(b'%PDF'))

        # Ingesta CSV: preview
        f = io.BytesIO(self.csv.encode('utf-8'))
        f.name = 'extracto.csv'
        preview = self.client.post('/api/ingestion/preview/', {'file': f}, format='multipart')
        self.assertEqual(preview.status_code, status.HTTP_200_OK)
        self.assertEqual(len(preview.data['rows']), 1)
        rows = preview.data['rows']

        # Confirm
        confirm = self.client.post('/api/ingestion/confirm/', {'rows': rows}, format='json')
        self.assertEqual(confirm.status_code, status.HTTP_200_OK)
        self.assertEqual(confirm.data['created'], 1)

        # Dedup en reimportacion
        confirm2 = self.client.post('/api/ingestion/confirm/', {'rows': rows}, format='json')
        self.assertEqual(confirm2.data['created'], 0)
        self.assertEqual(confirm2.data['skipped'], 1)
