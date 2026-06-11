from rest_framework import viewsets, filters, status
from rest_framework.decorators import action
from rest_framework.response import Response
from django_filters.rest_framework import DjangoFilterBackend

from .models import Category, Transaction
from .serializers import (
    CategorySerializer,
    TransactionSerializer,
    TransactionListSerializer,
)
from .services import CategoryService, TransactionService


class CategoryViewSet(viewsets.ModelViewSet):
    """
    ViewSet para gestionar categorías de transacciones.

    Endpoints disponibles:
    - GET /categories/ - Listar todas las categorías del usuario
    - POST /categories/ - Crear una nueva categoría
    - GET /categories/{id}/ - Obtener detalle de una categoría
    - PUT/PATCH /categories/{id}/ - Actualizar una categoría
    - DELETE /categories/{id}/ - Eliminar una categoría
    - GET /categories/by_type/ - Filtrar categorías por tipo
    """
    serializer_class = CategorySerializer
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['type', 'is_active']
    search_fields = ['name', 'description']
    ordering_fields = ['name', 'created_at', 'type']
    ordering = ['type', 'name']

    def get_queryset(self):
        return Category.objects.filter(user=self.request.user)

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)

    @action(detail=False, methods=['get'])
    def by_type(self, request):
        """GET /categories/by_type/"""
        result = CategoryService.get_by_type(
            self.get_queryset(),
            category_type=request.query_params.get('type'),
        )
        if not result['grouped']:
            serializer = self.get_serializer(result['filtered'], many=True)
            return Response(serializer.data)

        return Response({
            'income': CategorySerializer(result['income'], many=True).data,
            'expense': CategorySerializer(result['expense'], many=True).data,
        })


class TransactionViewSet(viewsets.ModelViewSet):
    """
    ViewSet para gestionar transacciones (movimientos manuales).

    Endpoints disponibles:
    - GET /transactions/ - Listar todas las transacciones del usuario
    - POST /transactions/ - Crear una nueva transacción
    - GET /transactions/{id}/ - Obtener detalle de una transacción
    - PUT/PATCH /transactions/{id}/ - Actualizar una transacción
    - DELETE /transactions/{id}/ - Eliminar una transacción
    - GET /transactions/summary/ - Obtener resumen financiero
    - GET /transactions/by_category/ - Agrupar transacciones por categoría
    """
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['type', 'category', 'transaction_date']
    search_fields = ['description', 'notes']
    ordering_fields = ['transaction_date', 'amount', 'created_at']
    ordering = ['-transaction_date']

    def get_queryset(self):
        return Transaction.objects.filter(user=self.request.user).select_related('category')

    def get_serializer_class(self):
        if self.action == 'list':
            return TransactionListSerializer
        return TransactionSerializer

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)

    @action(detail=False, methods=['get'])
    def summary(self, request):
        """GET /transactions/summary/"""
        data = TransactionService.get_summary(
            self.get_queryset(),
            period=request.query_params.get('period', 'month'),
            start_date=request.query_params.get('start_date'),
            end_date=request.query_params.get('end_date'),
        )
        return Response(data)

    @action(detail=False, methods=['get'])
    def by_category(self, request):
        """GET /transactions/by_category/"""
        try:
            data = TransactionService.get_by_category(
                self.get_queryset(),
                transaction_type=request.query_params.get('type'),
                start_date=request.query_params.get('start_date'),
                end_date=request.query_params.get('end_date'),
            )
        except ValueError as e:
            # Inverted dates or similar input errors → 400 instead of
            # silently returning an empty list.
            return Response({'detail': str(e)},
                            status=status.HTTP_400_BAD_REQUEST)
        return Response(data)
