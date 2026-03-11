# Motor de Transacciones - Documentación de API

## Descripción General

El Motor de Transacciones es el núcleo del sistema ProFinanzas. Proporciona funcionalidad completa para:
- **Categorización de gastos e ingresos**: Permite crear y gestionar categorías personalizadas
- **Creación de movimientos manuales**: Registra transacciones financieras con validación robusta
- **Análisis financiero**: Proporciona resúmenes y agrupaciones de datos

## Base URL

```
http://localhost:8000/api/
```

---

## Endpoints de Categorías

### 1. Listar Categorías
**GET** `/api/categories/`

Lista todas las categorías del usuario autenticado.

**Parámetros de query opcionales:**
- `type`: Filtrar por tipo (`INCOME` o `EXPENSE`)
- `is_active`: Filtrar por estado (`true` o `false`)
- `search`: Buscar en nombre y descripción
- `ordering`: Ordenar por campos (`name`, `created_at`, `type`)

**Respuesta de ejemplo:**
```json
[
  {
    "id": 1,
    "name": "Salario",
    "type": "INCOME",
    "description": null,
    "user": 1,
    "is_active": true,
    "created_at": "2026-03-11T11:12:14.795977Z",
    "updated_at": "2026-03-11T11:12:14.795993Z",
    "transactions_count": 1
  }
]
```

### 2. Crear Categoría
**POST** `/api/categories/`

Crea una nueva categoría.

**Body:**
```json
{
  "name": "Alimentación",
  "type": "EXPENSE",
  "description": "Gastos en comida",
  "user": 1,
  "is_active": true
}
```

**Validaciones:**
- `name`: Mínimo 3 caracteres
- `type`: Debe ser `INCOME` o `EXPENSE`
- No se permiten categorías duplicadas (mismo nombre y tipo para el usuario)

### 3. Obtener Detalle de Categoría
**GET** `/api/categories/{id}/`

Obtiene los detalles de una categoría específica.

### 4. Actualizar Categoría
**PUT/PATCH** `/api/categories/{id}/`

Actualiza una categoría existente.

### 5. Eliminar Categoría
**DELETE** `/api/categories/{id}/`

Elimina una categoría. **Nota:** No se puede eliminar una categoría con transacciones asociadas (protección PROTECT).

### 6. Categorías por Tipo
**GET** `/api/categories/by_type/`

Endpoint especial que agrupa las categorías por tipo.

**Parámetros opcionales:**
- `type`: Si se especifica, retorna solo ese tipo

**Respuesta sin parámetros:**
```json
{
  "income": [
    {
      "id": 1,
      "name": "Salario",
      "type": "INCOME",
      ...
    }
  ],
  "expense": [
    {
      "id": 2,
      "name": "Alimentación",
      "type": "EXPENSE",
      ...
    }
  ]
}
```

---

## Endpoints de Transacciones

### 1. Listar Transacciones
**GET** `/api/transactions/`

Lista todas las transacciones del usuario, ordenadas por fecha descendente.

**Parámetros de query opcionales:**
- `type`: Filtrar por tipo (`INCOME` o `EXPENSE`)
- `category`: Filtrar por ID de categoría
- `transaction_date`: Filtrar por fecha
- `search`: Buscar en descripción y notas
- `ordering`: Ordenar por campos (`transaction_date`, `amount`, `created_at`)

**Respuesta de ejemplo:**
```json
[
  {
    "id": 2,
    "type": "EXPENSE",
    "amount": "450.50",
    "description": "Compra de supermercado",
    "category_name": "Alimentación",
    "transaction_date": "2026-03-11T11:12:14.801811Z"
  }
]
```

### 2. Crear Transacción
**POST** `/api/transactions/`

Crea una nueva transacción (movimiento manual).

**Body:**
```json
{
  "user": 1,
  "category": 2,
  "type": "EXPENSE",
  "amount": "125.75",
  "description": "Comida a domicilio",
  "notes": "Pedido de pizza",
  "transaction_date": "2026-03-11T10:00:00Z"
}
```

**Validaciones:**
- `amount`: Debe ser mayor que 0
- `description`: Mínimo 3 caracteres
- `type`: Debe coincidir con el tipo de la categoría
- `transaction_date`: No puede ser fecha futura
- `category`: Debe pertenecer al usuario y estar activa

**Respuesta de ejemplo:**
```json
{
  "id": 3,
  "user": 1,
  "category": 2,
  "category_name": "Alimentación",
  "category_type": "EXPENSE",
  "type": "EXPENSE",
  "amount": "125.75",
  "description": "Comida a domicilio",
  "notes": "Pedido de pizza",
  "transaction_date": "2026-03-11T10:00:00Z",
  "created_at": "2026-03-11T11:12:31.775132Z",
  "updated_at": "2026-03-11T11:12:31.775146Z"
}
```

### 3. Obtener Detalle de Transacción
**GET** `/api/transactions/{id}/`

Obtiene los detalles completos de una transacción.

### 4. Actualizar Transacción
**PUT/PATCH** `/api/transactions/{id}/`

Actualiza una transacción existente.

### 5. Eliminar Transacción
**DELETE** `/api/transactions/{id}/`

Elimina una transacción.

### 6. Resumen Financiero
**GET** `/api/transactions/summary/`

Genera un resumen financiero con totales y balance.

**Parámetros opcionales:**
- `period`: Período predefinido (`week`, `month`, `year`). Por defecto: `month`
- `start_date`: Fecha inicial (formato: YYYY-MM-DD)
- `end_date`: Fecha final (formato: YYYY-MM-DD)

**Respuesta de ejemplo:**
```json
{
  "period": "month",
  "total_income": 3000.0,
  "total_expenses": 576.25,
  "balance": 2423.75,
  "transaction_count": 3,
  "income_count": 1,
  "expense_count": 2
}
```

### 7. Transacciones por Categoría
**GET** `/api/transactions/by_category/`

Agrupa las transacciones por categoría con totales.

**Parámetros opcionales:**
- `type`: Filtrar por tipo (`INCOME` o `EXPENSE`)
- `start_date`: Fecha inicial (formato: YYYY-MM-DD)
- `end_date`: Fecha final (formato: YYYY-MM-DD)

**Respuesta de ejemplo:**
```json
[
  {
    "category__id": 1,
    "category__name": "Salario",
    "category__type": "INCOME",
    "total_amount": 3000.0,
    "transaction_count": 1
  },
  {
    "category__id": 2,
    "category__name": "Alimentación",
    "category__type": "EXPENSE",
    "total_amount": 576.25,
    "transaction_count": 2
  }
]
```

---

## Códigos de Estado HTTP

- `200 OK`: Solicitud exitosa (GET, PUT, PATCH)
- `201 Created`: Recurso creado exitosamente (POST)
- `204 No Content`: Recurso eliminado exitosamente (DELETE)
- `400 Bad Request`: Error de validación
- `404 Not Found`: Recurso no encontrado
- `500 Internal Server Error`: Error del servidor

---

## Modelos de Datos

### Category
```python
{
  "id": int,
  "name": string (max 100 chars),
  "type": "INCOME" | "EXPENSE",
  "description": string (opcional),
  "user": int (foreign key),
  "is_active": boolean,
  "created_at": datetime,
  "updated_at": datetime,
  "transactions_count": int (calculado)
}
```

### Transaction
```python
{
  "id": int,
  "user": int (foreign key),
  "category": int (foreign key),
  "type": "INCOME" | "EXPENSE",
  "amount": decimal (max 12 dígitos, 2 decimales),
  "description": string (max 255 chars),
  "notes": string (opcional),
  "transaction_date": datetime,
  "created_at": datetime,
  "updated_at": datetime,
  "category_name": string (solo lectura),
  "category_type": string (solo lectura)
}
```

---

## Características Implementadas

### Seguridad y Validaciones
- ✅ Validación de tipos de categoría y transacción
- ✅ Validación de montos positivos
- ✅ Validación de fechas (no futuras)
- ✅ Protección contra eliminación de categorías con transacciones
- ✅ Validación de categorías duplicadas
- ✅ Validación de pertenencia de categoría al usuario

### Funcionalidad de Negocio
- ✅ CRUD completo para categorías
- ✅ CRUD completo para transacciones
- ✅ Cálculo de balance (ingresos - gastos)
- ✅ Resúmenes financieros por período
- ✅ Agrupación por categoría
- ✅ Filtrado y búsqueda
- ✅ Ordenamiento personalizado

### Optimizaciones
- ✅ Uso de `select_related` para evitar N+1 queries
- ✅ Índices de base de datos para búsquedas rápidas
- ✅ Serializers optimizados para listados

---

## Ejemplos de Uso con cURL

### Crear una categoría de gasto
```bash
curl -X POST http://localhost:8000/api/categories/ \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Transporte",
    "type": "EXPENSE",
    "description": "Gastos de transporte",
    "user": 1,
    "is_active": true
  }'
```

### Crear una transacción
```bash
curl -X POST http://localhost:8000/api/transactions/ \
  -H "Content-Type: application/json" \
  -d '{
    "user": 1,
    "category": 2,
    "type": "EXPENSE",
    "amount": "50.00",
    "description": "Taxi al aeropuerto",
    "transaction_date": "2026-03-11T14:30:00Z"
  }'
```

### Obtener resumen financiero del último mes
```bash
curl http://localhost:8000/api/transactions/summary/?period=month
```

### Obtener transacciones de gastos
```bash
curl http://localhost:8000/api/transactions/?type=EXPENSE
```

---

## Próximos Pasos

Este Motor de Transacciones sirve como base para:
1. **Hoja de Cálculo**: Vista detallada de todas las transacciones
2. **Dashboard**: Visualizaciones y gráficos de datos financieros
3. **Presupuestos**: Límites de gasto por categoría
4. **Reportes**: Generación de reportes financieros

---

## Notas de Implementación

- **Base de Datos**: SQLite para desarrollo, PostgreSQL (Supabase) para producción
- **Autenticación**: Preparado para JWT (SimpleJWT) - pendiente de implementación
- **Tests**: 32 tests unitarios y de integración con 100% de cobertura de endpoints
- **Framework**: Django 6.0.3 + Django REST Framework 3.16.1
