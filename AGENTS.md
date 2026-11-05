# AGENTS.md

## Repo structure

Three independent packages in one repo — no shared build system, no CI, no root-level task runner.

- `back/` — Django 6 + DRF (Python 3.14, **uv**, not pip; there is no `requirements.txt`)
- `android/` — Kotlin + Jetpack Compose (AGP 8.7.3, Gradle 8.11.1, Hilt)
- `desktop/` — Kotlin + Compose Multiplatform Desktop (Gradle 8.11.1, manual DI)

Only `back/` has tests. `android/` and `desktop/` have a single `src/main` source set — no test
sources, so "run the tests" in those packages means building, not testing.

## Backend (`back/`)

### Running tests — env vars are mandatory

`back/.env` is gitignored and absent on a fresh clone, and `settings.py` raises `ImproperlyConfigured`
without `SECRET_KEY`. `settings_test.py` imports `settings.py`, so **every** Django command fails
until you provide it. Worse: with no `DEBUG=True` in the environment, `settings.py` has already set
`SECURE_SSL_REDIRECT = True` before `settings_test.py` flips `DEBUG` — the test client then gets
`301` instead of `200`, and the suite fails ~58 tests that look like code bugs.

```bash
cd back
uv sync                                   # deps come from pyproject.toml + uv.lock
cp .env.example .env                      # then set SECRET_KEY=... and DEBUG=True
uv run pytest                             # green baseline: 91 passed

# or without touching .env:
SECRET_KEY=dev-secret DEBUG=True uv run pytest -q
```

Always export both vars before `manage.py` too. Failing to set `DEBUG=True` produces a wall of
`AssertionError: 301 != 200` — that is an env bug, not a code bug.

- Single file: `uv run pytest transactions/test_api.py`
- Single test: `uv run pytest usuarios/tests.py::LoginViewTest::test_login_exitoso_devuelve_200_y_tokens`
- `pytest.ini` pins `DJANGO_SETTINGS_MODULE=ProFinanzas.settings_test` (SQLite in-memory) plus
  `--nomigrations --reuse-db`. Tests call `force_authenticate()` because the DRF default permission
  is `IsAuthenticated`.

### Settings modules and the database

- `settings.py` (default in `manage.py`) — Postgres with `sslmode='require'`. Fails against a local
  Postgres without SSL; expects `URLDB` (Neon URI) or `DB_*` env vars.
- `settings_dev.py` — SQLite at `back/dev.sqlite3`, `ALLOWED_HOSTS = ['*']`. Use it to `runserver`
  without Postgres: `DJANGO_SETTINGS_MODULE=ProFinanzas.settings_dev uv run python manage.py runserver`.
  Delete `dev.sqlite3` to reset dev data.
- `settings_test.py` — SQLite in-memory, only for pytest.

### Migrations

All four apps have a committed `0001_initial.py` (`usuarios`, `transactions`, `budgets`,
`facturas`), and `migrations/` is **not** gitignored: a fresh database migrates with no manual
`makemigrations` step. Run `uv run python manage.py makemigrations` only after changing a model.
Tests are unaffected (`--nomigrations` builds tables from models).

`makemigrations` may print a `FileNotFoundError: .../bin/black` traceback — Django's migration
formatter finding a stale binary on PATH. The migration file is still written; ignore it.

### Backend gotchas

- Business logic goes in `services.py`; views only delegate. Exception: `facturas/` has no
  `services.py` (model/serializer-only app).
- `usuarios.models.User` — UUID primary key, `email` is `USERNAME_FIELD`.
- Transaction/category types are uppercase `'INCOME'` / `'EXPENSE'` and must match; that check lives
  in `TransactionSerializer.validate()`, **not** in `Model.clean()` (a removed duplicate that broke
  partial DRF updates — see the comment at the end of `transactions/models.py`).
- `user` is read-only on serializers, assigned server-side in `perform_create` from `request.user`.
- `transaction_date` may not be in the future; `Category` deletion is `PROTECT`ed; pagination is
  disabled by design (`DEFAULT_PAGINATION_CLASS = None`).
- Budgets: category must be `EXPENSE`, and no overlapping active budget per user + period + category.
- Auth is throttled per IP (login 10/min, register 5/hour, refresh/logout 30/min, change-password
  10/hour) — repeated manual `curl` against `/api/auth/*` returns 429.
- `POST /api/auth/register/` returns the same shape as login: `{ user, access, refresh }`.

## JVM packages (`android/`, `desktop/`)

**Both wrappers are Gradle 8.11.1, which cannot run on this machine's default JDK (OpenJDK 27).**
They need a JDK 17, which **is installed** at `/usr/lib/jvm/java-17-openjdk` (verified 17.0.20.1).
Builds abort with an opaque error whose whole message is the JVM version, e.g.
`What went wrong: 27.0.1.0.3` — that means the JDK, not your code.

Always build with JAVA_HOME pointing at 17:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
cd android && ./gradlew :app:compileDebugKotlin
cd desktop && ./gradlew compileKotlin
```

`android/local.properties` (gitignored) must exist with `sdk.dir=$HOME/Android/Sdk`; the Gradle
wrapper downloads dependencies on first run. `README.md` and older notes still say
`export JAVA_HOME=/tmp/jdk-17.0.2`, a `/tmp` path that disappears after reboot — do not use it.
Do not "fix" a failing Gradle build by editing Kotlin sources when the error is just a version
string.

```bash
cd android && ./gradlew assembleDebug      # ./gradlew installDebug to deploy
cd desktop && ./gradlew run                # ./gradlew assemble for distributions
```

- `android/local.properties` is gitignored and currently missing; CLI builds need `sdk.dir`
  (SDK lives at `~/Android/Sdk`) or `ANDROID_HOME` in the environment.
- API host: Android defaults to `10.0.2.2:8000` (emulator), desktop to `localhost:8000`. Both are
  one constant in `core/api/ApiConstants.kt`; cleartext is allowed only for `10.0.2.2`, `localhost`,
  `127.0.0.1` via `network_security_config.xml`.

### Architecture

Both apps share the same `core/{api,data/{local,model,repository},domain/{model,repository,usecase}}`
shape and the same DTO/serializer field names as the backend, but differ in wiring:

- **Android** — Hilt DI, `features/<name>/presentation/{screens,viewmodel}/`, ViewModel + StateFlow,
  all HTTP through `core/api/ApiService.kt` (never call Retrofit from a screen).
- **Desktop** — no Hilt: `di/AppModule.kt` lazy-singleton manual DI; feature files are flat
  (`FacturasScreen.kt` + `FacturasViewModel.kt`) and the "ViewModel" is a plain class owning a
  `CoroutineScope`, not AndroidX `ViewModel`. Compose Canvas charts instead of Vico, sidebar
  navigation instead of bottom bar, 1400x900 window (`Main.kt`, main class `com.profinanzas.MainKt`).
- Feature sets overlap but are not identical: both have `auth`, `categories`, `dashboard`,
  `facturas`, `transactions`, `wallet`; only Android has `scanner` (CameraX + ML Kit) and
  `analytics`. Android `facturas` lives under `presentation/screens/`, desktop's is a flat pair.

### Client gotchas

- Financial amounts are `Double` end to end, formatted with `Locale("es", "CO")`; the backend column
  is `Decimal(12,2)`.
- Android wallet cards persist in plain `SharedPreferences` under key `"cards"` as JSON (not
  DataStore) and never reach the backend. Tokens use `EncryptedSharedPreferences`.
- Biometric login buttons exist but their `onClick` is a `/* TODO: biometric auth */` placeholder —
  not wired to `BiometricPrompt`.

## Conventions

- Files/dirs `snake_case` (Python) and packages in `camelCase` dirs for Kotlin; classes `PascalCase`.
- Backend business logic in services, never in views.
- Locale `es-co`, timezone `America/Bogota`.
- Existing model/serializer docstrings, comments and API docs are Spanish; keep new prose in the
  language of the file you are editing.
- Detailed Django/DRF style rules: `.cursor/rules/django-drf-patterns.mdc`. Ignore
  `.cursor/rules/flutter-dart-patterns.mdc` — there is no Flutter/Dart code in this repo.
- Endpoint reference: `back/API_DOCUMENTATION.md`; human setup docs: `README.md` and `back/README.md`
  (note: both show test commands without the required env vars above).
