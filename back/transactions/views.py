from rest_framework import viewsets, filters, status
from rest_framework.decorators import action
from rest_framework.response import Response
from django_filters.rest_framework import DjangoFilterBackend
from django.db.models import Sum, Count
from django.utils import timezone
from datetime import timedelta

from .models import Category, Transaction
from .serializers import (
    CategorySerializer, 
    TransactionSerializer, 
    TransactionListSerializer
)


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
        """
        Retorna solo las categorías del usuario autenticado.
        Para desarrollo sin autenticación, retorna todas.
        """
        # TODO: Implementar autenticación JWT
        # return Category.objects.filter(user=self.request.user)
        return Category.objects.all()
    
    def perform_create(self, serializer):
        """
        Asigna automáticamente el usuario autenticado al crear una categoría.
        """
        # TODO: Usar self.request.user cuando se implemente autenticación
        serializer.save()
    
    @action(detail=False, methods=['get'])
    def by_type(self, request):
        """
        Endpoint personalizado para obtener categorías agrupadas por tipo.
        GET /categories/by_type/
        """
        category_type = request.query_params.get('type', None)
        
        if category_type and category_type in ['INCOME', 'EXPENSE']:
            categories = self.get_queryset().filter(type=category_type, is_active=True)
            serializer = self.get_serializer(categories, many=True)
            return Response(serializer.data)
        
        # Retornar categorías agrupadas por tipo
        income_categories = self.get_queryset().filter(type='INCOME', is_active=True)
        expense_categories = self.get_queryset().filter(type='EXPENSE', is_active=True)
        
        return Response({
            'income': CategorySerializer(income_categories, many=True).data,
            'expense': CategorySerializer(expense_categories, many=True).data
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
        """
        Retorna solo las transacciones del usuario autenticado.
        Para desarrollo sin autenticación, retorna todas.
        """
        # TODO: Implementar autenticación JWT
        # return Transaction.objects.filter(user=self.request.user).select_related('category')
        return Transaction.objects.all().select_related('category')
    
    def get_serializer_class(self):
        """
        Usa TransactionListSerializer para listados,
        TransactionSerializer para detalle y creación.
        """
        if self.action == 'list':
            return TransactionListSerializer
        return TransactionSerializer
    
    def perform_create(self, serializer):
        """
        Asigna automáticamente el usuario autenticado al crear una transacción.
        """
        # TODO: Usar self.request.user cuando se implemente autenticación
        serializer.save()
    
    @action(detail=False, methods=['get'])
    def summary(self, request):
        """
        Endpoint para obtener un resumen financiero del usuario.
        GET /transactions/summary/
        
        Parámetros opcionales:
        - start_date: Fecha inicial (YYYY-MM-DD)
        - end_date: Fecha final (YYYY-MM-DD)
        - period: 'week', 'month', 'year' (por defecto: 'month')
        """
        queryset = self.get_queryset()
        
        # Filtrar por período si se especifica
        period = request.query_params.get('period', 'month')
        start_date = request.query_params.get('start_date')
        end_date = request.query_params.get('end_date')
        
        if start_date and end_date:
            queryset = queryset.filter(
                transaction_date__gte=start_date,
                transaction_date__lte=end_date
            )
        else:
            # Usar período predefinido
            now = timezone.now()
            if period == 'week':
                start = now - timedelta(days=7)
            elif period == 'year':
                start = now - timedelta(days=365)
            else:  # month por defecto
                start = now - timedelta(days=30)
            
            queryset = queryset.filter(transaction_date__gte=start)
        
        # Calcular totales
        income_total = queryset.filter(type='INCOME').aggregate(
            total=Sum('amount')
        )['total'] or 0
        
        expense_total = queryset.filter(type='EXPENSE').aggregate(
            total=Sum('amount')
        )['total'] or 0
        
        balance = income_total - expense_total
        
        # Contar transacciones
        transaction_count = queryset.count()
        income_count = queryset.filter(type='INCOME').count()
        expense_count = queryset.filter(type='EXPENSE').count()
        
        return Response({
            'period': period,
            'total_income': float(income_total),
            'total_expenses': float(expense_total),
            'balance': float(balance),
            'transaction_count': transaction_count,
            'income_count': income_count,
            'expense_count': expense_count,
        })
    
    @action(detail=False, methods=['get'])
    def by_category(self, request):
        """
        Endpoint para agrupar transacciones por categoría.
        GET /transactions/by_category/
        
        Parámetros opcionales:
        - type: 'INCOME' o 'EXPENSE'
        - start_date: Fecha inicial (YYYY-MM-DD)
        - end_date: Fecha final (YYYY-MM-DD)
        """
        queryset = self.get_queryset()
        
        # Filtrar por tipo si se especifica
        transaction_type = request.query_params.get('type')
        if transaction_type in ['INCOME', 'EXPENSE']:
            queryset = queryset.filter(type=transaction_type)
        
        # Filtrar por fechas si se especifican
        start_date = request.query_params.get('start_date')
        end_date = request.query_params.get('end_date')
        
        if start_date:
            queryset = queryset.filter(transaction_date__gte=start_date)
        if end_date:
            queryset = queryset.filter(transaction_date__lte=end_date)
        
        # Agrupar por categoría y calcular totales
        categories_summary = queryset.values(
            'category__id',
            'category__name',
            'category__type'
        ).annotate(
            total_amount=Sum('amount'),
            transaction_count=Count('id')
        ).order_by('-total_amount')
        
        return Response(categories_summary)
