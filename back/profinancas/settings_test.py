"""
Test-only settings overrides.

Used by setting `DJANGO_SETTINGS_MODULE=profinancas.settings_test`
or by pytest when DJANGO_TESTING is set. Forces SQLite in-memory so
tests don't need a running Postgres instance.

Why not just toggle on DJANGO_TESTING in the main settings.py?
Because pytest-django imports the settings module as soon as it
sees DJANGO_SETTINGS_MODULE in pytest.ini — before any conftest
hooks fire — so we can't set the env var early enough from a
conftest alone. Easier: have a dedicated settings module that
always uses SQLite, and tell pytest to use it.
"""
from .settings import *  # noqa: F401,F403

DEBUG = True

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.sqlite3',
        'NAME': ':memory:',
    }
}

# Tests make requests against 'testserver' (Django's default test host).
ALLOWED_HOSTS = ['*']

# Evita 301 por SECURE_SSL_REDIRECT cuando DEBUG=False en el entorno:
# settings.py fija SECURE_SSL_REDIRECT=True antes de que este modulo ponga DEBUG=True.
SECURE_SSL_REDIRECT = False
SESSION_COOKIE_SECURE = False
CSRF_COOKIE_SECURE = False

# Los tests disparan muchas peticiones desde la misma IP (testserver) y el
# throttling real (5/hora en register) las bloquearia con 429. Se suben los
# limites en test para no interferir con la suite; la logica de throttling
# sigue activa en produccion (settings.py).
REST_FRAMEWORK = {**REST_FRAMEWORK, 'DEFAULT_THROTTLE_RATES': {
    **REST_FRAMEWORK['DEFAULT_THROTTLE_RATES'],
    'register': '10000/hour',
    'login': '10000/min',
    'refresh': '10000/min',
    'logout': '10000/min',
    'change_password': '10000/hour',
}}
