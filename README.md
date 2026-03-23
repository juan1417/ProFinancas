# ProFinancas

Plataforma de gestión financiera personal de alto nivel. Diseñada para registrar transacciones, analizar gastos, escanear facturas y administrar billeteras desde una sola app.

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Python · Django 5 · Django REST Framework |
| Frontend | Flutter 3 · Dart |
| Autenticación | JWT (SimpleJWT) |
| Base de datos | SQLite (desarrollo) / PostgreSQL (producción) |
| Estado (Flutter) | Provider |
| HTTP client | Dio |
| Gráficas | fl_chart |
| Fuentes | Google Fonts (Inter) |

---

## Arquitectura

### Backend — Django REST Framework

El backend sigue el patrón **Service Layer + Thin Views**:

```
back/
  usuarios/
    models.py        # Modelo de usuario personalizado
    serializers.py   # Validación de entrada/salida
    services.py      # Lógica de negocio (registro, logout)
    views.py         # Solo delega a services.py
  transactions/
    models.py        # Category, Transaction
    serializers.py
    services.py      # CategoryService, TransactionService
    views.py         # ViewSets delgados
```

Endpoints principales:

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/register/` | Registro de usuario |
| POST | `/api/auth/login/` | Login → devuelve tokens JWT |
| POST | `/api/auth/logout/` | Blacklist del refresh token |
| GET/POST | `/api/transactions/` | Listar / crear transacciones |
| GET | `/api/transactions/summary/` | Resumen financiero del período |
| GET/POST | `/api/categories/` | Listar / crear categorías |

### Frontend — Flutter (Clean Architecture)

El frontend usa **Clean Architecture con estructura feature-first**:

```
pro_finanzas/lib/
  core/
    api/             # ApiClient (Dio), ApiConstants
    errors/          # Jerarquía de excepciones
    shell/           # MainShell (5 tabs + bottom nav)
    theme/           # AppColors, AppTextStyles, AppTheme
    utils/           # CurrencyFormatter, DateFormatter
    widgets/         # AppCard, ProAppBar, ProBottomNav,
                     # SegmentedTabs, PercentageBadge
  features/
    auth/
      data/          # AuthRemoteDatasource, UserModel, AuthRepositoryImpl
      domain/        # User entity, AuthRepository (abstract), UseCases
      presentation/  # AuthProvider, LoginScreen, RegisterScreen
    transactions/
      data/          # TransactionRemoteDatasource, Models, RepositoryImpl
      domain/        # Category/Transaction entities, Repository, UseCases
      presentation/  # TransactionProvider, TransactionsScreen, widgets
    dashboard/       # DashboardScreen
    scanner/         # ScannerScreen
    analytics/       # AnalyticsScreen
    wallet/          # WalletScreen
```

Cada feature sigue las capas:

```
Domain  →  abstracciones puras (entidades, repositorios, use cases)
Data    →  implementación HTTP (datasources, modelos, repositorio)
Presentation → UI + Provider (estado)
```

---

## Pantallas

| Pantalla | Descripción |
|---|---|
| Login / Register | Autenticación con JWT, biométrico placeholder |
| Dashboard | Tarjeta de portafolio, presupuesto mensual, gráfica de donut por categoría |
| Expense Manager | Lista de transacciones con búsqueda, filtros y formulario de alta |
| Invoice Scanner | Visor de cámara, extracción de datos de facturas, historial de escaneos |
| Analytics | KPIs de ingreso/gasto, gráfica de barras comparativa, breakdown por categoría |
| Wallets | Tarjeta bancaria premium, acciones rápidas, estadísticas de uso de tarjeta |

---

## Sistema de diseño

- **Color primario:** Navy blue `#1A237E`
- **Ingreso:** Verde `#2E7D32` · **Gasto:** Rojo `#C62828`
- **Tipografía:** Inter (Google Fonts), 3 escalas: Headline / Body / Label
- **Componentes:** Cards con border-radius 16, Bottom nav con FAB central, chips de filtro animados

---

## Cómo ejecutar

### Backend

```bash
cd back
pip install -r requirements.txt   # o: uv sync
python manage.py migrate
python manage.py runserver
```

### Frontend

```bash
cd pro_finanzas
flutter pub get
flutter run -d edge        # web
flutter run -d windows     # escritorio
flutter run                # dispositivo conectado
```

> El frontend apunta a `http://10.0.2.2:8000/api/` por defecto (emulador Android).
> Para web/desktop, cambia `baseUrl` en `lib/core/api/api_constants.dart`.

---

## Convenciones

- Archivos y directorios: `snake_case`
- Clases: `PascalCase`
- Montos financieros: siempre `double`, mostrar con `CurrencyFormatter.format()`
- Tipos de transacción: `'INCOME'` / `'EXPENSE'` (mayúsculas, igual que el backend)
- Lógica de negocio en Django: siempre en `services.py`, nunca en `views.py`
