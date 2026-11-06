"""
WSGI config for profinancas project.

It exposes the WSGI callable as a module-level variable named ``application``.
``app`` is also exported for serverless platforms (Vercel) that look for it.
"""

import os

from django.core.wsgi import get_wsgi_application

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'profinancas.settings')

application = get_wsgi_application()
app = application
