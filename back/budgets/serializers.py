from rest_framework import serializers
from django.db.models import Sum
from .models import Budget
from transactions.models import Category, Transaction


class BudgetSerializer(serializers.ModelSerializer):
    category_name = serializers.CharField(source='category.name', read_only=True, default=None)
    spent = serializers.SerializerMethodField()

    class Meta:
        model = Budget
        fields = [
            'id', 'user', 'category', 'category_name', 'amount',
            'period', 'start_date', 'end_date', 'is_active',
            'spent', 'created_at', 'updated_at',
        ]
        read_only_fields = ['user', 'created_at', 'updated_at', 'spent']

    def _get_user(self, data):
        user = data.get('user')
        if not user:
            request = self.context.get('request')
            if request and hasattr(request, 'user'):
                user = request.user
        return user

    def validate(self, data):
        user = self._get_user(data)
        category = data.get('category')
        period = data.get('period')
        start_date = data.get('start_date', self.instance.start_date if self.instance else None)
        end_date = data.get('end_date', self.instance.end_date if self.instance else None)

        if start_date and end_date and start_date > end_date:
            raise serializers.ValidationError({
                'end_date': 'La fecha de fin debe ser posterior a la fecha de inicio.',
            })

        if user and period and start_date and end_date:
            queryset = Budget.objects.filter(
                user=user,
                period=period,
                is_active=True,
                start_date__lte=end_date,
                end_date__gte=start_date,
            )
            if category:
                queryset = queryset.filter(category=category)
            else:
                queryset = queryset.filter(category__isnull=True)

            if self.instance:
                queryset = queryset.exclude(pk=self.instance.pk)

            if queryset.exists():
                raise serializers.ValidationError(
                    'Ya existe un presupuesto activo para este período y categoría.'
                )

        return data

    def get_spent(self, obj):
        txs = Transaction.objects.filter(
            user=obj.user,
            type='EXPENSE',
            transaction_date__date__gte=obj.start_date,
            transaction_date__date__lte=obj.end_date,
        )
        if obj.category:
            txs = txs.filter(category=obj.category)

        result = txs.aggregate(total=Sum('amount'))['total']
        return float(result) if result else 0.0


class BudgetCreateSerializer(BudgetSerializer):
    class Meta(BudgetSerializer.Meta):
        fields = [
            'category', 'amount', 'period', 'start_date', 'end_date', 'spent',
        ]

    def validate_category(self, value):
        if value and value.type != 'EXPENSE':
            raise serializers.ValidationError(
                'La categoría de un presupuesto debe ser de tipo EXPENSE.'
            )
        return value
