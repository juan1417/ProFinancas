class DomainError(Exception):
    """Error de una regla de negocio.

    `field` permite que la capa HTTP devuelva el error asociado al campo que lo
    provocó; si es ``None`` el error es de la entidad completa.
    """

    def __init__(self, message: str, field: str | None = None):
        self.message = message
        self.field = field
        super().__init__(message)
