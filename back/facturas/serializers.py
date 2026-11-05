from rest_framework import serializers
from django.utils import timezone

from .models import Invoice
from .domain.status import effective_status


class InvoiceSerializer(serializers.ModelSerializer):
    class Meta:
        model = Invoice
        fields = [
            'id', 'invoice_number', 'user', 'provider', 'amount',
            'status', 'category', 'due_date', 'registered_date',
            'paid_date', 'notes', 'created_at', 'updated_at',
        ]
        read_only_fields = ['id', 'invoice_number', 'user', 'created_at', 'updated_at']

    def to_representation(self, instance):
        data = super().to_representation(instance)
        # OVERDUE se deriva de la fecha de vencimiento al leer; no se persiste.
        data['status'] = effective_status(
            instance.status, instance.due_date, timezone.localdate(),
        )
        return data

    def validate(self, data):
        status = data.get('status', self.instance.status if self.instance else None)
        paid_date = data.get('paid_date', self.instance.paid_date if self.instance else None)

        if status == 'PAID' and not paid_date:
            raise serializers.ValidationError({
                'paid_date': 'Las facturas pagadas deben tener una fecha de pago.',
            })

        if paid_date and status != 'PAID':
            raise serializers.ValidationError({
                'status': 'Si se establece fecha de pago, el estado debe ser PAID.',
            })

        return data


class InvoiceCreateSerializer(serializers.ModelSerializer):
    invoice_number = serializers.CharField(read_only=True)

    class Meta(InvoiceSerializer.Meta):
        fields = [
            'invoice_number', 'provider', 'amount', 'status', 'category',
            'due_date', 'registered_date', 'paid_date', 'notes',
        ]

    def validate(self, data):
        status = data.get('status', 'PENDING')
        paid_date = data.get('paid_date')

        if status == 'PAID' and not paid_date:
            raise serializers.ValidationError({
                'paid_date': 'Las facturas pagadas deben tener una fecha de pago.',
            })

        if paid_date and status != 'PAID':
            raise serializers.ValidationError({
                'status': 'Si se establece fecha de pago, el estado debe ser PAID.',
            })

        return data
