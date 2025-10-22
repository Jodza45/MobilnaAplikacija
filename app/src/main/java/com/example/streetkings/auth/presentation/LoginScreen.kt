package com.example.streetkings.auth.presentation

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Prijavi se", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { authViewModel.loginUser(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Prijavi se")
        }
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Nemaš nalog? Registruj se",
            modifier = Modifier.clickable { onRegisterClick() },
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = authState) {
            is AuthState.Loading -> CircularProgressIndicator()
            is AuthState.Success -> {
                LaunchedEffect(Unit) {

                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    onLoginSuccess()
                }
            }
            is AuthState.Error -> Text(text = "Greška: ${state.message}", color = MaterialTheme.colorScheme.error)
            else -> {}
        }
    }
}