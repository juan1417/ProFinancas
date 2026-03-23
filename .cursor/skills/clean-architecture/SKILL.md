---
name: clean-architecture
description: Apply clean architecture conventions to the ProFinancas project. Use when scaffolding a new feature, adding a new Django app, creating a Flutter screen or service, or when the user asks about project structure, layers, or how to organize code.
---

# Clean Architecture — ProFinancas

Two stacks, one principle: keep business logic independent of frameworks and delivery mechanisms.

- For detailed layer examples → [flutter-layers.md](flutter-layers.md)
- For Django service layer examples → [django-layers.md](django-layers.md)

---

## Flutter — Feature-First Structure

Every domain concept is a self-contained feature folder with three layers.

```
lib/
  core/
    api/            # ApiClient, ApiConstants (base URL, headers)
    errors/         # AppException, Failure classes
    utils/          # CurrencyFormatter, DateFormatter
  features/
    auth/
      data/
        datasources/    # AuthRemoteDatasource (raw HTTP)
        models/         # UserModel (fromJson / toJson)
        repositories/   # AuthRepositoryImpl
      domain/
        entities/       # User (pure Dart, no json)
        repositories/   # AuthRepository (abstract)
        usecases/       # LoginUseCase, RegisterUseCase
      presentation/
        screens/        # LoginScreen, RegisterScreen
        widgets/        # LoginForm, AuthButton
        providers/      # AuthProvider (ChangeNotifier)
    transactions/
      data/ / domain/ / presentation/   # same pattern
    categories/
      data/ / domain/ / presentation/
```

### Layer Rules

| Layer | Allowed imports | Forbidden imports |
|---|---|---|
| `domain` | nothing (pure Dart) | `data`, `presentation`, Flutter SDK |
| `data` | `domain` entities + repositories | `presentation` |
| `presentation` | `domain` usecases + entities | `data` directly |

```dart
// ✅ GOOD — presentation calls usecase
class TransactionProvider extends ChangeNotifier {
  final GetTransactionsUseCase _getTransactions;
  // ...
}

// ❌ BAD — presentation calls repository/datasource directly
class TransactionScreen extends StatelessWidget {
  final TransactionRepository _repo; // wrong layer
}
```

### Naming Conventions

- Entity: `Transaction` (domain/entities/transaction.dart)
- Model: `TransactionModel extends Transaction` (data/models/transaction_model.dart)
- Repository interface: `TransactionRepository` (domain/repositories/)
- Repository impl: `TransactionRepositoryImpl` (data/repositories/)
- UseCase: `GetTransactionsUseCase`, `CreateTransactionUseCase` (one class, one `call()` method)
- Provider: `TransactionProvider` (presentation/providers/)
- Screen: `TransactionsScreen` (presentation/screens/)

---

## Django — Service Layer

ViewSets handle HTTP only. Business logic lives in `services.py`.

```
back/
  <app>/
    models.py         # ORM models only — no business logic
    serializers.py    # Validation + serialization only
    services.py       # ← All business logic goes here
    views.py          # Orchestration: deserialize → call service → return response
    urls.py
    tests/
      test_services.py
      test_api.py
```

### Service Layer Rules

```python
# ✅ GOOD — view delegates to service
class TransactionViewSet(viewsets.ModelViewSet):
    def perform_create(self, serializer):
        TransactionService.create(serializer.validated_data, user=self.request.user)

# services.py
class TransactionService:
    @staticmethod
    def create(data: dict, user) -> Transaction:
        # Business logic: validate category ownership, check limits, etc.
        ...

# ❌ BAD — business logic inside a view
def perform_create(self, serializer):
    category = serializer.validated_data['category']
    if category.user != self.request.user:   # logic belongs in service
        raise ValidationError(...)
    serializer.save(user=self.request.user)
```

### Decision Guide

| Where does it belong? | Location |
|---|---|
| HTTP parsing, status codes | `views.py` |
| Field validation, type coercion | `serializers.py` |
| Business rules, cross-model operations | `services.py` |
| DB schema, relationships | `models.py` |
| Complex query logic (optional) | `repositories.py` |

---

## Adding a New Feature — Checklist

### Flutter feature
- [ ] Create `lib/features/<name>/data/`, `domain/`, `presentation/`
- [ ] Define entity in `domain/entities/`
- [ ] Define abstract repository in `domain/repositories/`
- [ ] Create use cases in `domain/usecases/` (one class per use case)
- [ ] Implement model with `fromJson`/`toJson` in `data/models/`
- [ ] Implement repository in `data/repositories/`
- [ ] Create provider in `presentation/providers/`
- [ ] Wire datasource → repository impl → provider in dependency injection

### Django app
- [ ] `python manage.py startapp <name>`
- [ ] Add to `INSTALLED_APPS`
- [ ] Create `services.py` alongside `models.py`
- [ ] Keep views thin — one service call per action
- [ ] Register router in `back/ProFinanzas/urls.py`
- [ ] Add tests in `tests/test_services.py` and `tests/test_api.py`
