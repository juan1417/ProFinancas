import uuid
from django.db import models
from django.contrib.auth.models import AbstractUser


class User(AbstractUser):
    """
    Modelo de usuario personalizado con UUID como clave primaria.
    Extiende AbstractUser de Django para mantener compatibilidad con
    el sistema de autenticación nativo.
    """
    id = models.UUIDField(
        primary_key=True,
        default=uuid.uuid4,
        editable=False,
    )
    email = models.EmailField(unique=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    # Usamos email como campo de autenticación principal
    USERNAME_FIELD = 'email'
    REQUIRED_FIELDS = ['username']

    class Meta:
        db_table = 'usuarios'
        ordering = ['-created_at']

    def __str__(self):
        return self.email


class FeatureFlag(models.Model):
    """Bandera de funcionalidad controlada desde el backend (feature flag)."""
    key = models.CharField(max_length=64, unique=True)
    enabled = models.BooleanField(default=False)
    description = models.CharField(max_length=255, blank=True, default='')
    min_version = models.CharField(max_length=32, blank=True, default='')
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'feature_flags'
        ordering = ['key']

    def __str__(self):
        return f'{self.key}={self.enabled}'
