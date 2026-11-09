"""Caso de uso: generar el documento de una factura.

Usa reportlab si está disponible; si no (p. ej. Vercel, donde Pillow no compila
como dependencia de reportlab), cae a un generador PDF propio sin dependencias.
"""
try:
    from ..adapters.pdf import ReportLabDocumentGenerator
    _AVAILABLE = True
except Exception:  # reportlab/Pillow no instalados
    _AVAILABLE = False
    ReportLabDocumentGenerator = None


def generate_invoice_document(invoice):
    """Devuelve (bytes, content_type) para la factura."""
    if _AVAILABLE:
        return ReportLabDocumentGenerator().generate(invoice)
    from ..adapters.pdf_minimal import generate_pdf
    return generate_pdf(invoice), 'application/pdf'
