package com.example.appandroidprojectedsa.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appandroidprojectedsa.models.ShopItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(onLogout: () -> Unit) {
    val items = listOf(
        ShopItem(1, "carga_EMP", "sirve para desactivar drones temporalmente", 4.0),
        ShopItem(2, "usb_amarillo", "si se enchufa a cierto ordenador, sigma puede dar fragmento_codigo2", 4.0),
        ShopItem(3, "tarjeta_temporal", "sirve para acceder a algunos despachos", 2.0),
        ShopItem(4, "botiquin", "sube hasta un 50% de vida", 2.0),
        ShopItem(5, "bateria_seguridad", "necesaria para abrir el laboratorio restringido", 3.0),
        ShopItem(6, "mapa_EETAC_ampliado", "se pueden ver zonas ocultas de la EETAC", 1.0)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Botiga d'Objectes") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Sortir", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { item ->
                ItemCard(item = item)
            }
        }
    }
}

@Composable
fun ItemCard(item: ShopItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleLarge)
                Text(text = item.description, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "${item.price.toInt()} ECTS",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Button(onClick = { /* Lògica de compra */ }) {
                Text("Comprar")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShopScreenPreview() {
    ShopScreen(onLogout = {})
}
