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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Estils de colors (coherents amb la ShopScreen)
private val LoginDark = Color(0xFF060D15)
private val LoginTerminalBg = Color(0xFF0A1929)
private val LoginCyan = Color(0xFF00E5FF)
private val LoginGreen = Color(0xFF00F5D4)
private val LoginGray = Color(0xFF94A3B8)
private val LoginGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF00B4D8), Color(0xFF00F5D4))
)

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onRegisterClick: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LoginDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            // Header: SISTEMA UPC/EETAC
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(LoginGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SISTEMA UPC/EETAC",
                    color = LoginGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "TERMINAL ACADEMICA CLASIFICADA",
                color = LoginCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "PROTOCOLO\nSIGMA",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 44.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Text(
                text = "Acceso restringido al sistema",
                color = LoginGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Terminal Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                    .background(LoginTerminalBg, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Column {
                    TerminalLine("> init sigma.auth")
                    TerminalLine("> validando credenciales EETAC...")
                    TerminalLine("> moneda detectada: ECTS")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login / Registro Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { /* Ja estem a login */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(LoginGradient, RoundedCornerShape(4.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("LOGIN", color = LoginDark, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onRegisterClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(4.dp),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = SolidColor(Color(0xFF1E293B))),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("REGISTRO", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Input Fields
            CyberTextField(
                label = "USUARIO",
                value = username,
                onValueChange = { username = it },
                placeholder = "id.alumno"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CyberTextField(
                label = "PASSWORD",
                value = password,
                onValueChange = { password = it },
                placeholder = "clave de acceso",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            Button(
                onClick = onLoginSuccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(LoginGradient, RoundedCornerShape(4.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "ENTRAR AL SISTEMA",
                    color = LoginDark,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, LoginGreen.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Cuenta creada. Ya estas dentro.",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun TerminalLine(text: String) {
    Text(
        text = text,
        color = LoginGreen,
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
fun CyberTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    Column {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, Color(0xFF1E293B), RoundedCornerShape(4.dp)),
            placeholder = { Text(placeholder, color = Color(0xFF334155), fontSize = 14.sp) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = LoginTerminalBg,
                unfocusedContainerColor = LoginTerminalBg,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = LoginCyan,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            shape = RoundedCornerShape(4.dp),
            singleLine = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(onLoginSuccess = {}, onRegisterClick = {})
}
