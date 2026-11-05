"""Caso de uso: generar el documento de una factura."""
from ..adapters.pdf import ReportLabDocumentGenerator


def generate_invoice_document(invoice) -> tuple[bytes, str]:
    generator = ReportLabDocumentGenerator()
    return generator.generate(invoice)
