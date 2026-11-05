"""Puerto de ingesta bancaria."""
from typing import Protocol, List, Dict, Any


class BankStatementImporter(Protocol):
    """Parsea un archivo de extracto y devuelve movimientos normalizados."""

    def parse(self, file_obj) -> List[Dict[str, Any]]:
        """Devuelve una lista de dicts con claves:
        date, description, amount (Decimal str), type (INCOME/EXPENSE), hash.
        """
        ...
