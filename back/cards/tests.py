import pytest
from rest_framework.test import APITestCase, APIClient
from rest_framework import status

from usuarios.models import User


@pytest.mark.django_db
class CardAPITest(APITestCase):
    def setUp(self):
        self.user = User.objects.create_user(
            email='card@t.com', username='carduser', password='TestPass123!'
        )
        self.client.force_authenticate(user=self.user)
        self.url = '/api/cards/'
        self.payload = {
            'brand': 'Visa', 'bank': 'Bancolombia', 'last4': '4242',
            'expiry': '12/28', 'holder_name': 'JUAN MACHADO', 'color_hex': '#1A4570',
        }

    def test_create_card_201_y_devuelve_id(self):
        res = self.client.post(self.url, self.payload, format='json')
        self.assertEqual(res.status_code, status.HTTP_201_CREATED)
        self.assertIn('id', res.data)
        self.assertEqual(res.data['last4'], '4242')

    def test_list_solo_propias(self):
        self.client.post(self.url, self.payload, format='json')
        other = User.objects.create_user(email='o@t.com', username='o', password='TestPass123!')
        c2 = APIClient()
        c2.force_authenticate(user=other)
        res = c2.get(self.url, format='json')
        self.assertEqual(res.status_code, status.HTTP_200_OK)
        self.assertEqual(len(res.data), 0)

    def test_last4_debe_ser_4_digitos(self):
        res = self.client.post(self.url, {**self.payload, 'last4': '12'}, format='json')
        self.assertEqual(res.status_code, status.HTTP_400_BAD_REQUEST)

    def test_update_y_delete(self):
        cid = self.client.post(self.url, self.payload, format='json').data['id']
        res = self.client.patch(self.url + f'{cid}/', {'brand': 'Amex'}, format='json')
        self.assertEqual(res.status_code, status.HTTP_200_OK)
        self.assertEqual(res.data['brand'], 'Amex')
        res = self.client.delete(self.url + f'{cid}/')
        self.assertEqual(res.status_code, status.HTTP_204_NO_CONTENT)
