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
- **Modo offline-ready** — Arquitectura preparada para caché local

---

## Pantallas

| Pantalla | Descripción |
|---|---|
| Login / Registro | Autenticación con JWT |
| Dashboard | Portafolio, presupuesto y gráfica de donut por categoría |
| Expense Manager | Lista de transacciones con búsqueda y filtros |
| Invoice Scanner | Visor de cámara y revisión de datos extraídos |
| Analytics | KPIs, gráfica de barras y breakdown por categoría |
| Wallets | Tarjeta bancaria, estadísticas y acciones rápidas |

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

### Backend — Service Layer + Thin Views

La lógica de negocio vive en `services.py`. Los `views.py` solo reciben la petición y delegan.

```
back/
  usuarios/
    models.py        # Usuario personalizado
    serializers.py   # Validación de entrada/salida
    services.py      # Lógica: registro, logout
    views.py         # Delega a services.py
  transactions/
    models.py        # Category, Transaction
    serializers.py
    services.py      # CategoryService, TransactionService
    views.py         # ViewSets delgados
```

### Frontend — Clean Architecture (feature-first)

```
pro_finanzas/lib/
  core/
    api/             # ApiClient (Dio), ApiConstants
    errors/          # Jerarquía de excepciones
    shell/           # MainShell (navegación 5 tabs)
    theme/           # AppColors, AppTextStyles, AppTheme
    utils/           # CurrencyFormatter, DateFormatter
    widgets/         # Componentes reutilizables
  features/
    auth/            # Login, registro, JWT
    transactions/    # CRUD de transacciones y categorías
    dashboard/       # Pantalla principal
    scanner/         # Escáner de facturas
    analytics/       # Estadísticas y gráficas
    wallet/          # Billeteras y tarjetas
```

Cada feature sigue tres capas:

```
Domain       →  entidades, repositorios abstractos, use cases
Data         →  datasources HTTP, modelos JSON, implementación
Presentation →  Provider (estado) + Screens + Widgets
```

---

## Sistema de diseño

- **Color primario:** Navy blue `#1A237E`
- **Ingresos:** Verde `#2E7D32` · **Gastos:** Rojo `#C62828`
- **Tipografía:** Inter — 3 escalas: Headline / Body / Label
- **Cards:** Border-radius 16, fondo blanco, borde sutil
- **Navegación:** Bottom nav con FAB central (escáner)
- **Botones:** 4 variantes — Primary, Secondary, Inverted, Outlined

---

## API — Endpoints principales

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/register/` | Registro de usuario |
| POST | `/api/auth/login/` | Login → tokens JWT |
| POST | `/api/auth/logout/` | Invalidar refresh token |
| GET/POST | `/api/transactions/` | Listar / crear transacciones |
| GET | `/api/transactions/summary/` | Resumen financiero del período |
| GET/POST | `/api/categories/` | Listar / crear categorías |

---

## Cómo ejecutar

### Backend

```bash
cd back
pip install -r requirements.txt
python manage.py migrate
python manage.py runserver
```

### Frontend

```bash
cd pro_finanzas
flutter pub get
flutter run -d edge        # Navegador web
flutter run -d windows     # Escritorio Windows
flutter run                # Dispositivo/emulador conectado
```

> Para web y desktop, actualiza `baseUrl` en `lib/core/api/api_constants.dart` apuntando a `http://localhost:8000/api/`.

---

## Convenciones de código

- Archivos y directorios: `snake_case` · Clases: `PascalCase`
- Montos financieros: siempre `double`, mostrar con `CurrencyFormatter.format()`
- Tipos de transacción: `'INCOME'` / `'EXPENSE'` (mayúsculas, igual que el backend)
- Lógica de negocio en Django: siempre en `services.py`, nunca directamente en `views.py`
- Llamadas HTTP en Flutter: solo desde la capa `data/datasources/`, nunca desde Screens
