from rest_framework import viewsets, filters
from django_filters.rest_framework import DjangoFilterBackend

from .models import Invoice
from .serializers import InvoiceSerializer, InvoiceCreateSerializer


class InvoiceViewSet(viewsets.ModelViewSet):
    """
    ViewSet para gestionar facturas.

    Endpoints:
    - GET    /invoices/           — Listar facturas del usuario
    - POST   /invoices/           — Crear factura
    - GET    /invoices/{id}/      — Detalle de factura
    - PUT    /invoices/{id}/      — Actualizar factura
    - DELETE /invoices/{id}/      — Eliminar factura
    """
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['status', 'category']
    search_fields = ['provider', 'invoice_number', 'notes']
    ordering_fields = ['amount', 'due_date', 'registered_date', 'created_at']
    ordering = ['-registered_date']

    def get_queryset(self):
        return Invoice.objects.filter(user=self.request.user)

    def get_serializer_class(self):
        if self.action == 'create':
            return InvoiceCreateSerializer
        return InvoiceSerializer

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)
