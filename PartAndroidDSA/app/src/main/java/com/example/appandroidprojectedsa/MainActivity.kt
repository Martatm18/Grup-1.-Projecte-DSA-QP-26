package com.example.appandroidprojectedsa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.appandroidprojectedsa.ui.navigation.AppNavigation
import com.example.appandroidprojectedsa.ui.theme.AppAndroidProjecteDSATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppAndroidProjecteDSATheme {
                AppNavigation()
            }
        }
    }
}
