from django.db import models
from django.contrib.auth.models import AbstractUser

# Create your models here.


class User(AbstractUser):
    """Modelo de usuario personalizado que extiende el AbstractUser de Django.
    Con los siguientes campos: id, username, email, first_name, last_name, created_at, updated_at.

    Args:
        AbstractUser (_type_): Clase base de Django para usuarios
    """
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
