from django.db import models
from django.conf import settings
from django.core.validators import MinValueValidator
from decimal import Decimal


class Budget(models.Model):
    PERIOD_TYPES = [
        ('weekly', 'Semanal'),
        ('monthly', 'Mensual'),
        ('yearly', 'Anual'),
    ]

    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='budgets',
        verbose_name='Usuario',
    )
    category = models.ForeignKey(
        'transactions.Category',
        on_delete=models.CASCADE,
        null=True,
        blank=True,
        related_name='budgets',
        verbose_name='Categoría',
        help_text='Si se omite, el presupuesto aplica al total del usuario.',
    )
    amount = models.DecimalField(
        max_digits=12,
        decimal_places=2,
        validators=[MinValueValidator(Decimal('0.01'))],
        verbose_name='Monto del presupuesto',
    )
    period = models.CharField(
        max_length=10,
        choices=PERIOD_TYPES,
        default='monthly',
        verbose_name='Período',
    )
    start_date = models.DateField(
        verbose_name='Fecha de inicio',
    )
    end_date = models.DateField(
        verbose_name='Fecha de fin',
    )
    is_active = models.BooleanField(
        default=True,
        verbose_name='Activo',
    )
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        verbose_name = 'Presupuesto'
        verbose_name_plural = 'Presupuestos'
        ordering = ['-created_at']
        indexes = [
            models.Index(fields=['user', 'is_active']),
            models.Index(fields=['user', 'period']),
        ]

    def __str__(self):
        cat = self.category.name if self.category else 'General'
        return f'{cat} ({self.get_period_display()}): {self.amount}'
