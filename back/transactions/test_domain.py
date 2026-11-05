"""Tests puros de domain/ (sin DB, sin settings)."""
import datetime
import pytest
from decimal import Decimal
from types import SimpleNamespace

from transactions.domain import rules
from transactions.domain.errors import DomainError
from transactions.domain.dates import parse_day, day_bounds


def test_validate_amount_rejects_zero():
    with pytest.raises(DomainError, match='mayor que cero'):
        rules.validate_amount(0)


def test_validate_amount_returns_decimal():
    assert rules.validate_amount('12.34') == Decimal('12.34')


def test_validate_description_too_short():
    with pytest.raises(DomainError, match='al menos 3 caracteres'):
        rules.validate_description('ab')


def test_validate_transaction_date_future():
    now = datetime.datetime(2026, 1, 1, tzinfo=datetime.timezone.utc)
    future = now + datetime.timedelta(days=1)
    with pytest.raises(DomainError, match='no puede ser futura'):
        rules.validate_transaction_date(future, now)


def test_validate_category_assignment_type_mismatch():
    cat = SimpleNamespace(type='EXPENSE', is_active=True, user_id=1, get_type_display=lambda: 'Gasto')
    with pytest.raises(DomainError, match='coincidir'):
        rules.validate_category_assignment(cat, 'INCOME', SimpleNamespace(id=1))


def test_validate_category_assignment_foreign_user():
    cat = SimpleNamespace(type='EXPENSE', is_active=True, user_id=2)
    with pytest.raises(DomainError, match='pertenecer al usuario'):
        rules.validate_category_assignment(cat, 'EXPENSE', SimpleNamespace(id=1))


def test_validate_category_assignment_inactive():
    cat = SimpleNamespace(type='EXPENSE', is_active=False, user_id=1)
    with pytest.raises(DomainError, match='inactiva'):
        rules.validate_category_assignment(cat, 'EXPENSE', SimpleNamespace(id=1))


def test_parse_day_accepts_str_and_date():
    assert parse_day('2026-07-15') == datetime.date(2026, 7, 15)
    assert parse_day(datetime.date(2026, 7, 15)) == datetime.date(2026, 7, 15)


def test_parse_day_rejects_garbage():
    with pytest.raises(DomainError):
        parse_day('not-a-date')


def test_day_bounds_inclusive():
    assert day_bounds('2026-07-15', end_of_day=False).hour == 0
    assert day_bounds('2026-07-15', end_of_day=True).hour == 23
    assert day_bounds('2026-07-15', end_of_day=True).microsecond == 999999
