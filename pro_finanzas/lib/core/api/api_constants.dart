class ApiConstants {
  ApiConstants._();

  // Use 10.0.2.2 for Android emulator, localhost for web/iOS simulator
  static const String baseUrl = 'http://10.0.2.2:8000/api';

  // Auth
  static const String register = '/auth/register/';
  static const String login = '/auth/login/';
  static const String logout = '/auth/logout/';
  static const String profile = '/auth/profile/';
  static const String changePassword = '/auth/change-password/';
  static const String tokenRefresh = '/auth/token/refresh/';

  // Categories
  static const String categories = '/categories/';
  static const String categoriesByType = '/categories/by_type/';

  // Transactions
  static const String transactions = '/transactions/';
  static const String transactionsSummary = '/transactions/summary/';
  static const String transactionsByCategory = '/transactions/by_category/';
}
