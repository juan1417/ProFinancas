"""
Dev-only settings override — points Django at a local SQLite file so
you can `runserver` without spinning up Postgres.

Usage:
    set DJANGO_SETTINGS_MODULE=ProFinanzas.settings_dev
    python manage.py runserver

Everything else is inherited from `settings.py`. The SQLite file lives
at `back/dev.sqlite3`; delete it to reset the dev DB.
"""
from .settings import *  # noqa: F401,F403

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.sqlite3',
        'NAME': str(BASE_DIR / 'dev.sqlite3'),  # noqa: F405
    }
}

# Loosen CORS / host for local dev when running on a phone or
# simulator that reaches the laptop over LAN.
ALLOWED_HOSTS = ['*']
