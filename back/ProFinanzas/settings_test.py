"""
Test-only settings overrides.

Used by setting `DJANGO_SETTINGS_MODULE=ProFinanzas.settings_test`
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

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.sqlite3',
        'NAME': ':memory:',
    }
}
