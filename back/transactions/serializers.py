from rest_framework import serializers
from .models import Category, Transaction
from django.utils import timezone


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
        read_only_fields = ['created_at', 'updated_at', 'transactions_count']
    
    def get_transactions_count(self, obj):
        """Retorna el número de transacciones asociadas a esta categoría"""
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
        user = data.get('user') or self.instance.user if self.instance else None
        name = data.get('name')
        type_cat = data.get('type')
        
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
        read_only_fields = ['created_at', 'updated_at', 'category_name', 'category_type']
    
    def validate_amount(self, value):
        """Validación del monto de la transacción"""
        if value <= 0:
            raise serializers.ValidationError(
                "El monto debe ser mayor que cero"
            )
        return value
    
    def validate_description(self, value):
        """Validación de la descripción"""
        if len(value.strip()) < 3:
            raise serializers.ValidationError(
                "La descripción debe tener al menos 3 caracteres"
            )
        return value.strip()
    
    def validate_transaction_date(self, value):
        """Validación de la fecha de transacción"""
        if value > timezone.now():
            raise serializers.ValidationError(
                "La fecha de la transacción no puede ser futura"
            )
        return value
    
    def validate(self, data):
        """
        Validación a nivel de objeto para asegurar consistencia
        entre el tipo de transacción y la categoría
        """
        category = data.get('category') or (self.instance.category if self.instance else None)
        transaction_type = data.get('type')
        user = data.get('user') or (self.instance.user if self.instance else None)
        
        # Validar que la categoría pertenezca al usuario
        if category and user and category.user != user:
            raise serializers.ValidationError({
                'category': 'La categoría debe pertenecer al usuario de la transacción'
            })
        
        # Validar que el tipo de transacción coincida con el tipo de categoría
        if category and transaction_type and category.type != transaction_type:
            raise serializers.ValidationError({
                'type': f'El tipo de transacción debe coincidir con el tipo de categoría ({category.get_type_display()})'
            })
        
        # Validar que la categoría esté activa
        if category and not category.is_active:
            raise serializers.ValidationError({
                'category': 'No se puede crear una transacción con una categoría inactiva'
            })
        
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
            'id', 'type', 'amount', 'description', 
            'category_name', 'transaction_date'
        ]
