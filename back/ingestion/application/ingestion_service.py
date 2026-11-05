"""Casos de uso de ingesta bancaria."""
import hashlib

from django.db import transaction

from transactions.models import Category, Transaction
from transactions.application.dates import aware_bound
from ..models import ImportedRow
from ..adapters.csv import CsvBankStatementImporter


def _importer():
    return CsvBankStatementImporter()


def preview(user, file_obj) -> list[dict]:
    rows = _importer().parse(file_obj)
    existing = set(
        ImportedRow.objects.filter(user=user).values_list('row_hash', flat=True)
    )
    for row in rows:
        row['is_duplicate'] = row['hash'] in existing
    return rows


def _resolve_category(user, tx_type, category_id=None):
    if category_id:
        try:
            cat = Category.objects.get(id=category_id, user=user)
        except Category.DoesNotExist:
            cat = None
        if cat is not None and cat.type == tx_type:
            return cat
    cat, _ = Category.objects.get_or_create(
        user=user, name='Importado', type=tx_type,
        defaults={'is_active': True},
    )
    return cat


def confirm(user, rows: list[dict]) -> dict:
    created = 0
    skipped = 0
    errors = []
    for row in rows:
        row_hash = row.get('hash')
        if not row_hash:
            # Recompute from normalized content if missing.
            row_hash = hashlib.sha256(
                '|'.join(str(row.get(k)) for k in ('date', 'description', 'amount', 'type')).encode()
            ).hexdigest()
        if ImportedRow.objects.filter(user=user, row_hash=row_hash).exists():
            skipped += 1
            continue
        try:
            with transaction.atomic():
                cat = _resolve_category(user, row['type'], row.get('category_id'))
                tx = Transaction.objects.create(
                    user=user,
                    category=cat,
                    type=row['type'],
                    amount=row['amount'],
                    description=row.get('description') or 'Importado',
                    transaction_date=aware_bound(row['date'], end_of_day=False),
                )
                ImportedRow.objects.create(
                    user=user, row_hash=row_hash, transaction=tx,
                )
                created += 1
        except Exception as exc:  # noqa: BLE001
            errors.append(str(exc))
    return {'created': created, 'skipped': skipped, 'errors': errors}
