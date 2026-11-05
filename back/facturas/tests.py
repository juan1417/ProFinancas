import datetime

from django.contrib.auth import get_user_model
from rest_framework import status
from rest_framework.test import APITestCase

from .models import Invoice
from .application.invoice_service import create_invoice

User = get_user_model()


class InvoiceModelTest(APITestCase):
    def setUp(self):
        self.user = User.objects.create_user(
            email='test@test.com', username='testuser', password='TestPass123!'
        )

    def test_create_invoice_success(self):
        invoice = create_invoice(
            user=self.user,
            provider='TEPCO',
            amount=6420.00,
            status='PENDING',
            category='Servicios',
            due_date=datetime.date(2026, 7, 15),
            registered_date=datetime.date(2026, 7, 1),
        )
        self.assertEqual(invoice.invoice_number, 'FAC-001')
        self.assertEqual(str(invoice), 'FAC-001 - TEPCO')

    def test_invoice_number_auto_increment(self):
        create_invoice(
            user=self.user, provider='A', amount=100,
            status='PENDING', due_date=datetime.date(2026, 7, 15),
            registered_date=datetime.date(2026, 7, 1),
        )
        inv2 = create_invoice(
            user=self.user, provider='B', amount=200,
            status='PENDING', due_date=datetime.date(2026, 7, 20),
            registered_date=datetime.date(2026, 7, 2),
        )
        self.assertEqual(inv2.invoice_number, 'FAC-002')

    def test_invoice_str(self):
        invoice = create_invoice(
            user=self.user, provider='NTT', amount=4500,
            status='PAID', due_date=datetime.date(2026, 7, 20),
            registered_date=datetime.date(2026, 7, 5),
            paid_date=datetime.date(2026, 7, 19),
        )
        self.assertEqual(str(invoice), 'FAC-001 - NTT')


class InvoiceAPITest(APITestCase):
    def setUp(self):
        self.user = User.objects.create_user(
            email='test@test.com', username='testuser', password='TestPass123!'
        )
        self.client.force_authenticate(user=self.user)
        self.invoice_data = {
            'provider': 'TEPCO Electricidad',
            'amount': '6420.00',
            'status': 'PENDING',
            'category': 'Servicios',
            'due_date': '2030-07-15',
            'registered_date': '2026-07-01',
        }

    def test_create_invoice(self):
        response = self.client.post('/api/invoices/', self.invoice_data, format='json')
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertEqual(response.data['provider'], 'TEPCO Electricidad')
        self.assertEqual(response.data['invoice_number'], 'FAC-001')
        self.assertEqual(response.data['status'], 'PENDING')

    def test_create_invoice_returns_id(self):
        """La respuesta de creación debe incluir el id del recurso creado."""
        response = self.client.post('/api/invoices/', self.invoice_data, format='json')

        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertIn('id', response.data)
        invoice = Invoice.objects.get(invoice_number=response.data['invoice_number'])
        self.assertEqual(str(invoice.id), str(response.data['id']))

    def test_create_invoice_paid_requires_paid_date(self):
        data = {**self.invoice_data, 'status': 'PAID'}
        response = self.client.post('/api/invoices/', data, format='json')
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

    def test_create_invoice_paid_with_date(self):
        data = {
            **self.invoice_data,
            'status': 'PAID',
            'paid_date': '2026-07-14',
        }
        response = self.client.post('/api/invoices/', data, format='json')
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertEqual(response.data['paid_date'], '2026-07-14')

    def test_list_invoices(self):
        create_invoice(
            user=self.user, provider='A', amount=100,
            status='PENDING', due_date=datetime.date(2026, 7, 15),
            registered_date=datetime.date(2026, 7, 1),
        )
        response = self.client.get('/api/invoices/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 1)

    def test_only_own_invoices(self):
        other = User.objects.create_user(
            email='other@test.com', username='other', password='TestPass123!'
        )
        create_invoice(
            user=other, provider='Other', amount=100,
            status='PENDING', due_date=datetime.date(2026, 7, 15),
            registered_date=datetime.date(2026, 7, 1),
        )
        response = self.client.get('/api/invoices/')
        self.assertEqual(len(response.data), 0)

    def test_filter_by_status(self):
        create_invoice(
            user=self.user, provider='A', amount=100,
            status='PAID', due_date=datetime.date(2026, 7, 15),
            registered_date=datetime.date(2026, 7, 1), paid_date=datetime.date(2026, 7, 14),
        )
        create_invoice(
            user=self.user, provider='B', amount=200,
            status='PENDING', due_date=datetime.date(2026, 7, 20),
            registered_date=datetime.date(2026, 7, 2),
        )
        response = self.client.get('/api/invoices/?status=PAID')
        self.assertEqual(len(response.data), 1)
        self.assertEqual(response.data[0]['provider'], 'A')

    def test_update_invoice(self):
        invoice = create_invoice(
            user=self.user, provider='A', amount=100,
            status='PENDING', due_date=datetime.date(2026, 7, 15),
            registered_date=datetime.date(2026, 7, 1),
        )
        response = self.client.patch(
            f'/api/invoices/{invoice.id}/',
            {'status': 'PAID', 'paid_date': '2026-07-14'},
            format='json',
        )
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['status'], 'PAID')

    def test_delete_invoice(self):
        invoice = create_invoice(
            user=self.user, provider='A', amount=100,
            status='PENDING', due_date=datetime.date(2026, 7, 15),
            registered_date=datetime.date(2026, 7, 1),
        )
        response = self.client.delete(f'/api/invoices/{invoice.id}/')
        self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)
        self.assertEqual(Invoice.objects.count(), 0)

    def test_search_by_provider(self):
        create_invoice(
            user=self.user, provider='TEPCO', amount=100,
            status='PENDING', due_date=datetime.date(2026, 7, 15),
            registered_date=datetime.date(2026, 7, 1),
        )
        create_invoice(
            user=self.user, provider='NTT', amount=200,
            status='PENDING', due_date=datetime.date(2026, 7, 20),
            registered_date=datetime.date(2026, 7, 2),
        )
        response = self.client.get('/api/invoices/?search=TEPCO')
        self.assertEqual(len(response.data), 1)
        self.assertEqual(response.data[0]['provider'], 'TEPCO')

    def test_overdue_is_derived_from_due_date(self):
        """Una factura PENDING con due_date pasada se reporta como OVERDUE."""
        invoice = create_invoice(
            user=self.user, provider='A', amount=100,
            status='PENDING', due_date=datetime.date(2020, 1, 1),
            registered_date=datetime.date(2019, 12, 1),
        )
        response = self.client.get(f'/api/invoices/{invoice.id}/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['status'], 'OVERDUE')

        response = self.client.get('/api/invoices/?status=OVERDUE')
        self.assertEqual(len(response.data), 1)

    def test_download_document_returns_pdf(self):
        invoice = create_invoice(
            user=self.user, provider='TEPCO', amount=100,
            status='PENDING', due_date=datetime.date(2030, 7, 15),
            registered_date=datetime.date(2026, 7, 1),
        )
        response = self.client.get(f'/api/invoices/{invoice.id}/document/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response['Content-Type'], 'application/pdf')
        self.assertTrue(b''.join(response.streaming_content).startswith(b'%PDF'))

    def test_unauthenticated_returns_401(self):
        self.client.force_authenticate(user=None)
        response = self.client.get('/api/invoices/')
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)
