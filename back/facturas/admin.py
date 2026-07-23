from django.contrib import admin
from .models import Invoice


@admin.register(Invoice)
class InvoiceAdmin(admin.ModelAdmin):
    list_display = ['invoice_number', 'provider', 'amount', 'status', 'due_date', 'user']
    list_filter = ['status', 'category']
    search_fields = ['invoice_number', 'provider']
    readonly_fields = ['id', 'invoice_number', 'created_at', 'updated_at']
