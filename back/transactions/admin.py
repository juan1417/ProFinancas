from django.contrib import admin
from .models import Category, Transaction


@admin.register(Category)
class CategoryAdmin(admin.ModelAdmin):
    """Admin interface for Category model"""
    list_display = ['name', 'type', 'user', 'is_active', 'created_at']
    list_filter = ['type', 'is_active', 'created_at']
    search_fields = ['name', 'description', 'user__username']
    readonly_fields = ['created_at', 'updated_at']
    fieldsets = (
        ('Información Básica', {
            'fields': ('name', 'type', 'description', 'user')
        }),
        ('Estado', {
            'fields': ('is_active',)
        }),
        ('Fechas', {
            'fields': ('created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )


@admin.register(Transaction)
class TransactionAdmin(admin.ModelAdmin):
    """Admin interface for Transaction model"""
    list_display = ['description', 'type', 'amount', 'category', 'user', 'transaction_date']
    list_filter = ['type', 'category', 'transaction_date', 'created_at']
    search_fields = ['description', 'notes', 'user__username']
    readonly_fields = ['created_at', 'updated_at']
    date_hierarchy = 'transaction_date'
    fieldsets = (
        ('Información de la Transacción', {
            'fields': ('user', 'type', 'category', 'amount', 'transaction_date')
        }),
        ('Detalles', {
            'fields': ('description', 'notes')
        }),
        ('Fechas de Registro', {
            'fields': ('created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )
