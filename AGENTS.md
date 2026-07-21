# AGENTS.md

## Repo structure

Three independent packages in one repo — no shared build system between them.

- `back/` — Django 6 + DRF backend (Python 3.14)
- `android/` — Kotlin + Jetpack Compose Android app
- `desktop/` — Kotlin + Compose Multiplatform Desktop app

## Backend (`back/`)

### Dependency management

Uses **uv**, not pip. The main README's `pip install -r requirements.txt` is wrong — no such file exists.

```bash
cd back
uv sync                    # install all deps
uv run pytest              # run tests
uv run python manage.py runserver  # start dev server
```

### Testing

Tests use **pytest** with `pytest-django`. Config is in `back/pytest.ini`.

- Settings module: `ProFinanzas.settings_test` (SQLite in-memory, no Postgres needed)
- Run all tests: `uv run pytest` (from `back/`)
- Run a single file: `uv run pytest transactions/test_api.py`
- Run a single test: `uv run pytest transactions/test_api.py::TestTransactionAPI::test_create_transaction`
- Markers: `unit`, `integration`, `slow` (defined but not widely used yet)
- Tests use `force_authenticate()` because the default permission is `IsAuthenticated` — every API test must authenticate or it tests nothing real.

### Architecture

- **Service Layer + Thin Views**: business logic lives in `services.py`, `views.py` only delegates.
- User model (`usuarios.models.User`) uses **UUID primary key** and **email** as `USERNAME_FIELD`.
- Transaction types are uppercase strings: `'INCOME'` / `'EXPENSE'` — must match the category's type (validated in serializer).
- `user` field on serializers is **read-only** — assigned server-side in `perform_create` from `request.user`.
- Migrations are gitignored (`migrations/` in `.gitignore`). Run `uv run python manage.py migrate` after pulls.

### Apps

| App | Purpose | Key endpoints |
|-----|---------|---------------|
| `usuarios/` | User auth (register, login, profile) | `/api/auth/*` |
| `transactions/` | Categories + Transactions CRUD | `/api/categories/*`, `/api/transactions/*` |
| `budgets/` | Budget management | `/api/budgets/*` |

### Gotchas

- `POST /api/auth/register/` returns the same shape as login: `{ user, access, refresh }`.
- Transaction date cannot be in the future (serializer validation).
- Category deletion is protected if it has transactions (`on_delete=PROTECT`).
- Pagination is disabled (`DEFAULT_PAGINATION_CLASS = None`) — all results return at once.
- Budget category must be EXPENSE type (validated in serializer).
- Budget duplicate check: no overlapping active budgets for same user + period + category.

## Android (`android/`)

### Running

Open `android/` in Android Studio. The project uses Gradle Kotlin DSL.

```bash
cd android
./gradlew assembleDebug       # build debug APK
./gradlew installDebug        # build + install on device
```

For the emulator, the API host defaults to `10.0.2.2:8000`. Override in `ApiConstants.kt` for physical devices.

### Architecture

Clean Architecture with MVVM + Hilt DI:

```
android/app/src/main/java/com/profinanzas/
  core/
    api/              → Retrofit ApiService, AuthInterceptor, constants
    data/
      model/          → DTOs (data classes with Gson annotations)
      repository/     → Repository implementations
    domain/
      model/          → Domain entities
      repository/     → Repository interfaces
      usecase/        → Use cases (auth, category, transaction, budget)
    ui/
      theme/          → Color, Typography, Theme (Material 3)
      navigation/     → Screen routes, NavGraph
      components/     → Reusable composables (AppBar, BottomNav, Cards, etc.)
  features/
    auth/             → Login, Register, Profile
    transactions/     → CRUD + summary + filters
    categories/       → CRUD + type filtering
    dashboard/        → Balance card, budget progress, expense breakdown
    scanner/          → CameraX + ML Kit OCR
    analytics/        → Bar charts, KPIs, category breakdown
    wallet/           → Local card storage (DataStore)
  di/                 → Hilt modules (NetworkModule, AppModule)
```

### Key dependencies

| Purpose | Library |
|---------|---------|
| HTTP | Retrofit + OkHttp + Gson |
| DI | Hilt |
| UI | Jetpack Compose + Material 3 |
| State | ViewModel + StateFlow |
| Charts | Vico (compose-m3) |
| Camera | CameraX |
| OCR | ML Kit Text Recognition |
| Biometry | BiometricPrompt (AndroidX) |
| Secure Storage | EncryptedSharedPreferences |
| Local Storage | DataStore Preferences |

### Conventions

- State management: **ViewModel + StateFlow** (not Provider)
- Financial amounts: always `Double`, format with `NumberFormat.getCurrencyInstance(Locale("es", "CO"))`
- Transaction types are uppercase strings: `'INCOME'` / `'EXPENSE'` — must match backend
- API base URL in `ApiConstants.kt` — `10.0.2.2` for emulator, `localhost` for device
- Network security config allows cleartext for localhost/10.0.2.2
- All API calls go through `ApiService.kt` — never call Retrofit directly from screens
- Biometric login: buttons exist but `onTap` is placeholder (needs wiring to BiometricPrompt)
- Wallet cards are stored locally only (SharedPreferences) — not sent to backend

## Desktop (`desktop/`)

### Running

```bash
cd desktop
export JAVA_HOME=/tmp/jdk-17.0.2   # Gradle 8.11.1 requires JDK 17
./gradlew run                         # run desktop app
./gradlew assemble                    # build distributions
```

### Architecture

Standalone Kotlin/JVM + Compose Desktop app with manual DI (no Hilt):

```
desktop/src/main/kotlin/com/profinanzas/
  core/
    api/              → Retrofit ApiService, AuthInterceptor, constants
    data/
      model/          → DTOs (data classes with Gson annotations)
      repository/     → Repository implementations
    domain/
      model/          → Domain entities (shared with Android)
      repository/     → Repository interfaces (shared with Android)
      usecase/        → Use cases (auth, category, transaction, budget)
    ui/
      theme/          → Color, Typography, Theme (Material 3, no Android deps)
      components/     → AppCard, StatusPill, PercentageBadge
  features/
    auth/             → Login, Register, Profile screens + AuthViewModel
    dashboard/        → DashboardScreen (4-col stats, charts, recent txns, budgets)
    facturas/         → FacturasScreen (table + filters + detail panel)
    wallet/           → WalletScreen (cards + budget progress)
  ui/navigation/
    Sidebar.kt        → Desktop sidebar nav (260px, icons + labels)
    TopBar.kt         → Desktop top bar (title + date + avatar)
    DesktopNavGraph.kt → Screen routing with sidebar shell
    Screen.kt         → Route definitions
  di/
    AppModule.kt      → Manual DI container (lazy singletons)
  Main.kt            → Window entry point (1400x900)
  ProFinanzasApp.kt  → App composable (theme wrapper)
```

### Key differences from Android

- **No Hilt** — manual DI via `AppModule` object with lazy singletons
- **No CameraX/ML Kit/Biometric** — desktop doesn't have camera/biometric
- **Sidebar navigation** instead of bottom navigation bar
- **Wider layout** — 4-column stats grid, table + detail panel for facturas
- **API host** defaults to `localhost:8000` (not `10.0.2.2`)
- **JVM-only** — no Android dependencies, pure Kotlin/JVM + Compose Desktop

### Key dependencies

| Purpose | Library |
|---------|---------|
| UI | Compose Desktop (Material 3) |
| HTTP | Retrofit + OkHttp + Gson |
| State | Coroutine StateFlow (no ViewModel) |
| DI | Manual (AppModule singleton) |

## Cross-cutting

- No CI/CD workflows in the repo.
- No `.env` committed — copy `.env.example` to `.env` in `back/`.
- Locale: `es-co`, timezone `America/Bogota`.
- Backend runs on port 8000 by default.
