"""Conversión de bordes de rango a datetimes aware (capa con Django)."""
from datetime import datetime

from django.utils import timezone

from ..domain.dates import day_bounds


def aware_bound(value, *, end_of_day: bool) -> datetime:
    """Normaliza un borde a un datetime aware inclusivo.

    Un `datetime` ya aware se devuelve tal cual (la hora del caller se respeta).
    Una fecha cruda se expande al inicio (00:00) o al final (23:59:59.999999)
    del día, en la zona horaria activa.
    """
    if isinstance(value, datetime):
        return value if timezone.is_aware(value) else timezone.make_aware(value)
    return timezone.make_aware(day_bounds(value, end_of_day=end_of_day))
