package com.example.appandroidprojectedsa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Nota: Els colors CyberDark, CyberCyan, CyberGray, CyberGradient i CyberCard 
// ja estan definits a ShopScreen.kt dins del mateix package.

@Composable
fun NewUser(onLoginSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmedpswd by remember { mutableStateOf("") }

    val passwordsMatch = password == confirmedpswd && password.isNotEmpty()
    val showError = password.isNotEmpty() && confirmedpswd.isNotEmpty() && !passwordsMatch

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CyberDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            // Header: REGISTRE DE NOU USUARI
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(CyberCyan, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "REGISTRE DE NOU USUARI",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "CREAR\nPERFIL",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 44.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Text(
                text = "Introdueix les teves credencials per al Protocol SIGMA",
                color = CyberGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Input Fields
            CyberTextFieldNew(
                label = "USUARI",
                value = username,
                onValueChange = { username = it },
                placeholder = "nom.usuari"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CyberTextFieldNew(
                label = "CONTRASENYA",
                value = password,
                onValueChange = { password = it },
                placeholder = "nova contrasenya",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            CyberTextFieldNew(
                label = "REPETIR CONTRASENYA",
                value = confirmedpswd,
                onValueChange = { confirmedpswd = it },
                placeholder = "confirma la contrasenya",
                isPassword = true,
                isError = showError
            )

            if (showError) {
                Text(
                    text = "Les contrasenyes no coincideixen",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Action Button
            Button(
                onClick = onLoginSuccess,
                enabled = passwordsMatch && username.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        if (passwordsMatch && username.isNotEmpty()) CyberGradient else Brush.linearGradient(listOf(Color.DarkGray, Color.Gray)), 
                        RoundedCornerShape(4.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "CREAR USUARI",
                    color = if (passwordsMatch && username.isNotEmpty()) CyberDark else Color.LightGray,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Informació de seguretat
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, CyberGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .background(CyberCard.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "SEGURETAT DE LA TERMINAL",
                        color = Color(0xFF00F5D4), // Green accent
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "La teva contrasenya s'encriptarà seguint els protocols de seguretat de la EETAC. No la comparteixis.",
                        color = CyberGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CyberTextFieldNew(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    isError: Boolean = false
) {
    Column {
        Text(
            text = label,
            color = if (isError) MaterialTheme.colorScheme.error else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 0.5.dp, 
                    color = if (isError) MaterialTheme.colorScheme.error else Color(0xFF1E293B), 
                    shape = RoundedCornerShape(4.dp)
                ),
            placeholder = { Text(placeholder, color = Color(0xFF334155), fontSize = 14.sp) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CyberCard,
                unfocusedContainerColor = CyberCard,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = CyberCyan,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                errorContainerColor = CyberCard,
                errorIndicatorColor = Color.Transparent
            ),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            shape = RoundedCornerShape(4.dp),
            singleLine = true,
            isError = isError
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NewUserPreview() {
    NewUser(onLoginSuccess = {})
}
