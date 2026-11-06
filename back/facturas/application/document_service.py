"""Caso de uso: generar el documento de una factura."""
try:
    from ..adapters.pdf import ReportLabDocumentGenerator
    _AVAILABLE = True
except Exception:  # pragma: no cover - reportlab no instalado
    _AVAILABLE = False
    ReportLabDocumentGenerator = None


def generate_invoice_document(invoice):
    if not _AVAILABLE:
        raise RuntimeError('Generador de documentos no disponible (reportlab no instalado).')
    generator = ReportLabDocumentGenerator()
    return generator.generate(invoice)
