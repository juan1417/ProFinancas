"""Normalización pura de fechas de rango.

El rango es inclusivo: un movimiento a las 23:59 del `end_date` debe contar.
Por eso un extremo superior se expande al final del día, no a su medianoche.
"""
from datetime import date, datetime, time

from .errors import DomainError


def parse_day(value) -> date:
    """Acepta `date`, `datetime` o `'YYYY-MM-DD'` y devuelve la fecha."""
    if isinstance(value, datetime):
        return value.date()
    if isinstance(value, date):
        return value
    if isinstance(value, str):
        try:
            return date.fromisoformat(value)
        except ValueError:
            raise DomainError(
                f'Fecha inválida: {value!r}. Formato esperado: YYYY-MM-DD.',
                field='date',
            )
    raise DomainError(
        f'Tipo de fecha no soportado: {type(value).__name__}.',
        field='date',
    )


def day_bounds(value, *, end_of_day: bool) -> datetime:
    """Devuelve el inicio o el fin del día indicado, sin zona horaria."""
    moment = time.max if end_of_day else time.min
    return datetime.combine(parse_day(value), moment)
