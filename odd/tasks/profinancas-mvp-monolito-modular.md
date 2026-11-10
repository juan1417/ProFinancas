# ProFinancas — MVP operativo + monolito modular con puertos

Objetivo: que el MVP funcione de punta a punta (guardar, leer y generar facturas y movimientos, con
ingesta bancaria) y que el backend tenga reglas de dominio en `domain/` + `application/` en vez de
repartidas entre serializers, views y modelos.
Rama: `feature/mvp-monolito-modular`. Estrategia de entrega: `ask-on-risk` (default).
Test runner backend: `cd back && SECRET_KEY=dev-secret DEBUG=True uv run pytest`.
Plan aprobado: `/home/juan/.opencode/plan/profinancas-mvp-monolito-modular.md`.
Restricción vigente del usuario: sin `git commit`/`git push` sin confirmación expresa.

## Specs

- **S1** — Los clientes compilan en esta máquina: `android: ./gradlew :app:compileDebugKotlin` y
  `desktop: ./gradlew compileKotlin`, con JDK 17 (`/usr/lib/jvm/java-17-openjdk`).
- **S2** — Las migraciones de `budgets` y `facturas` existen y están versionadas; `migrations/` ya no
  se ignora en `.gitignore`.
- **S3** — Una base de datos nueva migra sin pasos manuales:
  `DJANGO_SETTINGS_MODULE=ProFinanzas.settings_dev uv run python manage.py migrate` aplica
  `usuarios.0001`, `transactions.0001`, `budgets.0001`, `facturas.0001`.
- **S4** — `PATCH /api/transactions/{id}/` no puede dejar `type` desincronizado con
  `category.type`: si se cambia solo la categoría, la regla de coincidencia debe evaluarse contra el
  `type` guardado. Respuesta esperada: `400`.
- **S5** — `start_date`/`end_date` incluyen el día completo en `GET /api/transactions/summary/` y
  `GET /api/transactions/by_category/`: un movimiento a las 23:59 del `end_date` cuenta.
- **S6** — `POST /api/budgets/` rechaza (`400`) una categoría que pertenece a otro usuario.
- **S7** — `POST /api/invoices/` devuelve el recurso creado **con `id`**.
- **S8** — `GET /api/transactions/by_category/?start_date=not-a-date` responde `400`, no `500`.
- **S9** — Desde la UI se puede: registrar usuario, crear categoría, crear movimiento con fecha
  pasada, crear factura, y ver el resumen actualizado. Errores de red/validación visibles.
- **S10** — `GET /api/invoices/{id}/document/` devuelve un PDF real; la ingesta bancaria por archivo
  tiene vista previa, confirmación y reimportación sin duplicados (supuesto: CSV, sin API bancaria).
- **S11** — Sin valores de gráfico literales, sin colores hex fuera de tokens, tema claro conmutable,
  estados `Loading/Empty/Error` consistentes.

## Tasks

| ID | Spec | Ruta | Estado | Evidencia |
| --- | --- | --- | --- | --- |
| T1 | S1 | inline | hecho | JDK 17.0.20.1 en `/usr/lib/jvm/java-17-openjdk`; `JAVA_HOME` añadido a `~/.config/fish/config.fish`; `android/local.properties` con `sdk.dir`; `compileDebugKotlin` y `compileKotlin` = BUILD SUCCESSFUL |
| T2 | S2 | inline | hecho | `budgets/migrations/0001_initial.py`, `facturas/migrations/0001_initial.py`; `.gitignore` sin `migrations/` |
| T3 | S3 | inline | hecho | `migrate` desde cero aplica `budgets.0001` y `facturas.0001`; `runserver` responde 401 sin auth (esperado) |
| T4 | S4-S8 | inline | hecho | RED observado: `6 failed, 85 passed` — los 6 fallos son exactamente S4-S8 (los 2 tests de "sigue funcionando" ya pasaban) |
| T5 | S4-S8 | inline | hecho | GREEN: `91 passed` (83 originales + 8 nuevos). Cambios: `transactions/serializers.py:124`, `transactions/services.py` (`_coerce_bound`), `budgets/serializers.py:33`, `facturas/views.py:33` |
| T6 | S9 | inline | hecho | Android: FAB real + `AddTransactionSheet`, `Categorías` en bottom nav, fecha editable, `ErrorBanner` compartido en dashboard/categorías/facturas/analytics; Desktop: `password_confirm`, diálogo "Nueva factura", fecha editable, `ErrorBanner` en dashboard/categorías, refresh 401. Ambos: `TokenRefreshAuthenticator` OkHttp y persistencia del refresh rotado. Builds OK |
| T7 | S9 | inline | hecho | `transactions/domain/{rules,dates,errors}.py` + `application/{dates,reports}.py`; `facturas/domain/{numbering,status}.py` + `application/invoice_service.py` (numeración transaccional + UniqueConstraint + OVERDUE derivado); `budgets/domain/{rules,errors}.py` + `application/queries.py` (is_active escribible); `usuarios/services.get_client_ip`. Eliminados `transactions/services.py` y `budgets/services.py`. 107 tests verdes, migrate desde cero OK |
| T8 | S10 | inline | hecho | `facturas/ports.py` + `adapters/pdf.py` (reportlab) + endpoint `/api/invoices/{id}/document/` (FileResponse PDF); nueva app `ingestion/` con `BankStatementImporter` CSV (preview/confirm + dedup por hash de fila). 110 tests verdes |
| T9 | S11 | inline | parcial | `LightColorScheme` en Android y desktop; `ProFinanzasTheme(darkTheme=...)`; moneda por defecto COP; gráfico Android con datos reales (7 días). Pendiente: generador `design/tokens.json`, cableado del toggle al estado global, color de categoría por hash, estados Loading/Empty/Error compartidos |

## Log

- **L1** (2026-11-05, usuario, verbatim): "puedes empezar con el plan"
- **L2** (2026-11-05, usuario, verbatim): "quiero hacer un cambio arquitectonico y crear un plan para
  que se cumpla el MVP del software. para la arquitectura quiero cambiar el mvc del back para
  suplantarlo por una arquietctura mas solida como hexagonal o intentar crear un microservicios con
  podman y mejorar el sistema de diseño que hay en la aplicacion"
- **L3** (2026-11-05, usuario, verbatim, definición del MVP): "guardar leer y generar facturas y/o
  mavimientos de un sistema bancario para hacer monitoreo de gastos"
- **L4** (2026-11-05): decisión aprobada — monolito modular con puertos; microservicios y Podman
  descartados. Reglas: `domain/` Python puro; `application/` sin DRF; `models.py`/`views.py`/
  `serializers.py`/`urls.py` no se mueven.
- **L5** (2026-11-05): JDK 17 ya existía en `/usr/lib/jvm/java-17-openjdk` (17.0.20.1); no hizo falta
  instalar nada. `JAVA_HOME` global sigue sin fijar en fish; por ahora se pasa por entorno.
- **L6** (2026-11-05): hallazgo — `facturas` y `budgets` no tenían ni directorio `migrations/`.
  `makemigrations` los creó y el traceback de `black` (`/opt/miniconda3/bin/black`) es cosmético.
- **L7** (2026-11-05): `by_category` ya captura `ValueError`, pero una fecha malformada lanza
  `django.core.exceptions.ValidationError` al filtrar → 500. El test fija el 400.
- **L8** (2026-11-05): `get_summary` con un solo `start_date` o `end_date` ignora el parámetro y cae
  en la ventana por `period`. Fuera del alcance de F0; se decide en T5/T7 si se corrige.
- **L9** (2026-11-05): RED de F0.4 observado antes de tocar producción: `6 failed, 85 passed`.
  Fallos: PATCH categoría sin `type` (200 != 400), `summary` y `by_category` con `end_date` exclusivo
  (0 != 1), fecha malformada en `by_category` (500), presupuesto con categoría ajena (201), factura
  creada sin `id`.
- **L10** (2026-11-05): GREEN tras el fix mínimo: `91 passed`. Se añadió `_coerce_bound()` en
  `transactions/services.py`: convierte fecha a inicio/fin de día aware (`time.min`/`time.max`), así
  el rango es inclusivo y una fecha malformada levanta `ValueError` (que la vista ya traduce a 400).
- **L11** (2026-11-05): `AGENTS.md` actualizado: baseline 91 tests, migraciones versionadas en las
  cuatro apps, y ruta real del JDK 17 con los comandos de build verificados.
- **L12** (2026-11-05): F1.4 — Desktop `FacturasScreen` gana botón "Nueva factura" y
  `CreateInvoiceDialog`; llama a `FacturasViewModel.createInvoice` (antes sin call sites). Se añade
  `clearError()` y banner de error. Verificado con `compileKotlin`.
- **L13** (2026-11-05): F1.5 — fecha editable en el diálogo de movimiento de Android y desktop.
  `createTransaction` acepta `date` (YYYY-MM-DD): si es hoy usa la hora actual (el backend rechaza
  fechas futuras), si es otro día usa mediodía local. En Android se renombró el parámetro del caso
  de uso a `createTransactionUseCase` porque el nombre nuevo del parámetro de la función creaba
  ambigüedad y riesgo de recursión.
- **L14** (2026-11-05): F1.5 — refresh de token: se añadió `TokenRefreshAuthenticator` (OkHttp
  `Authenticator`) en Android (Hilt) y desktop (manual DI). El refresh usa un cliente propio sin el
  interceptor para no recursar, reintenta una sola vez y guarda el refresh rotado
  (`ROTATE_REFRESH_TOKENS=True`). `TokenRefreshResponse` gana `refresh` y `AuthRepositoryImpl` lo
  persiste en ambos clientes (antes se perdía y el siguiente refresh fallaba).
- **L15** (2026-11-05): F1.5 — errores visibles: `ErrorBanner` compartido en Android y desktop;
  conectado a dashboard, categorías, facturas y analytics (transacciones ya lo tenía). En
  `AnalyticsViewModel` el fallo de `getTransactions` se tragaba y dejaba el spinner infinito; ahora
  se reporta.
- **L16** (2026-11-05): gotcha kapt — `NetworkModule` necesitó `import
  com.profinanzas.core.api.TokenRefreshAuthenticator`; sin import explícito el stub generó
  `error/NonExistentClass` y Hilt falló con "could not be resolved" aunque la clase compilara.
- **L17** (2026-11-05): F1 cerrada — `desktop` tenía el mismo gotcha que Android: `AppModule.kt`
  necesitó `import com.profinanzas.core.api.TokenRefreshAuthenticator`; sin él, el compilador
  reportaba "Unresolved reference" y errores de inferencia en cascada. Tras añadirlo:
  `compileKotlin` y `:app:compileDebugKotlin` = BUILD SUCCESSFUL.
- **L18** (2026-11-05): F2 cerrada. Estructura modular: `domain/` Python puro
  (transacciones reglas + fechas, facturas numeración + estado, presupuestos reglas) y
  `application/` con ORM (reportes, creación transaccional de factura, spent). Numeración
  de factura movida fuera del modelo (`facturas/domain/numbering.py` + `application/invoice_service.py`)
  con `UniqueConstraint(user, invoice_number)` y reintento ante IntegrityError. OVERDUE se deriva
  al leer (`facturas/domain/status.effective_status`) con filtro consistente. `is_active` de
  presupuesto ahora escribible. `_get_client_ip` unificado en `usuarios/services.py`. Eliminados
  `transactions/services.py` y `budgets/services.py`. 15 tests puros nuevos (test_domain.py por app).
  Suite: 107 passed.
- **L19** (2026-11-05): F3 cerrada. `facturas/ports.DocumentGenerator` con adaptador
  reportlab; endpoint de descarga PDF. Nueva app `ingestion` con `BankStatementImporter` CSV:
  preview → confirm, dedup por hash de fila (`ImportedRow`), movimientos entran por el ORM de
  `transactions`. Tests de preview/confirm/dedup añadidos.
- **L20** (2026-11-05): F4 parcial. `LightColorScheme` añadido en ambos clientes y
  `ProFinanzasTheme` acepta `darkTheme`; moneda por defecto COP; gráfico de 7 días del dashboard
  Android ahora se alimenta de las transacciones descargadas. Pendiente: fuente única de tokens con
  generador, cableado del toggle de tema al estado global (hoy `remember` local), color de categoría
  por hash del id y estados Loading/Empty/Error compartidos.
- **L21** (2026-11-10): Fase B (sistema de tema adaptativo) en Android. `Palette.kt` nuevo:
  `AppPalette` (Dark/Light) + `LocalAppPalette` + `AppTheme` (ThemeMode SYSTEM/LIGHT/DARK persistido
  en SharedPreferences) + `isDarkTheme()`. Los 11 tokens neutros (`Bg/Surface/Fg/Muted/Border/
  AccentSoft/FgSoft/SurfaceHover/Neutral*/SurfaceLight`) pasaron a getters `@Composable` -> los ~304
  call sites NO se tocaron. Marca (`Accent/Danger/Cat*/Chart*/NavyBlue`) queda estática. `Theme.kt`
  reescrito para proporcionar la paleta y elegir colorScheme efectivo. `MainActivity.init(AppTheme)`,
  switch de `PreferencesSheet` ahora persiste y reconfigura en vivo. Se quitó `color = Muted` del
  `bodySmall` top-level (Type.kt) porque ya no es composable. Verificado con screenshots del emulador
  en claro y oscuro (legibles ambos) y connectedAndroidTest 2/2. Desktop sin cambios (modo oscuro fijo,
  sin toggle, sin bug de contraste actual).
- **L22** (2026-11-10): Fase B desktop migrada (Palette+AppTheme prefs, Color solo marca, Theme
  provee paleta, Canvas hoist Border, Type sin color). QA visual en emulador (claro) destapó 2 bugs
  que el compilador no veía: (1) FAB con "+" blanco sobre verde y botones `contentColor=Bg` casi
  blancos sobre verde en modo claro, porque `onPrimary`/`Bg` del palette claro eran el bg (casi
  blanco). Fix: `onPrimary`/`onPrimaryContainer` = oscuro fijo en ambos temas; botones marca ->
  `buttonColors()` del tema. Re-captura: FAB oscuro legible. Desktop paridad aplicada.
