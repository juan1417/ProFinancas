from rest_framework import permissions, viewsets, filters

from .models import Card
from .serializers import CardSerializer


class CardViewSet(viewsets.ModelViewSet):
    """
    Gestor de Tarjetas (issue #13). Solo muestra/crea tarjetas del usuario autenticado.

    - GET    /api/cards/        — Listar tarjetas propias
    - POST   /api/cards/        — Crear tarjeta
    - GET    /api/cards/{id}/   — Detalle
    - PUT    /api/cards/{id}/   — Reemplazar
    - PATCH  /api/cards/{id}/   — Actualización parcial
    - DELETE /api/cards/{id}/   — Eliminar
    """
    permission_classes = [permissions.IsAuthenticated]
    serializer_class = CardSerializer
    filter_backends = [filters.SearchFilter, filters.OrderingFilter]
    search_fields = ['brand', 'bank', 'holder_name']
    ordering_fields = ['created_at', 'brand']
    ordering = ['-created_at']

    def get_queryset(self):
        return Card.objects.filter(user=self.request.user)

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)
