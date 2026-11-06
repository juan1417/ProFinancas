import os
import sys

# Añadir el directorio del backend al path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "back"))

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "ProFinanzas.settings")

import django
django.setup()

from django.core.wsgi import get_wsgi_application

app = get_wsgi_application()
