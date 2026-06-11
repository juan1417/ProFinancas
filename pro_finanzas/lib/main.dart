import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'core/api/api_client.dart';
import 'core/theme/app_theme.dart';
import 'core/shell/main_shell.dart';
import 'features/auth/data/datasources/auth_remote_datasource.dart';
import 'features/auth/data/repositories/auth_repository_impl.dart';
import 'features/auth/domain/usecases/login_usecase.dart';
import 'features/auth/domain/usecases/register_usecase.dart';
import 'features/auth/domain/usecases/logout_usecase.dart';
import 'features/auth/presentation/providers/auth_provider.dart';
import 'features/auth/presentation/screens/login_screen.dart';
import 'features/transactions/data/datasources/transaction_remote_datasource.dart';
import 'features/transactions/data/repositories/transaction_repository_impl.dart';
import 'features/transactions/domain/usecases/get_transactions_usecase.dart';
import 'features/transactions/domain/usecases/create_transaction_usecase.dart';
import 'features/transactions/domain/usecases/get_summary_usecase.dart';
import 'features/transactions/presentation/providers/transaction_provider.dart';
import 'features/wallet/presentation/providers/cards_provider.dart';

void main() {
  runApp(const ProFinancasApp());
}

class ProFinancasApp extends StatelessWidget {
  const ProFinancasApp({super.key});

  @override
  Widget build(BuildContext context) {
    final apiClient = ApiClient.instance;

    final authRepo =
        AuthRepositoryImpl(AuthRemoteDatasource(apiClient));
    final txRepo =
        TransactionRepositoryImpl(TransactionRemoteDatasource(apiClient));

    return MultiProvider(
      providers: [
        ChangeNotifierProvider(
          create: (_) => AuthProvider(
            loginUseCase: LoginUseCase(authRepo),
            registerUseCase: RegisterUseCase(authRepo),
            logoutUseCase: LogoutUseCase(authRepo),
          ),
        ),
        ChangeNotifierProvider(
          create: (_) => TransactionProvider(
            getTransactions: GetTransactionsUseCase(txRepo),
            createTransaction: CreateTransactionUseCase(txRepo),
            getSummary: GetSummaryUseCase(txRepo),
            repository: txRepo,
          ),
        ),
        ChangeNotifierProvider(
          create: (_) => CardsProvider()..load(),
        ),
      ],
      child: MaterialApp(
        title: 'ProFinancas',
        debugShowCheckedModeBanner: false,
        theme: AppTheme.light,
        home: const _AuthGate(),
      ),
    );
  }
}

class _AuthGate extends StatelessWidget {
  const _AuthGate();

  @override
  Widget build(BuildContext context) {
    final isAuthenticated = context.watch<AuthProvider>().isAuthenticated;
    return isAuthenticated ? const MainShell() : const LoginScreen();
  }
}
