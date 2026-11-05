"""Adaptador PDF con reportlab (sin dependencias de sistema)."""
from io import BytesIO

from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.lib.units import cm
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle
from reportlab.lib.styles import getSampleStyleSheet

from ..ports import DocumentGenerator


class ReportLabDocumentGenerator:
    """Implementación concreta de `DocumentGenerator` usando reportlab."""

    def generate(self, invoice) -> tuple[bytes, str]:
        buffer = BytesIO()
        doc = SimpleDocTemplate(buffer, pagesize=letter)
        styles = getSampleStyleSheet()
        story = []

        story.append(Paragraph(f'Factura {invoice.invoice_number}', styles['Title']))
        story.append(Spacer(1, 12))

        rows = [
            ['Proveedor', str(invoice.provider)],
            ['Categoría', str(invoice.category) or '-'],
            ['Estado', str(invoice.status)],
            ['Monto', str(invoice.amount)],
            ['Vencimiento', str(invoice.due_date)],
            ['Registro', str(invoice.registered_date)],
        ]
        if invoice.paid_date:
            rows.append(['Pagada el', str(invoice.paid_date)])
        if invoice.notes:
            rows.append(['Notas', str(invoice.notes)])

        table = Table(rows, colWidths=[4 * cm, 12 * cm])
        table.setStyle(TableStyle([
            ('BACKGROUND', (0, 0), (0, -1), colors.lightgrey),
            ('GRID', (0, 0), (-1, -1), 0.5, colors.grey),
            ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
            ('PADDING', (0, 0), (-1, -1), 6),
        ]))
        story.append(table)

        doc.build(story)
        return buffer.getvalue(), 'application/pdf'
