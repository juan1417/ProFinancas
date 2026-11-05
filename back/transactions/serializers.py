from rest_framework import serializers
from .models import Category, Transaction
from django.utils import timezone

from .domain import rules
from .domain.errors import DomainError
from .application import reports as report_service


class CategorySerializer(serializers.ModelSerializer):
    """
    Serializer para el modelo Category.
    Maneja la serialización y validación de categorías.
    """
    transactions_count = serializers.SerializerMethodField()

    class Meta:
        model = Category
        fields = [
            'id', 'name', 'type', 'description', 'user',
            'is_active', 'created_at', 'updated_at', 'transactions_count'
        ]
        # `user` is assigned server-side in CategoryViewSet.perform_create
        # from request.user. Marking it read-only makes the serializer
        # ignore it on input and the field stays "output only", so
        # clients don't need to (and shouldn't) send it.
        # See view.perform_create at transactions/views.py:37.
        read_only_fields = ['user', 'created_at', 'updated_at', 'transactions_count']
    
    def get_transactions_count(self, obj):
        """Retorna el número de transacciones asociadas a esta categoría"""
        if hasattr(obj, 'tx_count'):
            return obj.tx_count
        return obj.transactions.count()
    
    def validate_name(self, value):
        """Validación personalizada para el nombre de la categoría"""
        if len(value.strip()) < 3:
            raise serializers.ValidationError(
                "El nombre de la categoría debe tener al menos 3 caracteres"
            )
        return value.strip()
    
    def validate(self, data):
        """
        Validación a nivel de objeto para asegurar que no exista
        una categoría duplicada para el mismo usuario
        """
        user = self.context['request'].user
        name = data.get('name', self.instance.name if self.instance else None)
        type_cat = data.get('type', self.instance.type if self.instance else None)
        
        if user and name and type_cat:
            # Excluir la instancia actual en caso de actualización
            queryset = Category.objects.filter(
                user=user, 
                name__iexact=name,
                type=type_cat
            )
            if self.instance:
                queryset = queryset.exclude(pk=self.instance.pk)
            
            if queryset.exists():
                raise serializers.ValidationError(
                    "Ya existe una categoría con este nombre y tipo para este usuario"
                )
        
        return data


class TransactionSerializer(serializers.ModelSerializer):
    """
    Serializer para el modelo Transaction.
    Maneja la serialización y validación de transacciones manuales.
    """
    category_name = serializers.CharField(source='category.name', read_only=True)
    category_type = serializers.CharField(source='category.type', read_only=True)
    
    class Meta:
        model = Transaction
        fields = [
            'id', 'user', 'category', 'category_name', 'category_type',
            'type', 'amount', 'description', 'notes', 'transaction_date',
            'created_at', 'updated_at'
        ]
        # `user` is assigned server-side in TransactionViewSet.perform_create
        # from request.user. The Flutter client doesn't have (or need)
        # the user's UUID to POST a transaction, so we treat it as
        # read-only on input.
        read_only_fields = ['user', 'created_at', 'updated_at', 'category_name', 'category_type']
    
    def validate_amount(self, value):
        """Validación del monto de la transacción (delegada a domain)."""
        try:
            return rules.validate_amount(value)
        except DomainError as exc:
            raise serializers.ValidationError(str(exc))

    def validate_description(self, value):
        """Validación de la descripción (delegada a domain)."""
        try:
            return rules.validate_description(value)
        except DomainError as exc:
            raise serializers.ValidationError(str(exc))

    def validate_transaction_date(self, value):
        """Validación de la fecha de transacción (delegada a domain)."""
        now = timezone.now()
        try:
            return rules.validate_transaction_date(value, now)
        except DomainError as exc:
            raise serializers.ValidationError(str(exc))

    def validate(self, data):
        """
        Validación a nivel de objeto: pertenencia, tipo y estado de la categoría.
        Delegada a `transactions.domain.rules`.
        """
        category = data.get('category') or (self.instance.category if self.instance else None)
        # En un PATCH puede venir solo `category`: el `type` efectivo es el
        # guardado. Sin este fallback la validación de coincidencia se salta
        # y la transacción queda con type != category.type.
        transaction_type = data.get('type') or (self.instance.type if self.instance else None)
        user = self.context['request'].user

        try:
            rules.validate_category_assignment(category, transaction_type, user)
        except DomainError as exc:
            raise serializers.ValidationError(
                {exc.field: str(exc)} if exc.field else {'non_field_errors': str(exc)}
            )

        return data


class TransactionListSerializer(serializers.ModelSerializer):
    """
    Serializer simplificado para listados de transacciones.
    Incluye menos campos para mejorar el rendimiento en listados grandes.
    """
    category_name = serializers.CharField(source='category.name', read_only=True)

    class Meta:
        model = Transaction
        fields = [
            'id', 'category', 'type', 'amount', 'description',
            'category_name', 'transaction_date'
        ]
