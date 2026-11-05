"""Adaptador CSV del puerto BankStatementImporter."""
import csv
import hashlib
import io
from datetime import datetime
from decimal import Decimal, InvalidOperation

_DATE_KEYS = ('date', 'fecha', 'transaction_date', 'fecha_transaccion')
_DESC_KEYS = ('description', 'descripcion', 'concepto', 'detalle', 'memo')
_AMOUNT_KEYS = ('amount', 'monto', 'valor', 'importe')
_TYPE_KEYS = ('type', 'tipo')


def _find(headers, candidates):
    lowered = [h.strip().lower() for h in headers]
    for cand in candidates:
        if cand in lowered:
            return lowered.index(cand)
    return None


def _parse_date(raw):
    raw = (raw or '').strip()
    for fmt in ('%Y-%m-%d', '%d/%m/%Y', '%d-%m-%Y', '%m/%d/%Y'):
        try:
            return datetime.strptime(raw, fmt).date().isoformat()
        except ValueError:
            continue
    return raw


def _row_hash(row):
    payload = '|'.join(str(v) for v in row.values())
    return hashlib.sha256(payload.encode('utf-8')).hexdigest()


class CsvBankStatementImporter:
    def parse(self, file_obj) -> list[dict]:
        if hasattr(file_obj, 'read'):
            raw = file_obj.read()
        else:
            raw = file_obj
        if isinstance(raw, bytes):
            text = raw.decode('utf-8-sig')
        else:
            text = raw
        reader = csv.DictReader(io.StringIO(text))
        headers = reader.fieldnames or []
        di = _find(headers, _DATE_KEYS)
        desc_i = _find(headers, _DESC_KEYS)
        amt_i = _find(headers, _AMOUNT_KEYS)
        type_i = _find(headers, _TYPE_KEYS)

        hnames = headers
        def get(row, idx):
            if idx is None:
                return ''
            key = hnames[idx]
            return row.get(key, '')

        result = []
        for row in reader:
            date_raw = get(row, di)
            desc_raw = get(row, desc_i)
            amt_raw = get(row, amt_i)
            type_raw = get(row, type_i).strip().upper()

            try:
                amount = Decimal(str(amt_raw).replace(',', '.').strip())
            except (InvalidOperation, ValueError):
                continue

            if type_raw in ('INCOME', 'INGRESO', 'CREDITO', 'CRÉDITO', 'CREDIT'):
                tx_type = 'INCOME'
            elif type_raw in ('EXPENSE', 'GASTO', 'DEBITO', 'DÉBITO', 'DEBIT'):
                tx_type = 'EXPENSE'
            else:
                tx_type = 'EXPENSE' if amount < 0 else 'INCOME'

            amount = abs(amount)
            parsed_date = _parse_date(date_raw)
            normalized = {
                'date': parsed_date,
                'description': (desc_raw or '').strip(),
                'amount': str(amount),
                'type': tx_type,
            }
            normalized['hash'] = _row_hash({**normalized, '_raw': dict(row)})
            result.append(normalized)
        return result
