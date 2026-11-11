from django.contrib import admin
from .models import Card


@admin.register(Card)
class CardAdmin(admin.ModelAdmin):
    list_display = ('user', 'brand', 'bank', 'last4', 'expiry', 'is_active')
    list_filter = ('brand', 'is_active')
