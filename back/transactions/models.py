from django.db import models
from django.conf import settings
from django.core.validators import MinValueValidator
from decimal import Decimal


class Category(models.Model):
    """
    Modelo para categorización de gastos e ingresos.
    Permite clasificar transacciones en categorías personalizadas por usuario.
    """
    CATEGORY_TYPES = [
        ('INCOME', 'Ingreso'),
        ('EXPENSE', 'Gasto'),
    ]
    
    name = models.CharField(
        max_length=100,
        verbose_name='Nombre de la categoría',
        help_text='Nombre descriptivo de la categoría (ej: Alimentación, Transporte)'
    )
    type = models.CharField(
        max_length=10,
        choices=CATEGORY_TYPES,
        verbose_name='Tipo de categoría',
        help_text='Indica si la categoría es para ingresos o gastos'
    )
    description = models.TextField(
        blank=True,
        null=True,
        verbose_name='Descripción',
        help_text='Descripción opcional de la categoría'
    )
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='categories',
        verbose_name='Usuario',
        help_text='Usuario propietario de la categoría'
    )
    is_active = models.BooleanField(
        default=True,
        verbose_name='Activa',
        help_text='Indica si la categoría está activa y disponible para uso'
    )
    created_at = models.DateTimeField(
        auto_now_add=True,
        verbose_name='Fecha de creación'
    )
    updated_at = models.DateTimeField(
        auto_now=True,
        verbose_name='Fecha de actualización'
    )
    
    class Meta:
        verbose_name = 'Categoría'
        verbose_name_plural = 'Categorías'
        ordering = ['type', 'name']
        unique_together = ['user', 'name', 'type']
        indexes = [
            models.Index(fields=['user', 'type', 'is_active']),
        ]
    
    def __str__(self):
        return f"{self.get_type_display()}: {self.name}"


class Transaction(models.Model):
    """
    Modelo para movimientos manuales (ingresos y gastos).
    Registra todas las transacciones financieras del usuario.
    """
    TRANSACTION_TYPES = [
        ('INCOME', 'Ingreso'),
        ('EXPENSE', 'Gasto'),
    ]
    
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='transactions',
        verbose_name='Usuario',
        help_text='Usuario propietario de la transacción'
    )
    category = models.ForeignKey(
        Category,
        on_delete=models.PROTECT,
        related_name='transactions',
        verbose_name='Categoría',
        help_text='Categoría a la que pertenece esta transacción'
    )
    type = models.CharField(
        max_length=10,
        choices=TRANSACTION_TYPES,
        verbose_name='Tipo de transacción',
        help_text='Indica si es un ingreso o un gasto'
    )
    amount = models.DecimalField(
        max_digits=12,
        decimal_places=2,
        validators=[MinValueValidator(Decimal('0.01'))],
        verbose_name='Monto',
        help_text='Monto de la transacción (siempre positivo)'
    )
    description = models.CharField(
        max_length=255,
        verbose_name='Descripción',
        help_text='Descripción breve de la transacción'
    )
    notes = models.TextField(
        blank=True,
        null=True,
        verbose_name='Notas adicionales',
        help_text='Notas o detalles adicionales sobre la transacción'
    )
    transaction_date = models.DateTimeField(
        verbose_name='Fecha de la transacción',
        help_text='Fecha y hora en que ocurrió la transacción'
    )
    created_at = models.DateTimeField(
        auto_now_add=True,
        verbose_name='Fecha de creación'
    )
    updated_at = models.DateTimeField(
        auto_now=True,
        verbose_name='Fecha de actualización'
    )
    
    class Meta:
        verbose_name = 'Transacción'
        verbose_name_plural = 'Transacciones'
        ordering = ['-transaction_date']
        indexes = [
            models.Index(fields=['user', '-transaction_date']),
            models.Index(fields=['user', 'type']),
            models.Index(fields=['category', '-transaction_date']),
        ]
    
    def __str__(self):
        return f"{self.get_type_display()}: {self.amount} - {self.description}"
    
    def clean(self):
        """
        Validación personalizada para asegurar que el tipo de transacción
        coincide con el tipo de categoría.
        """
        from django.core.exceptions import ValidationError
        
        if self.category and self.type != self.category.type:
            raise ValidationError({
                'category': f'La categoría debe ser de tipo {self.get_type_display()}'
            })
    
    def save(self, *args, **kwargs):
        """Override save to call clean() for validation"""
        self.full_clean()
        super().save(*args, **kwargs)
