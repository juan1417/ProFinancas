# syntax=docker/dockerfile:1

FROM python:3.14-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    DJANGO_SETTINGS_MODULE=profinancas.settings

WORKDIR /app/back

# Dependencias del sistema mínimas
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential libpq-dev curl \
    && rm -rf /var/lib/apt/lists/*

# Instalar uv
RUN pip install --no-cache-dir uv

# Copiar manifiesto y lock primero (mejor cache)
COPY back/pyproject.toml back/uv.lock ./
RUN uv sync --frozen --no-dev

# Copiar el código
COPY back/ ./

# Usuario no root
RUN useradd -m appuser && chown -R appuser:appuser /app
USER appuser

EXPOSE 8000

# Migraciones + gunicorn
CMD ["sh", "-c", "uv run python manage.py migrate --noinput && uv run gunicorn profinancas.wsgi:application --bind 0.0.0.0:8000 --workers 2 --timeout 60"]
