from rest_framework_simplejwt.tokens import RefreshToken
from rest_framework_simplejwt.exceptions import TokenError

from .serializers import UserProfileSerializer


def get_client_ip(request) -> str:
    xff = request.META.get('HTTP_X_FORWARDED_FOR')
    return xff.split(',')[0].strip() if xff else request.META.get('REMOTE_ADDR')


class UserService:

    @staticmethod
    def register(serializer) -> dict:
        """
        Saves a validated UserRegisterSerializer, generates JWT tokens,
        and returns the user profile + token pair.

        Response shape matches /api/auth/login/:
            { user: {...}, access: str, refresh: str }
        """
        user = serializer.save()
        refresh = RefreshToken.for_user(user)
        return {
            'user': UserProfileSerializer(user).data,
            'access': str(refresh.access_token),
            'refresh': str(refresh),
        }

    @staticmethod
    def logout(refresh_token: str) -> None:
        """
        Blacklists the given refresh token.
        Raises ValueError if the token is missing or invalid/expired.
        """
        if not refresh_token:
            raise ValueError('El campo "refresh" es requerido.')
        try:
            RefreshToken(refresh_token).blacklist()
        except TokenError:
            raise ValueError('Token inválido o ya expirado.')
