from rest_framework import permissions, viewsets, filters, status
from rest_framework.decorators import action
from rest_framework.response import Response
from django_filters.rest_framework import DjangoFilterBackend

from .models import Budget
from .serializers import BudgetSerializer, BudgetCreateSerializer


class BudgetViewSet(viewsets.ModelViewSet):
    """
    ViewSet para gestionar presupuestos.

    Endpoints:
    - GET    /budgets/           — Listar presupuestos del usuario
    - POST   /budgets/           — Crear presupuesto
    - GET    /budgets/{id}/      — Detalle de presupuesto
    - PUT    /budgets/{id}/      — Actualizar presupuesto
    - DELETE /budgets/{id}/      — Eliminar presupuesto
    """
    permission_classes = [permissions.IsAuthenticated]
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['period', 'is_active', 'category']
    search_fields = ['category__name']
    ordering_fields = ['amount', 'start_date', 'created_at']
    ordering = ['-created_at']

    def get_queryset(self):
        return Budget.objects.filter(
            user=self.request.user,
        ).select_related('category')

    def get_serializer_class(self):
        if self.action in ('create', 'update', 'partial_update'):
            return BudgetCreateSerializer
        return BudgetSerializer

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)

    @action(detail=False, methods=['get'], url_path='alerts')
    def alerts(self, request):
        """GET /api/budgets/alerts/ — estado de límite por presupuesto activo."""
        from .application.queries import spent_for_budget
        from .domain.rules import budget_utilization
        data = []
        for budget in self.get_queryset().filter(is_active=True).select_related('category'):
            spent = spent_for_budget(budget)
            pct, status = budget_utilization(spent, float(budget.amount))
            data.append({
                'budget_id': budget.id,
                'category_name': budget.category.name if budget.category else 'General',
                'period': budget.period,
                'amount': float(budget.amount),
                'spent': spent,
                'remaining': round(float(budget.amount) - spent, 2),
                'percentage': pct,
                'status': status,
            })
        return Response(data)
