from io import BytesIO
from django.http import FileResponse
from rest_framework import viewsets, filters, status
from rest_framework.decorators import action
from rest_framework.response import Response
from django_filters.rest_framework import DjangoFilterBackend
from django_filters import FilterSet, CharFilter
from django.utils import timezone

from .models import Invoice
from .serializers import InvoiceSerializer, InvoiceCreateSerializer
from .application.invoice_service import create_invoice
from .domain.status import effective_status
from .application.document_service import generate_invoice_document


class InvoiceFilter(FilterSet):
    """Filtro por estado efectivo (OVERDUE se deriva de due_date)."""
    status = CharFilter(method='filter_effective_status')

    class Meta:
        model = Invoice
        fields = ['category']

    def filter_effective_status(self, queryset, name, value):
        today = timezone.localdate()
        if value == 'OVERDUE':
            return queryset.filter(status='PENDING', due_date__lt=today)
        if value == 'PENDING':
            # Una factura pendiente vencida se cuenta como OVERDUE, así que
            # ?status=PENDING devuelve solo las que siguen pendientes.
            return queryset.filter(status='PENDING', due_date__gte=today)
        return queryset.filter(status=value)


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
    filterset_class = InvoiceFilter
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
        # La numeración vive en application/; el serializer ya validó.
        serializer.instance = create_invoice(
            user=self.request.user, **serializer.validated_data,
        )

    def create(self, request, *args, **kwargs):
        """Crea la factura y responde con el recurso completo (incluye `id`).

        `InvoiceCreateSerializer` no expone `id` porque solo valida la
        entrada; la respuesta usa `InvoiceSerializer` para que el cliente
        pueda direccionar lo que acaba de crear.
        """
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        self.perform_create(serializer)
        output = InvoiceSerializer(
            serializer.instance, context=self.get_serializer_context(),
        )
        headers = self.get_success_headers(output.data)
        return Response(output.data, status=status.HTTP_201_CREATED, headers=headers)


    @action(detail=True, methods=['get'], url_path='document')
    def document(self, request, pk=None):
        invoice = self.get_object()
        content, content_type = generate_invoice_document(invoice)
        response = FileResponse(
            BytesIO(content),
            content_type=content_type,
        )
        response['Content-Disposition'] = (
            f'attachment; filename="invoice-{invoice.invoice_number}.pdf"'
        )
        return response
