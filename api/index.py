import os
import sys
import traceback

# Añadir el directorio del backend al path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "back"))

# Env vars por defecto (Vercel las sobreescribe con las configuradas en el dashboard)
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "profinancas.settings")
os.environ.setdefault("SECRET_KEY", "dev-secret-change-me-in-prod")
os.environ.setdefault("DEBUG", "False")
os.environ.setdefault("ALLOWED_HOSTS", "*")
os.environ.setdefault("DJANGO_TESTING", "True")

_error = None
app = None

try:
    import django
    django.setup()
    from django.core.wsgi import get_wsgi_application
    app = get_wsgi_application()
except Exception as e:
    _error = traceback.format_exc()
    # Fallback: una app WSGI mínima que devuelve el error para depurar
    def app(environ, start_response):
        status = "500 Internal Server Error"
        headers = [("Content-Type", "text/plain")]
        start_response(status, headers)
        return [_error.encode("utf-8")]

handler = app
