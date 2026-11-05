"""Creación transaccional de facturas con numeración atómica."""
from django.db import transaction, IntegrityError

from ..domain import numbering as numbering_rules
from ..models import Invoice


def next_invoice_number(user) -> str:
    last = Invoice.objects.filter(user=user).order_by('-created_at').first()
    count = Invoice.objects.filter(user=user).count()
    sequence = numbering_rules.next_sequence(
        last.invoice_number if last else None, count,
    )
    return numbering_rules.format_invoice_number(sequence)


def create_invoice(*, user, **fields) -> Invoice:
    """Crea la factura asignando el número en una transacción.

    Reintenta una vez si otra creación simultánea tomó el mismo número (la
    constraint única fuerza un IntegrityError y se vuelve a calcular).
    """
    for attempt in range(2):
        try:
            with transaction.atomic():
                return Invoice.objects.create(
                    user=user,
                    invoice_number=next_invoice_number(user),
                    **fields,
                )
        except IntegrityError:
            if attempt == 1:
                raise
