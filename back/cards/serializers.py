from rest_framework import serializers
from .models import Card, last4_validator


class CardSerializer(serializers.ModelSerializer):
    # DRF no hereda los validators del modelo: se aplican explícitamente.
    last4 = serializers.CharField(max_length=4, validators=[last4_validator])

    class Meta:
        model = Card
        fields = [
            'id', 'user', 'brand', 'bank', 'last4', 'expiry',
            'holder_name', 'color_hex', 'is_active', 'created_at', 'updated_at',
        ]
        read_only_fields = ['user', 'created_at', 'updated_at']
