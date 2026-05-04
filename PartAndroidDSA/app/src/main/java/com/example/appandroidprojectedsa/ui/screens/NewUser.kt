package com.example.appandroidprojectedsa.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun NewUser(onLoginSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmedpswd by remember { mutableStateOf("") }


    val passwordsMatch = password == confirmedpswd && password.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Nou Usuari", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Usuari") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contrasenya") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = password.isNotEmpty() && confirmedpswd.isNotEmpty() && !passwordsMatch
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmedpswd,
            onValueChange = { confirmedpswd = it },
            label = { Text("Repetir Contrasenya") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = password.isNotEmpty() && confirmedpswd.isNotEmpty() && !passwordsMatch,
            supportingText = {
                if (password.isNotEmpty() && confirmedpswd.isNotEmpty() && !passwordsMatch) {
                    Text(text = "Les contrasenyes no coincideixen", color = MaterialTheme.colorScheme.error)
                }
            }
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLoginSuccess,
            modifier = Modifier.fillMaxWidth(),
            enabled = passwordsMatch && username.isNotEmpty()
        ) {
            Text("Crear usuari")
        }


    }
}

@Preview(showBackground = true)
@Composable
fun NewUserPreview() {
    NewUser(onLoginSuccess = {})
}
