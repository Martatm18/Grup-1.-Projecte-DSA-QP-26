package com.example.appandroidprojectedsa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appandroidprojectedsa.models.ShopItem

// Estils de colors segons la imatge
val CyberDark = Color(0xFF060D15)
val CyberCard = Color(0xFF0A1929)
val CyberCyan = Color(0xFF00E5FF)
val CyberGray = Color(0xFF94A3B8)
val CyberGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF00B4D8), Color(0xFF00F5D4))
)

@Composable
fun ShopScreen(onLogout: () -> Unit) {
    val items = listOf(
        ShopItem(1, "carga_EMP", "Desactiva drones temporalmente", 4.0),
        ShopItem(2, "usb_amarillo", "Obtiene fragmento_codigo2", 4.0),
        ShopItem(3, "tarjeta_temporal", "Acceso a despachos", 2.0),
        ShopItem(4, "botiquin", "Sube 50% de vida", 2.0),
        ShopItem(5, "bateria_security", "Para abrir laboratorio", 3.0),
        ShopItem(6, "mapa_ampliado", "Ver zonas ocultas", 1.0)
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CyberDark
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item(span = { GridItemSpan(2) }) {
                HeaderSection(onLogout)
            }

            // Tabs/Filters
            item(span = { GridItemSpan(2) }) {
                TabsSection()
            }

            // Shop Items
            items(items) { item ->
                ItemCardCyber(item)
            }

            // Footer / Inventari
            item(span = { GridItemSpan(2) }) {
                InventorySection()
            }
        }
    }
}

@Composable
fun HeaderSection(onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = "INVENTARIO DESBLOQUEABLE",
                color = CyberCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tienda SIGMA",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "carla - 100 ECTS",
                color = CyberGray,
                fontSize = 14.sp
            )
        }
        
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                .height(36.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("SALIR", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TabsSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val tabs = listOf("MISIONES", "PROFESORES", "TERMINALES", "ECTS")
        tabs.forEach { tab ->
            OutlinedButton(
                onClick = { },
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp),
                shape = RoundedCornerShape(4.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 0.5.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberGray)
            ) {
                Text(tab, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ItemCardCyber(item: ShopItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
            .background(CyberCard, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "OBJ-${item.id}",
                color = CyberCyan,
                fontSize = 8.sp,
                modifier = Modifier
                    .border(0.5.dp, CyberCyan, RoundedCornerShape(2.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.description,
                color = CyberGray,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                minLines = 2,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${item.price.toInt()} ECTS",
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                
                Button(
                    onClick = { /* Compra */ },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(28.dp)
                        .background(CyberGradient, RoundedCornerShape(4.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("COMPRAR", color = CyberDark, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun InventorySection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 16.dp)
    ) {
        HorizontalDivider(color = Color(0xFF1E293B), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "OBJETOS RECUPERADOS",
            color = CyberCyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Inventario",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Compra objetos para desbloquear nuevas pistas.",
            color = CyberGray,
            fontSize = 13.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ShopScreenPreview() {
    ShopScreen(onLogout = {})
}
