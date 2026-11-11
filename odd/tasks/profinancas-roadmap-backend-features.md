# ProFinancas — Funcionalidades del roadmap (backend) #11–#15

Objetivo: auditar el backend para mobile+desktop y crear las funcionalidades faltantes del roadmap
(Gestor de Tarjetas, Notificaciones de Límite, Hoja de Cálculo).
Rama: `feature/roadmap-backend-features`. Entrega: `ask-on-risk`. Runner:
`cd back && SECRET_KEY=dev-secret DEBUG=True uv run pytest`.
Restricción: sin `git commit`/`git push` sin confirmación expresa del usuario (histórico: autoriza
por fase, y en esta sesión pidió subir/mergear; se confirma cada push).

## Auditoría (estado al iniciar)

| Área | Endpoint back | Android consume | Desktop consume | Estado |
|---|---|---|---|---|
| Auth (register/login/logout/refresh/profile/change-pass) | /api/auth/* | Sí | Sí | OK |
| Categorías CRUD + by_type | /api/categories/* | Sí (sin by_type) | Sí | OK |
| Transacciones CRUD + summary + by_category | /api/transactions/* | GET/POST/DELETE (sin PUT/PATCH) | igual | OK |
| Presupuestos CRUD | /api/budgets/* | Sí | Sí | OK |
| Facturas CRUD + documento PDF | /api/invoices/* (+document) | GET/POST/PUT/DELETE (sin document) | igual | OK |
| Ingesta bancaria (preview/confirm CSV) | /api/ingestion/* | NO | NO | backend-only |
| Version/config OTA | /api/app/version /config | Sí (UpdateChecker/RemoteConfig) | Sí | OK |

## Features del roadmap faltantes (a construir)

- **#13 Gestor de Tarjetas** — hoy solo existe local en Android (SharedPreferences), sin backend.
  Crear app `cards`: modelo + CRUD. Campos (alineados al cliente): brand, bank, last4, expiry,
  holder_name, color_hex; dueño=user.
- **#15 Notificaciones de Límite** — presupuestos existen pero no exponen alertas. Endpoint
  `/api/budgets/alerts/` que calcula utilización y devuelve estado (ok/warning/exceeded).
  Umbral warning=80%, exceeded=100% (supuesto, configurable). Entrega push = alcance cliente, fuera.
- **#14 Hoja de Cálculo** — no hay export/tabla. Endpoint `/api/transactions/export/?format=csv|json`
  con filas filtradas (mismo rango que summary/by_category). Interpretación: export de datos
  (no editor en servidor).

## Tasks

| ID | Feature | Estado | Evidencia |
|---|---|---|---|
| A1 | cards: modelo+CRUD+tests | hecho | RED (4 failed/404) -> GREEN; `cards` app (models/serializers/views/urls/admin, migración 0001). last4 validado en serializador (DRF no copia validators del modelo). Suite 115 passed |
| A2 | budgets alerts: lógica+endpoint+tests | hecho | RED 4 failed/404 -> GREEN; `budgets/domain/rules.budget_utilization` (80/100) + action `alerts` en BudgetViewSet; 4 tests (ok/warning/exceeded/ownership). Suite 123 passed |
| A3 | transactions export: csv/json+tests | hecho | RED 4 failed/404 -> GREEN; action `export` en TransactionViewSet (inclusive dates, type filter, CSV attachment / JSON). 4 tests. Suite 123 passed |

## Log

- **R1** (2026-11-11, usuario, verbatim): pide auditoría del back para que funcione en mobile y desktop,
  y crear las nuevas funcionalidades según issues #11–#15 (tarjetas, sincronización, notificaciones de
  límite, hoja de cálculo, dashboard).

- **R2** (2026-11-11): Auditoría + features A1/A2/A3 implementados y desplegados.
  - Auditoría: 11 endpoints consumidos por mobile+desktop (auth, categories, transactions,
    budgets, invoices, summary, by_category, app/version, app/config). ingestion y document
    son backend-only (ningún cliente los llama todavía).
  - A1 Gestor de Tarjetas: app `cards` (modelo, serializer, viewset, urls, admin, migración)
    con campos alineados al wallet local de Android (brand, bank, last4, expiry, holder_name,
    color_hex). 4 tests (create, ownership isolation, last4 validation, update/delete).
  - A2 Notificaciones de Límite: `budgets/domain/rules.budget_utilization` (80% warning, 100%
    exceeded) + `GET /api/budgets/alerts/` por presupuesto activo. 4 tests.
  - A3 Hoja de Cálculo: `GET /api/transactions/export/?export=csv|json` con rango inclusivo y
    filtro por tipo. 4 tests.
  - Suite: 123 passed. Desplegado en Vercel (back-wine-nu.vercel.app) y verificado en vivo:
    cards CRUD, budgets/alerts, transactions/export responden 200/201.
  - Supuestos: alertas se calculan on-demand (no push); export es descarga CSV/JSON
    (no editor en servidor); tarjetas usan los mismos campos que el wallet local de Android.
