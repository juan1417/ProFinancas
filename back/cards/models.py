from django.conf import settings
from django.core.validators import RegexValidator
from django.db import models

last4_validator = RegexValidator(
    regex=r'^\d{4}$',
    message='El últimos 4 dígitos deben tener exactamente 4 números.',
)


class Card(models.Model):
    """Tarjeta bancaria del usuario (Gestor de Tarjetas, issue #13)."""
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='cards',
        verbose_name='Usuario',
    )
    brand = models.CharField(
        max_length=50,
        verbose_name='Marca',
        help_text='Visa, Mastercard, Amex, etc.',
    )
    bank = models.CharField(
        max_length=100,
        blank=True,
        default='',
        verbose_name='Banco emisor',
    )
    last4 = models.CharField(
        max_length=4,
        validators=[last4_validator],
        verbose_name='Últimos 4 dígitos',
    )
    expiry = models.CharField(
        max_length=5,
        verbose_name='Vencimiento',
        help_text='Formato MM/AA',
    )
    holder_name = models.CharField(
        max_length=100,
        blank=True,
        default='',
        verbose_name='Titular',
    )
    color_hex = models.CharField(
        max_length=9,
        blank=True,
        default='#1A4570',
        verbose_name='Color',
    )
    is_active = models.BooleanField(
        default=True,
        verbose_name='Activa',
    )
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        verbose_name = 'Tarjeta'
        verbose_name_plural = 'Tarjetas'
        ordering = ['-created_at']

    def __str__(self):
        return f'{self.brand} •••• {self.last4}'
