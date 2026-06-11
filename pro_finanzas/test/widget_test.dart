// Smoke test: boots the app and verifies the login screen is the initial
// route (no active session is restored from secure storage in tests).
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:pro_finanzas/main.dart';

void main() {
  testWidgets('App boots and lands on the login screen', (tester) async {
    await tester.pumpWidget(const ProFinancasApp());
    // Let the AuthProvider finish its async tryRestoreSession() check.
    await tester.pumpAndSettle();

    // The login screen has the brand title and a CTA button.
    expect(find.text('Profinancas'), findsOneWidget);
    expect(find.text('Enter Workspace →'), findsOneWidget);
  });
}
