from rest_framework_simplejwt.tokens import RefreshToken
from rest_framework_simplejwt.exceptions import TokenError

from .serializers import UserProfileSerializer


class UserService:

    @staticmethod
    def register(serializer) -> dict:
        """
        Saves a validated UserRegisterSerializer, generates JWT tokens,
        and returns the user profile + token pair.
        """
        user = serializer.save()
        refresh = RefreshToken.for_user(user)
        return {
            'user': UserProfileSerializer(user).data,
            'tokens': {
                'refresh': str(refresh),
                'access': str(refresh.access_token),
            },
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
