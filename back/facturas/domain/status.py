"""Estado derivado de una factura: OVERDUE se calcula al leer, no se persiste."""
from datetime import date


def effective_status(stored_status: str, due_date, today: date) -> str:
    if stored_status == 'PENDING' and due_date is not None and due_date < today:
        return 'OVERDUE'
    return stored_status
