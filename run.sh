#!/usr/bin/env bash
# Ejecuta ProFinancas en modo desarrollo.
# Uso:
#   ./run.sh            -> solo backend (Django dev server en :8000)
#   ./run.sh desktop    -> backend + app desktop (Compose)
#   ./run.sh android    -> backend + build/install de la app Android
#   ./run.sh test       -> suite de tests del backend
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACK="$ROOT/back"
ANDROID="$ROOT/android"
DESKTOP="$ROOT/desktop"

JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk}"
export JAVA_HOME

# ─── Backend ────────────────────────────────────────────────────────────────
cd "$BACK"

if [ ! -f .env ]; then
    echo ">> Creando back/.env desde .env.example"
    cp .env.example .env
    # SECRET_KEY mínimo para desarrollo si no existe
    if ! grep -q '^SECRET_KEY=' .env; then
        echo "SECRET_KEY=dev-secret-change-me" >> .env
    fi
    if ! grep -q '^DEBUG=' .env; then
        echo "DEBUG=True" >> .env
    fi
fi

# Cargar .env al entorno
set -a
# shellcheck disable=SC1091
source .env
set +a
# Si SECRET_KEY/DEBUG vienen vacios del .env, usar defaults de desarrollo
export SECRET_KEY="${SECRET_KEY:-dev-secret-change-me}"
[ -z "$SECRET_KEY" ] && export SECRET_KEY="dev-secret-change-me"
export DEBUG="${DEBUG:-True}"
[ "$DEBUG" = "False" ] && [ "${1:-}" = "backend" ] && export DEBUG=True

echo ">> Instalando dependencias del backend (uv)..."
uv sync --quiet

echo ">> Aplicando migraciones (settings_dev, SQLite)..."
DJANGO_SETTINGS_MODULE=profinancas.settings_dev uv run python manage.py migrate --run-syncdb 2>/dev/null || \
    DJANGO_SETTINGS_MODULE=profinancas.settings_dev uv run python manage.py migrate

start_backend() {
    echo ">> Levantando backend en http://127.0.0.1:8000 (Ctrl+C para detener)"
    DJANGO_SETTINGS_MODULE=profinancas.settings_dev \
        SECRET_KEY="$SECRET_KEY" DEBUG="$DEBUG" \
        uv run python manage.py runserver 0.0.0.0:8000
}

case "${1:-backend}" in
    backend)
        start_backend
        ;;
    test)
        echo ">> Ejecutando suite de tests del backend..."
        SECRET_KEY="${SECRET_KEY:-dev-secret-change-me}" DEBUG=True uv run pytest
        ;;
    desktop)
        start_backend &
        BACKEND_PID=$!
        trap 'kill $BACKEND_PID 2>/dev/null || true' EXIT
        sleep 3
        echo ">> Compilando y ejecutando app desktop..."
        cd "$DESKTOP"
        JAVA_HOME="$JAVA_HOME" ./gradlew run
        ;;
    android)
        start_backend &
        BACKEND_PID=$!
        trap 'kill $BACKEND_PID 2>/dev/null || true' EXIT
        sleep 3
        echo ">> Compilando app Android..."
        cd "$ANDROID"
        JAVA_HOME="$JAVA_HOME" ./gradlew :app:assembleDebug
        echo ">> APK en: $ANDROID/app/build/outputs/apk/debug/app-debug.apk"
        if command -v adb >/dev/null 2>&1; then
            echo ">> Instalando en dispositivo/emulador conectado..."
            JAVA_HOME="$JAVA_HOME" ./gradlew :app:installDebug
        fi
        ;;
    *)
        echo "Uso: $0 [backend|test|desktop|android]"
        exit 1
        ;;
esac
