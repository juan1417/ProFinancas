# 🏦 Proyecto Profiancas - Master Plan Backend

Este documento define la arquitectura, el stack tecnológico y los estándares de desarrollo para el núcleo del sistema financiero Profiancas.

# 🚀 Stack Tecnológico Seleccionado

| Componente | Tecnología | Razón de elección |
|------------|------------|-------------------|
| Lenguaje   | Python 3.12+ | Robustez y ecosistema financiero. |
| Framework  | Django 5.x | Seguridad por defecto y administración integrada. |
| API Engine | Django REST Framework (DRF) | Estándar para APIs potentes y serialización. |
| Gestor de Paquetes | uv | Velocidad extrema (Rust) y control de versiones. |
| Base de Datos | PostgreSQL (Supabase) | Motor relacional de alta fidelidad |
| Auth | JWT (SimpleJWT) | Autenticación stateless ideal para Flutter. |
| Hashing | Bcrypt / Argon2 | Máxima protección contra ataques de fuerza bruta. |


# 🏗️ Arquitectura: Modular + Hexagonal (Ports & Adapters)

El sistema se construye separando la lógica de negocio de las implementaciones técnicas para garantizar que la "Hoja de Cálculo" y el "Motor de Transacciones" no dependan de herramientas externas.

## Capas del Sistema:

1. **Dominio (Core)**: Modelos de Django y lógica de validación financiera.

2. **Aplicación (Casos de Uso)**: Serializers de DRF que orquestan los datos.

3. **Infraestructura (Adaptadores)**: Conexión a Supabase, integración de OCR, y adaptadores de notificaciones.

4. **Entrada**: API REST consumida por Flutter.

5. **Salida**: Base de datos en Supabase, Almacenamiento S3 para facturas, y APIs de OCR.

# 📂 Organización de Aplicaciones (Apps)

El proyecto se divide en módulos independientes para facilitar el mantenimiento y los tests:

- `users`: Gestión de perfiles con AbstractUser, UUID como PK y hashing Bcrypt.

- `wallets`: Registro de métodos de pago (Tarjetas/Cuentas) y saldos.

- `transactions`: El cerebro del sistema. Maneja ingresos, gastos y la lógica de la hoja de cálculo.

- `invoices`: Módulo de procesamiento de imágenes con OCR para facturas físicas.

- `notifications`: Disparadores de alertas de gasto basados en límites configurados.

# 🔐 Estrategia de Persistencia (Supabase)

La conexión se realiza mediante variables de entorno para cumplir con los estándares de seguridad.

- **Puerto de Conexión**: 6543 (Transaction Pooler) para evitar el agotamiento de hilos de PostgreSQL.

- **SSL**: Obligatorio (sslmode=require).

- **Entorno de Tests**: Durante la ejecución de pytest o manage.py test, se utilizará SQLite en memoria para garantizar que el ciclo de TDD sea instantáneo y no dependa de latencia de red.

# 🧪 Metodología de Desarrollo: TDD

El flujo de trabajo sigue estrictamente el ciclo Red-Green-Refactor:

1. **Red**: Escribir una prueba unitaria (ej. "Validar que un gasto no exceda el presupuesto") que falle inicialmente.

2. **Green**: Escribir el código mínimo necesario en Django para que la prueba pase.

3. **Refactor**: Limpiar el código, optimizar consultas (evitar el problema de N+1 con select_related) y asegurar legibilidad.

# ⚙️ Configuración del Entorno (Guía Rápida)

Para levantar el backend siguiendo este plan:
```

# 1. Instalar dependencias base
uv add django djangorestframework psycopg2-binary dj-database-url python-dotenv bcrypt argon2-cffi djangorestframework-simplejwt

# 2. Iniciar Apps base
python manage.py startapp users
python manage.py startapp transactions

# 3. Ejecutar Migraciones iniciales
python manage.py makemigrations
python manage.py migrate"

```

# 📊 Roadmap de Prioridades

1. **Prioridad 1 (Crítica)**: Autenticación segura y modelo de usuario con UUID.

2. **Prioridad 2 (Core)**: CRUD de transacciones y filtros para la vista de hoja de cálculo.

3. **Prioridad 3 (Feature)**: Integración de adaptador OCR para captura de facturas.

4. **Prioridad 4 (UX)**: Sistema de notificaciones y límites de presupuesto.