from django.conf import settings
from django.db import models


class ImportedRow(models.Model):
    """Huella de una fila ya importada, para deduplicar reimportaciones."""
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    row_hash = models.CharField(max_length=64)
    transaction = models.ForeignKey(
        'transactions.Transaction', on_delete=models.SET_NULL, null=True, blank=True,
    )
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        constraints = [
            models.UniqueConstraint(fields=['user', 'row_hash'], name='ingestion_user_rowhash_uniq'),
        ]
        indexes = [models.Index(fields=['user', 'row_hash'])]

    def __str__(self):
        return f'{self.user_id}:{self.row_hash[:8]}'
