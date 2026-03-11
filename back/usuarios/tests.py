from django.test import TestCase
from django.contrib.auth import get_user_model
from rest_framework.test import APIClient
from rest_framework import status

User = get_user_model()

REGISTER_URL = '/api/auth/register/'
LOGIN_URL = '/api/auth/login/'
LOGOUT_URL = '/api/auth/logout/'
PROFILE_URL = '/api/auth/profile/'
CHANGE_PASSWORD_URL = '/api/auth/change-password/'


def create_user(email='test@profinanzas.com', password='TestPass123!', **kwargs):
    kwargs.setdefault('username', 'testuser')
    return User.objects.create_user(username=kwargs.pop('username'), email=email, password=password, **kwargs)


class UserModelTest(TestCase):
    """Tests del modelo User."""

    def test_user_tiene_uuid_como_pk(self):
        user = create_user()
        import uuid
        self.assertIsInstance(user.id, uuid.UUID)

    def test_user_email_como_campo_login(self):
        self.assertEqual(User.USERNAME_FIELD, 'email')

    def test_user_str_retorna_email(self):
        user = create_user()
        self.assertEqual(str(user), 'test@profinanzas.com')

    def test_email_unico(self):
        create_user(email='duplicado@test.com')
        with self.assertRaises(Exception):
            create_user(email='duplicado@test.com', username='otro')


class RegisterViewTest(TestCase):
    """Tests del endpoint de registro (TDD: Red → Green → Refactor)."""

    def setUp(self):
        self.client = APIClient()
        self.payload = {
            'email': 'nuevo@profinanzas.com',
            'username': 'nuevo',
            'password': 'StrongPass123!',
            'password_confirm': 'StrongPass123!',
        }

    def test_registro_exitoso_devuelve_201_y_tokens(self):
        res = self.client.post(REGISTER_URL, self.payload, format='json')
        self.assertEqual(res.status_code, status.HTTP_201_CREATED)
        self.assertIn('tokens', res.data)
        self.assertIn('access', res.data['tokens'])
        self.assertIn('refresh', res.data['tokens'])
        self.assertIn('user', res.data)

    def test_registro_passwords_no_coinciden_devuelve_400(self):
        self.payload['password_confirm'] = 'Diferente123!'
        res = self.client.post(REGISTER_URL, self.payload, format='json')
        self.assertEqual(res.status_code, status.HTTP_400_BAD_REQUEST)

    def test_registro_email_duplicado_devuelve_400(self):
        self.client.post(REGISTER_URL, self.payload, format='json')
        res = self.client.post(REGISTER_URL, self.payload, format='json')
        self.assertEqual(res.status_code, status.HTTP_400_BAD_REQUEST)

    def test_registro_password_insegura_devuelve_400(self):
        self.payload['password'] = '123'
        self.payload['password_confirm'] = '123'
        res = self.client.post(REGISTER_URL, self.payload, format='json')
        self.assertEqual(res.status_code, status.HTTP_400_BAD_REQUEST)


class LoginViewTest(TestCase):
    """Tests del endpoint de login JWT."""

    def setUp(self):
        self.client = APIClient()
        self.user = create_user(email='login@profinanzas.com', password='StrongPass123!')

    def test_login_exitoso_devuelve_200_y_tokens(self):
        res = self.client.post(LOGIN_URL, {
            'email': 'login@profinanzas.com',
            'password': 'StrongPass123!',
        }, format='json')
        self.assertEqual(res.status_code, status.HTTP_200_OK)
        self.assertIn('access', res.data)
        self.assertIn('refresh', res.data)

    def test_login_password_incorrecta_devuelve_401(self):
        res = self.client.post(LOGIN_URL, {
            'email': 'login@profinanzas.com',
            'password': 'Incorrecta!',
        }, format='json')
        self.assertEqual(res.status_code, status.HTTP_401_UNAUTHORIZED)


class ProfileViewTest(TestCase):
    """Tests del endpoint de perfil."""

    def setUp(self):
        self.client = APIClient()
        self.user = create_user(email='perfil@profinanzas.com')
        self.client.force_authenticate(user=self.user)

    def test_obtener_perfil_autenticado_devuelve_200(self):
        res = self.client.get(PROFILE_URL)
        self.assertEqual(res.status_code, status.HTTP_200_OK)
        self.assertEqual(res.data['email'], self.user.email)

    def test_perfil_sin_autenticacion_devuelve_401(self):
        self.client.force_authenticate(user=None)
        res = self.client.get(PROFILE_URL)
        self.assertEqual(res.status_code, status.HTTP_401_UNAUTHORIZED)

    def test_actualizar_first_name_con_patch(self):
        res = self.client.patch(PROFILE_URL, {'first_name': 'Juan'}, format='json')
        self.assertEqual(res.status_code, status.HTTP_200_OK)
        self.user.refresh_from_db()
        self.assertEqual(self.user.first_name, 'Juan')


class ChangePasswordViewTest(TestCase):
    """Tests del endpoint de cambio de contraseña."""

    def setUp(self):
        self.client = APIClient()
        self.user = create_user(email='cambio@profinanzas.com', password='OldPass123!')
        self.client.force_authenticate(user=self.user)

    def test_cambio_exitoso_devuelve_200(self):
        res = self.client.put(CHANGE_PASSWORD_URL, {
            'old_password': 'OldPass123!',
            'new_password': 'NewPass456!',
            'new_password_confirm': 'NewPass456!',
        }, format='json')
        self.assertEqual(res.status_code, status.HTTP_200_OK)

    def test_cambio_con_old_password_incorrecto_devuelve_400(self):
        res = self.client.put(CHANGE_PASSWORD_URL, {
            'old_password': 'Incorrecta!',
            'new_password': 'NewPass456!',
            'new_password_confirm': 'NewPass456!',
        }, format='json')
        self.assertEqual(res.status_code, status.HTTP_400_BAD_REQUEST)
