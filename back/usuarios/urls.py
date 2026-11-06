from django.urls import path

from .views import (
    RegisterView, LoginView, LogoutView, ProfileView,
    ChangePasswordView, ThrottledTokenRefreshView, app_version,
)

urlpatterns = [
    path('app/version/', app_version, name='app-version'),
    path('register/', RegisterView.as_view(), name='auth-register'),
    path('login/', LoginView.as_view(), name='auth-login'),
    path('logout/', LogoutView.as_view(), name='auth-logout'),
    path('token/refresh/', ThrottledTokenRefreshView.as_view(), name='auth-token-refresh'),
    path('profile/', ProfileView.as_view(), name='auth-profile'),
    path('change-password/', ChangePasswordView.as_view(), name='auth-change-password'),
]
