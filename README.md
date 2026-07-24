# ProFinancas

> Plataforma de gestión financiera personal de alto nivel.

---

## Descripción

ProFinancas es una aplicación fullstack para el control y análisis de finanzas personales. Permite registrar ingresos y gastos, visualizar el estado financiero en tiempo real, escanear facturas automáticamente y administrar múltiples billeteras desde una interfaz moderna y elegante.

---

## Características

- **Registro de transacciones** — Crea ingresos y gastos con categoría, descripción y fecha
- **Dashboard financiero** — Tarjeta de portafolio con balance, presupuesto mensual y gráfica de distribución de gastos
- **Escáner de facturas** — Captura y extrae datos de facturas automáticamente para registrarlas sin escribir nada
- **Analíticas avanzadas** — Gráfica comparativa de ingresos vs gastos, KPIs por período y desglose por categoría
- **Gestión de billeteras** — Vista de tarjetas bancarias, tasa de ahorro y acciones rápidas (transferir, agregar fondos)
- **Autenticación segura** — Login y registro con tokens JWT; soporte para Touch ID y Face ID (placeholder)
- **Presupuestos** — Define límites mensuales por categoría y monitorea tu progreso

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Python · Django 6 · Django REST Framework |
| Android | Kotlin · Jetpack Compose · Material 3 · Hilt |
| Desktop | Kotlin · Compose Desktop · Material 3 |
| Autenticación | JWT (SimpleJWT) |
| Base de datos | PostgreSQL (Neon) |
| HTTP client (mobile/desktop) | Retrofit + OkHttp + Gson |
| Gráficas (Android) | Vico |
| Gráficas (Desktop) | Compose Canvas |

---

## Cómo ejecutar

### Backend

```bash
cd back
uv sync
uv run python manage.py migrate
uv run python manage.py runserver
```

### Android

```bash
cd android
./gradlew assembleDebug   # Build debug APK
./gradlew installDebug     # Build + install on device
```

Emulador usa `10.0.2.2:8000` como API host. Dispositivo físico: cambia `ApiConstants.kt`.

### Desktop

```bash
cd desktop
export JAVA_HOME=/tmp/jdk-17.0.2  # Gradle 8.11.1 requires JDK 17
./gradlew run
```

---

## Arquitectura

### Backend — Service Layer + Thin Views

La lógica de negocio vive en `services.py`. Los `views.py` solo reciben la petición y delegan.

```
back/
  usuarios/         # Auth (register, login, profile, change-password)
  transactions/     # Categories + Transactions CRUD + Summary
  budgets/          # Budget management
  facturas/         # Invoices CRUD
```

### Android — Clean Architecture + MVVM + Hilt

```
android/app/src/main/java/com/profinanzas/
  core/
    api/             → Retrofit ApiService, AuthInterceptor
    data/            → DTOs, Repository implementations
    domain/          → Entities, Repository interfaces, UseCases
    ui/              → Theme, Navigation, Reusable components
  features/
    auth/            → Login, Register, Profile
    transactions/    → CRUD + summary + filters
    categories/      → CRUD + type filtering
    dashboard/       → Balance card, budget progress, expense breakdown
    scanner/         → CameraX + ML Kit OCR
    analytics/       → Bar charts, KPIs, category breakdown
    wallet/          → Local card storage (DataStore)
  di/                → Hilt modules
```

### Desktop — Kotlin/JVM + Compose Desktop + Manual DI

```
desktop/src/main/kotlin/com/profinanzas/
  core/
    api/             → Retrofit ApiService, AuthInterceptor
    data/            → DTOs, Repository implementations
    domain/          → Entities, Repository interfaces, UseCases
    ui/              → Theme, Reusable components
  features/
    auth/            → Login, Register, Profile
    dashboard/       → Balance card, charts, recent transactions
    facturas/        → Invoice table + filters + detail panel
    wallet/          → Card storage + budget progress
    transactions/    → CRUD + category picker
    categories/      → CRUD + type filtering
  ui/navigation/     → Sidebar + TopBar + NavGraph
  di/                → Manual DI (AppModule)
```

---

## API — Endpoints principales

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/register/` | Registro de usuario |
| POST | `/api/auth/login/` | Login → tokens JWT |
| POST | `/api/auth/logout/` | Invalidar refresh token |
| POST | `/api/auth/token/refresh/` | Renovar access token |
| GET/PUT/PATCH | `/api/auth/profile/` | Ver / actualizar perfil |
| POST | `/api/auth/change-password/` | Cambiar contraseña |
| GET/POST | `/api/transactions/` | Listar / crear transacciones |
| GET | `/api/transactions/summary/` | Resumen financiero del período |
| GET/POST | `/api/categories/` | Listar / crear categorías |
| GET/POST | `/api/budgets/` | Listar / crear presupuestos |
| GET/POST | `/api/invoices/` | Listar / crear facturas |

---

## Convenciones de código

- Archivos y directorios: `snake_case` · Clases: `PascalCase`
- Montos financieros: siempre `double`, formatear con locale `es-CO`
- Tipos de transacción: `'INCOME'` / `'EXPENSE'` (mayúsculas, igual que el backend)
- Lógica de negocio en Django: siempre en `services.py`, nunca directamente en `views.py`
- Android: ViewModel + StateFlow, Hilt DI, Jetpack Compose
- Desktop: Plain class + CoroutineScope, manual DI, Compose Desktop
