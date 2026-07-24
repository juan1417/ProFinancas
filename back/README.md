# Backend — ProFinanzas

Django 6 + Django REST Framework API for the ProFinanzas personal finance platform.

## Setup

```bash
cd back
uv sync
uv run python manage.py migrate
uv run python manage.py runserver
```

## Environment Variables

Copy `.env.example` to `.env` and configure:

- `SECRET_KEY` — Django secret key
- `DEBUG` — `True` for development, `False` for production
- `URLDB` — PostgreSQL connection string (Neon format)
- `ALLOWED_HOSTS` — Comma-separated hostnames

## Running Tests

```bash
uv run pytest
```

Tests use `pytest-django` with SQLite in-memory (`settings_test.py`). No running Postgres needed.

## Apps

| App | Purpose | Endpoints |
|---|---|---|
| `usuarios/` | User auth | `/api/auth/*` |
| `transactions/` | Categories + Transactions | `/api/categories/*`, `/api/transactions/*` |
| `budgets/` | Budget management | `/api/budgets/*` |
| `facturas/` | Invoice management | `/api/invoices/*` |

## Architecture

- Service Layer + Thin Views: business logic in `services.py`, views only delegate
- Custom User model with UUID primary key and email-based authentication
- JWT authentication with refresh token rotation and blacklisting
- Rate limiting on auth endpoints
