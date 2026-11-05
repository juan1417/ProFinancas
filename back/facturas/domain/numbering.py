"""Numeración de facturas: reglas puras, sin DB."""


def format_invoice_number(sequence: int) -> str:
    if sequence < 1:
        raise ValueError('sequence must be positive')
    return f'FAC-{sequence:03d}'


def parse_invoice_number(value: str | None) -> int | None:
    if not value or not value.startswith('FAC-'):
        return None
    try:
        return int(value.split('-', 1)[1])
    except (IndexError, ValueError):
        return None


def next_sequence(last_number: str | None, count: int) -> int:
    """Devuelve el número siguiente.

    Si hay una factura previa con número `FAC-NNN`, continúa desde ahí; si no,
    parte del conteo actual + 1 (comportamiento histórico).
    """
    parsed = parse_invoice_number(last_number)
    if parsed is not None:
        return parsed + 1
    return count + 1
