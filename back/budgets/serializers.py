from rest_framework import serializers
from .models import Budget
from .domain.rules import validate_budget_category
from .domain.errors import DomainError
from .application import queries as budget_queries


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
        return budget_queries.spent_for_budget(obj)


class BudgetCreateSerializer(BudgetSerializer):
    class Meta(BudgetSerializer.Meta):
        fields = [
            'category', 'amount', 'period', 'start_date', 'end_date',
            'is_active', 'spent',
        ]

    def validate_category(self, value):
        request = self.context.get('request')
        user = getattr(request, 'user', None) if request else None
        try:
            validate_budget_category(value, user)
        except DomainError as exc:
            raise serializers.ValidationError(str(exc))
        return value
