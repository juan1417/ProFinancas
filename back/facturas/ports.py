"""Puertos (interfaces hacia afuera) de la app de facturas."""
from typing import Protocol, Tuple


class DocumentGenerator(Protocol):
    """Genera un documento imprimible para una factura.

    Devuelve ``(bytes, content_type)``. La implementación decide el formato
    (PDF, HTML, ...). El resto del sistema solo depende de este contrato.
    """

    def generate(self, invoice) -> Tuple[bytes, str]: ...
