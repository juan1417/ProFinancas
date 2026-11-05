"""Reglas puras de una transacción.

Ninguna función de este módulo toca la base de datos: reciben objetos con los
atributos que necesitan (`category.type`, `category.user_id`, ...), de modo que
pueden probarse con `SimpleNamespace` y sin Django.
"""
from datetime import datetime
from decimal import Decimal, InvalidOperation

from .errors import DomainError

TYPE_INCOME = 'INCOME'
TYPE_EXPENSE = 'EXPENSE'
TRANSACTION_TYPES = (TYPE_INCOME, TYPE_EXPENSE)

_TYPE_LABELS = {
    TYPE_INCOME: 'Ingreso',
    TYPE_EXPENSE: 'Gasto',
}


def validate_amount(value) -> Decimal:
    """El monto debe ser positivo y convertible a Decimal de 2 decimales."""
    try:
        amount = Decimal(str(value))
    except (InvalidOperation, TypeError, ValueError):
        raise DomainError('El monto debe ser un número válido', field='amount')
    if amount <= 0:
        raise DomainError('El monto debe ser mayor que cero', field='amount')
    return amount


def validate_description(value: str) -> str:
    """La descripción se guarda sin espacios externos y con 3 caracteres mínimo."""
    text = (value or '').strip()
    if len(text) < 3:
        raise DomainError(
            'La descripción debe tener al menos 3 caracteres',
            field='description',
        )
    return text


def validate_transaction_date(value: datetime, now: datetime) -> datetime:
    """La fecha no puede ser futura.

    Un datetime naive se interpreta en la zona de `now` para poder compararlos.
    """
    if value.tzinfo is None:
        value = value.replace(tzinfo=now.tzinfo)
    if value > now:
        raise DomainError(
            'La fecha de la transacción no puede ser futura',
            field='transaction_date',
        )
    return value


def validate_category_assignment(category, transaction_type: str | None, user) -> None:
    """La categoría debe ser del usuario, estar activa y coincidir en tipo.

    `category` y `user` son objetos ya resueltos (no querysets); el módulo solo
    lee `type`, `is_active`, `user_id` e `id`.
    """
    if category is None:
        return

    category_owner_id = getattr(category, 'user_id', None)
    user_id = getattr(user, 'id', None)
    if user_id is None:
        user_id = getattr(user, 'pk', None)

    if category_owner_id is not None and user_id is not None:
        if category_owner_id != user_id:
            raise DomainError(
                'La categoría debe pertenecer al usuario de la transacción',
                field='category',
            )

    if not getattr(category, 'is_active', True):
        raise DomainError(
            'No se puede crear una transacción con una categoría inactiva',
            field='category',
        )

    if transaction_type and getattr(category, 'type', None) != transaction_type:
        label = _TYPE_LABELS.get(category.type, category.type)
        raise DomainError(
            'El tipo de transacción debe coincidir con el tipo de categoría '
            f'({label})',
            field='type',
        )
