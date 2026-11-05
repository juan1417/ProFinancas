from rest_framework import permissions, status, viewsets
from rest_framework.decorators import action
from rest_framework.parsers import MultiPartParser, FormParser, JSONParser
from rest_framework.response import Response

from .application.ingestion_service import preview, confirm


class IngestionViewSet(viewsets.ViewSet):
    permission_classes = [permissions.IsAuthenticated]
    parser_classes = [MultiPartParser, FormParser, JSONParser]

    @action(detail=False, methods=['post'], url_path='preview')
    def preview_action(self, request):
        file_obj = request.FILES.get('file')
        if file_obj is None:
            return Response({'detail': 'Se requiere un archivo (campo "file").'},
                            status=status.HTTP_400_BAD_REQUEST)
        rows = preview(request.user, file_obj)
        return Response({'rows': rows})

    @action(detail=False, methods=['post'], url_path='confirm')
    def confirm_action(self, request):
        rows = request.data.get('rows')
        if not isinstance(rows, list):
            return Response({'detail': 'Se requiere "rows" (lista).'},
                            status=status.HTTP_400_BAD_REQUEST)
        result = confirm(request.user, rows)
        return Response(result)
