"""Generador PDF mínimo, sin dependencias (fallback para serverless como Vercel).

Escribe un PDF 1.4 de una sola página con el número de factura, proveedor,
monto y fechas. Es suficiente para que el endpoint devuelva un PDF real y no
rompa cuando reportlab no se puede instalar (Pillow no compila en Vercel).
"""


def _pdf_escape(text):
    return (
        str(text)
        .replace('\\', '\\\\')
        .replace('(', '\\(')
        .replace(')', '\\)')
    )


def generate_pdf(invoice):
    lines = [
        ('Proveedor', invoice.provider),
        ('Categoria', invoice.category or '-'),
        ('Estado', invoice.status),
        ('Monto', str(invoice.amount)),
        ('Vencimiento', str(invoice.due_date)),
        ('Registro', str(invoice.registered_date)),
    ]
    if invoice.paid_date:
        lines.append(('Pagada el', str(invoice.paid_date)))
    if getattr(invoice, 'notes', ''):
        lines.append(('Notas', str(invoice.notes)))

    title = "Factura %s" % _pdf_escape(invoice.invoice_number)
    content_parts = [
        "BT", "/F1 18 Tf", "72 760 Td", "(%s) Tj" % title, "ET",
        "BT", "/F1 11 Tf", "72 720 Td",
    ]
    for label, value in lines:
        content_parts.append("14 Td")
        content_parts.append("(%s: %s) Tj" % (label, _pdf_escape(value)))
    content_parts.append("ET")
    content = "\n".join(content_parts).encode('latin-1', 'replace')

    objects = [
        b"<< /Type /Catalog /Pages 2 0 R >>",
        b"<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
        (b"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] "
         b"/Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>"),
        b"<< /Length %d >>\nstream\n%s\nendstream" % (len(content), content),
        b"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
    ]

    out = bytearray(b"%PDF-1.4\n")
    offsets = []
    for i, obj in enumerate(objects, start=1):
        offsets.append(len(out))
        out += ("%d 0 obj\n" % i).encode('latin-1')
        out += obj
        out += b"\nendobj\n"

    xref_pos = len(out)
    n = len(objects) + 1
    out += ("xref\n0 %d\n" % n).encode('latin-1')
    out += b"0000000000 65535 f \n"
    for off in offsets:
        out += ("%010d 00000 n \n" % off).encode('latin-1')
    out += ("trailer\n<< /Size %d /Root 1 0 R >>\nstartxref\n%d\n%%EOF\n"
            % (n, xref_pos)).encode('latin-1')

    return bytes(out)
