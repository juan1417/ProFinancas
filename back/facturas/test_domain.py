"""Tests puros de domain/ de facturas (sin DB)."""
import datetime
import pytest

from facturas.domain import numbering, status


def test_format_invoice_number():
    assert numbering.format_invoice_number(1) == 'FAC-001'
    assert numbering.format_invoice_number(42) == 'FAC-042'


def test_parse_invoice_number():
    assert numbering.parse_invoice_number('FAC-007') == 7
    assert numbering.parse_invoice_number('nope') is None
    assert numbering.parse_invoice_number(None) is None


def test_next_sequence_continues_from_last():
    assert numbering.next_sequence('FAC-003', 3) == 4


def test_next_sequence_falls_back_to_count():
    assert numbering.next_sequence('garbage', 5) == 6
    assert numbering.next_sequence(None, 2) == 3


def test_effective_status_overdue():
    today = datetime.date(2026, 11, 5)
    assert status.effective_status('PENDING', datetime.date(2026, 1, 1), today) == 'OVERDUE'
    assert status.effective_status('PENDING', datetime.date(2026, 12, 1), today) == 'PENDING'
    assert status.effective_status('PAID', datetime.date(2026, 1, 1), today) == 'PAID'
