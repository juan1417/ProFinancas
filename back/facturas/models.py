import uuid

from django.conf import settings
from django.db import models


class Invoice(models.Model):
    STATUS_CHOICES = [
        ('PENDING', 'Pendiente'),
        ('PAID', 'Pagada'),
        ('OVERDUE', 'Vencida'),
    ]

    id = models.UUIDField(
        primary_key=True,
        default=uuid.uuid4,
        editable=False,
    )
    invoice_number = models.CharField(
        max_length=20,
        verbose_name='Número de factura',
    )
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='invoices',
        verbose_name='Usuario',
    )
    provider = models.CharField(
        max_length=200,
        verbose_name='Proveedor',
    )
    amount = models.DecimalField(
        max_digits=12,
        decimal_places=2,
        verbose_name='Monto',
    )
    status = models.CharField(
        max_length=10,
        choices=STATUS_CHOICES,
        default='PENDING',
        verbose_name='Estado',
    )
    category = models.CharField(
        max_length=100,
        blank=True,
        default='',
        verbose_name='Categoría',
    )
    due_date = models.DateField(
        verbose_name='Fecha de vencimiento',
    )
    registered_date = models.DateField(
        verbose_name='Fecha de registro',
    )
    paid_date = models.DateField(
        null=True,
        blank=True,
        verbose_name='Fecha de pago',
    )
    notes = models.TextField(
        blank=True,
        default='',
        verbose_name='Notas',
    )
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        verbose_name = 'Factura'
        verbose_name_plural = 'Facturas'
        ordering = ['-registered_date']
        constraints = [
            models.UniqueConstraint(
                fields=['user', 'invoice_number'],
                name='facturas_invoice_user_number_uniq',
            ),
        ]
        indexes = [
            models.Index(fields=['user', 'status']),
            models.Index(fields=['user', '-registered_date']),
        ]

    def __str__(self):
        return f'{self.invoice_number} - {self.provider}'

    # La numeración vive en `facturas/application/invoice_service.py` (regla en
    # `facturas/domain/numbering.py`). El modelo no contiene reglas de negocio.
