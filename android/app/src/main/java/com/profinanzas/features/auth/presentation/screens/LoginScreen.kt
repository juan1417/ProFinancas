package com.profinanzas.features.auth.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.profinanzas.core.ui.theme.*
import com.profinanzas.features.auth.presentation.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onLoginSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "ProFinanzas",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = NavyBlue,
        )
        Text(
            text = "Gestión financiera personal",
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral600,
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(8.dp))

        uiState.error?.let { error ->
            Text(text = error, color = ExpenseRed, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
            enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank(),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = SurfaceLight,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Iniciar Sesión", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("¿No tienes cuenta? ", color = Neutral600)
            TextButton(onClick = onNavigateToRegister) {
                Text("Regístrate", color = NavyBlue, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider(color = Neutral200)
        Spacer(modifier = Modifier.height(16.dp))

        Text("O inicia sesión con", color = Neutral600, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedButton(
                onClick = { /* TODO: biometric auth */ },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Touch ID")
            }
            OutlinedButton(
                onClick = { /* TODO: biometric auth */ },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Face ID")
            }
        }
    }
}
