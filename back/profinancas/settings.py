"""
Django settings for profinancas project.
"""

from pathlib import Path
from datetime import timedelta
import os

BASE_DIR = Path(__file__).resolve().parent.parent

SECRET_KEY = os.getenv('SECRET_KEY') or os.getenv('VERCEL_SECRET_KEY') or 'django-insecure-fallback-key-change-me'

DEBUG = os.getenv('DEBUG', 'False').lower() in ('true', '1', 'yes')

_allowed = os.getenv('ALLOWED_HOSTS', '')
if _allowed:
    ALLOWED_HOSTS = [h.strip() for h in _allowed.split(',') if h.strip()]
elif DEBUG:
    ALLOWED_HOSTS = ['*']
else:
    ALLOWED_HOSTS = [
        '*.vercel.app',
        'localhost',
        '127.0.0.1',
    ]

# Application definition
INSTALLED_APPS = [
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'django.contrib.staticfiles',
    'corsheaders',
    'rest_framework',
    'rest_framework_simplejwt',
    'rest_framework_simplejwt.token_blacklist',
    'django_filters',
    'usuarios',
    'transactions',
    'budgets',
    'facturas',
    'ingestion',
]

MIDDLEWARE = [
    'django.middleware.security.SecurityMiddleware',
    'corsheaders.middleware.CorsMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
]

ROOT_URLCONF = 'profinancas.urls'

TEMPLATES = [
    {
        'BACKEND': 'django.template.backends.django.DjangoTemplates',
        'DIRS': [],
        'APP_DIRS': True,
        'OPTIONS': {
            'context_processors': [
                'django.template.context_processors.debug',
                'django.template.context_processors.request',
                'django.contrib.auth.context_processors.auth',
                'django.contrib.messages.context_processors.messages',
            ],
        },
    },
]

WSGI_APPLICATION = 'profinancas.wsgi.application'

# ─── Base de Datos ────────────────────────────────────────────────────────────
# En entorno de tests se usa SQLite en memoria para velocidad (TDD)
TESTING = os.getenv('DJANGO_TESTING', 'False') == 'True'

if TESTING:
    DATABASES = {
        'default': {
            'ENGINE': 'django.db.backends.sqlite3',
            'NAME': ':memory:',
        }
    }
else:
    # Prioridad: URLDB (URI completa) > variables individuales DB_*
    url_db = os.getenv('URLDB')

    if url_db:
        from urllib.parse import urlparse
        parsed = urlparse(url_db)
        DATABASES = {
            'default': {
                'ENGINE': 'django.db.backends.postgresql',
                'NAME': parsed.path.lstrip('/').split('?')[0],
                'USER': parsed.username,
                'PASSWORD': parsed.password,
                'HOST': parsed.hostname,
                'PORT': parsed.port or 5432,
                'OPTIONS': {
                    'sslmode': 'require',
                },
            }
        }
    else:
        DATABASES = {
            'default': {
                'ENGINE': 'django.db.backends.postgresql',
                'NAME': os.getenv('DB_NAME', 'postgres'),
                'USER': os.getenv('DB_USER', 'postgres'),
                'PASSWORD': os.getenv('DB_PASSWORD'),
                'HOST': os.getenv('DB_HOST', 'localhost'),
                'PORT': os.getenv('DB_PORT', '5432'),
                'OPTIONS': {
                    'sslmode': 'require',
                },
            }
        }



# ─── Autenticación ────────────────────────────────────────────────────────────
AUTH_USER_MODEL = 'usuarios.User'

AUTH_PASSWORD_VALIDATORS = [
    {'NAME': 'django.contrib.auth.password_validation.UserAttributeSimilarityValidator'},
    {'NAME': 'django.contrib.auth.password_validation.MinimumLengthValidator'},
    {'NAME': 'django.contrib.auth.password_validation.CommonPasswordValidator'},
    {'NAME': 'django.contrib.auth.password_validation.NumericPasswordValidator'},
]

PASSWORD_HASHERS = [
    'django.contrib.auth.hashers.Argon2PasswordHasher',
    'django.contrib.auth.hashers.BCryptSHA256PasswordHasher',
    'django.contrib.auth.hashers.PBKDF2PasswordHasher',
]

# ─── Django REST Framework ────────────────────────────────────────────────────
REST_FRAMEWORK = {
    'DEFAULT_AUTHENTICATION_CLASSES': (
        'rest_framework_simplejwt.authentication.JWTAuthentication',
    ),
    'DEFAULT_PERMISSION_CLASSES': (
        'rest_framework.permissions.IsAuthenticated',
    ),
    'DEFAULT_FILTER_BACKENDS': (
        'django_filters.rest_framework.DjangoFilterBackend',
        'rest_framework.filters.SearchFilter',
        'rest_framework.filters.OrderingFilter',
    ),
    'DEFAULT_PAGINATION_CLASS': None,  # Finanzas personales: a lo sumo cientos de items por usuario, no necesitamos paginar. Si crece, se reactiva.
    # Rate limiting para evitar brute force en /api/auth/login/ y
    # /api/auth/register/. Limites: 10/min para login, 5/hora para
    # register. Configurable desde env si se quiere tunear.
    'DEFAULT_THROTTLE_CLASSES': (
        'rest_framework.throttling.AnonRateThrottle',
        'rest_framework.throttling.UserRateThrottle',
    ),
    'DEFAULT_THROTTLE_RATES': {
        'anon': '60/min',
        'user': '600/min',
        'login': os.getenv('THROTTLE_LOGIN', '10/min'),
        'register': os.getenv('THROTTLE_REGISTER', '5/hour'),
        'refresh': '30/min',
        'logout': '30/min',
        'change_password': '10/hour',
    },
}

# ─── SimpleJWT ────────────────────────────────────────────────────────────────
SIMPLE_JWT = {
    'ACCESS_TOKEN_LIFETIME': timedelta(minutes=60),
    'REFRESH_TOKEN_LIFETIME': timedelta(days=1),
    'ROTATE_REFRESH_TOKENS': True,
    'BLACKLIST_AFTER_ROTATION': True,
    'UPDATE_LAST_LOGIN': True,
    'ALGORITHM': 'HS256',
    'AUTH_HEADER_TYPES': ('Bearer',),
    'USER_ID_FIELD': 'id',
    'USER_ID_CLAIM': 'user_id',
}

# ─── Internacionalización ─────────────────────────────────────────────────────
LANGUAGE_CODE = 'es-co'
TIME_ZONE = 'America/Bogota'
USE_I18N = True
USE_TZ = True

# ─── CORS ─────────────────────────────────────────────────────────────────────
_cors_raw = os.getenv('CORS_ALLOWED_ORIGINS', '')
if _cors_raw.strip() == '*':
    CORS_ALLOW_ALL_ORIGINS = True
    CORS_ALLOWED_ORIGINS = []
else:
    CORS_ALLOWED_ORIGINS = [
        origin.strip()
        for origin in _cors_raw.split(',')
        if origin.strip()
    ]
    if not CORS_ALLOWED_ORIGINS:
        CORS_ALLOWED_ORIGINS = [
            'http://localhost:8000',
            'http://127.0.0.1:8000',
            'http://10.0.2.2:8000',
        ]
CORS_ALLOW_CREDENTIALS = True

# ─── Seguridad HTTP (solo cuando DEBUG=False) ─────────────────────────────────
if not DEBUG:
    SECURE_SSL_REDIRECT = True
    SECURE_HSTS_SECONDS = 31536000  # 1 year
    SECURE_HSTS_INCLUDE_SUBDOMAINS = True
    SECURE_HSTS_PRELOAD = True
    SESSION_COOKIE_SECURE = True
    CSRF_COOKIE_SECURE = True
    SECURE_PROXY_SSL_HEADER = ('HTTP_X_FORWARDED_PROTO', 'https')

# ─── Logging ───────────────────────────────────────────────────────────────────
import os
os.makedirs(BASE_DIR / 'logs', exist_ok=True)

# Logging: solo console (compatible con Vercel/serverless donde el FS es read-only).
# El handler de archivo solo se activa si el directorio de logs es escribible.
import os as _os
_LOG_DIR = BASE_DIR / 'logs'
_SECURITY_HANDLERS = ['console']
_handlers = {
    'console': {
        'level': 'WARNING',
        'class': 'logging.StreamHandler',
        'formatter': 'security',
    },
}
if not _os.getenv('VERCEL') and not _os.getenv('VERCEL_ENV'):
    try:
        _LOG_DIR.mkdir(parents=True, exist_ok=True)
        _test = _LOG_DIR / 'security.log'
        with open(_test, 'a'):
            pass
        _handlers['security_file'] = {
            'level': 'INFO',
            'class': 'logging.FileHandler',
            'filename': str(_test),
            'formatter': 'security',
        }
        _SECURITY_HANDLERS = ['security_file', 'console']
    except (OSError, IOError):
        pass

LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,
    'formatters': {
        'security': {
            'format': '{asctime} {levelname} {message}',
            'style': '{',
        },
    },
    'handlers': _handlers,
    'loggers': {
        'profinanzas.security': {
            'handlers': _SECURITY_HANDLERS,
            'level': 'INFO',
            'propagate': False,
        },
    },
}

# ─── Archivos estáticos ───────────────────────────────────────────────────────
STATIC_URL = 'static/'

DEFAULT_AUTO_FIELD = 'django.db.models.BigAutoField'
