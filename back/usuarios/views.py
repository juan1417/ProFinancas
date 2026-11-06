import logging
import os

from rest_framework import generics, permissions, status
from rest_framework.decorators import api_view, permission_classes
from rest_framework.response import Response
from rest_framework.throttling import AnonRateThrottle, UserRateThrottle
from rest_framework.views import APIView
from rest_framework_simplejwt.views import TokenObtainPairView, TokenRefreshView

from .serializers import (
    EmailTokenObtainPairSerializer,
    UserRegisterSerializer,
    UserProfileSerializer,
    ChangePasswordSerializer,
)
from .services import UserService, get_client_ip

logger = logging.getLogger('profinanzas.security')


class _RegisterThrottle(AnonRateThrottle):
    scope = 'register'


class _LoginThrottle(AnonRateThrottle):
    scope = 'login'


class _TokenRefreshThrottle(AnonRateThrottle):
    scope = 'refresh'


class _ChangePasswordThrottle(UserRateThrottle):
    scope = 'change_password'


class _LogoutThrottle(UserRateThrottle):
    scope = 'logout'


class RegisterView(generics.CreateAPIView):
    """
    POST /api/auth/register/
    Registro público de nuevos usuarios. Devuelve tokens JWT al registrarse.
    """
    serializer_class = UserRegisterSerializer
    permission_classes = [permissions.AllowAny]
    throttle_classes = [_RegisterThrottle]

    def create(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        data = UserService.register(serializer)
        logger.info(
            'REGISTER success email=%s ip=%s',
            request.data.get('email', ''),
            get_client_ip(request),
        )
        return Response(data, status=status.HTTP_201_CREATED)


class LoginView(TokenObtainPairView):
    """
    POST /api/auth/login/
    Autenticación con email y contraseña. Devuelve par de tokens JWT
    y el perfil del usuario.

    El response tiene la misma forma que /auth/register/:
      { user: {...}, access: str, refresh: str }
    para que el cliente Flutter pueda parsear ambos endpoints
    con un solo decoder.
    """
    serializer_class = EmailTokenObtainPairSerializer
    permission_classes = [permissions.AllowAny]
    throttle_classes = [_LoginThrottle]

    def post(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        try:
            serializer.is_valid(raise_exception=True)
        except Exception:
            logger.warning(
                'LOGIN failed email=%s ip=%s',
                request.data.get('email', ''),
                get_client_ip(request),
            )
            raise
        logger.info(
            'LOGIN success email=%s ip=%s',
            serializer.user.email,
            get_client_ip(request),
        )
        return Response({
            'user': UserProfileSerializer(serializer.user).data,
            'access': serializer.validated_data['access'],
            'refresh': serializer.validated_data['refresh'],
        }, status=status.HTTP_200_OK)


class LogoutView(APIView):
    """
    POST /api/auth/logout/
    Invalida el refresh token (blacklist). Requiere autenticación.
    """
    permission_classes = [permissions.IsAuthenticated]
    throttle_classes = [_LogoutThrottle]

    def post(self, request):
        try:
            UserService.logout(request.data.get('refresh'))
            logger.info(
                'LOGOUT user=%s ip=%s',
                request.user.email,
                get_client_ip(request),
            )
            return Response({'detail': 'Sesión cerrada correctamente.'}, status=status.HTTP_200_OK)
        except ValueError as e:
            return Response({'detail': str(e)}, status=status.HTTP_400_BAD_REQUEST)


class ProfileView(generics.RetrieveUpdateAPIView):
    """
    GET  /api/auth/profile/  — Ver perfil del usuario autenticado.
    PATCH /api/auth/profile/ — Actualizar first_name, last_name, username.
    """
    serializer_class = UserProfileSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_object(self):
        return self.request.user


class ChangePasswordView(generics.UpdateAPIView):
    """
    PUT/PATCH /api/auth/change-password/
    Cambia la contraseña del usuario autenticado.
    """
    serializer_class = ChangePasswordSerializer
    permission_classes = [permissions.IsAuthenticated]
    throttle_classes = [_ChangePasswordThrottle]

    def get_object(self):
        return self.request.user

    def update(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        serializer.save()
        logger.info(
            'CHANGE_PASSWORD user=%s ip=%s',
            request.user.email,
            get_client_ip(request),
        )
        return Response({'detail': 'Contraseña actualizada correctamente.'}, status=status.HTTP_200_OK)


class ThrottledTokenRefreshView(TokenRefreshView):
    """
    POST /api/auth/token/refresh/
    Renueva el access token. Throttled para prevenir abuso.
    """
    throttle_classes = [_TokenRefreshThrottle]


@api_view(['GET'])
@permission_classes([AllowAny])
def app_version(request):
    """Endpoint de versionado para actualizaciones OTA de los clientes."""
    return Response({
        'latest_version': os.getenv('APP_LATEST_VERSION', '1.0.0'),
        'min_version': os.getenv('APP_MIN_VERSION', '1.0.0'),
        'download_url_android': os.getenv('APP_DOWNLOAD_ANDROID', ''),
        'download_url_desktop': os.getenv('APP_DOWNLOAD_DESKTOP', ''),
        'changelog': os.getenv('APP_CHANGELOG', ''),
        'force_update': os.getenv('APP_FORCE_UPDATE', 'false').lower() == 'true',
    })
