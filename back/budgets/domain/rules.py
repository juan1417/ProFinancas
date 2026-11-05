"""Reglas puras de presupuestos."""
from .errors import DomainError


def validate_budget_category(category, user) -> None:
    """La categoría de un presupuesto debe ser EXPENSE y del usuario."""
    if category is None:
        return
    if getattr(category, 'type', None) != 'EXPENSE':
        raise DomainError(
            'La categoría de un presupuesto debe ser de tipo EXPENSE.',
            field='category',
        )
    category_owner_id = getattr(category, 'user_id', None)
    user_id = getattr(user, 'id', None)
    if user_id is None:
        user_id = getattr(user, 'pk', None)
    if category_owner_id is not None and user_id is not None:
        if category_owner_id != user_id:
            raise DomainError(
                'La categoría debe pertenecer al usuario del presupuesto.',
                field='category',
            )
