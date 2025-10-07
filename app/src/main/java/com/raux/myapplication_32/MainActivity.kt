package com.raux.myapplication_32

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raux.myapplication_32.ui.components.CameraPermissionHandler
import com.raux.myapplication_32.ui.screens.MainScreen
import com.raux.myapplication_32.ui.theme.MyApplication_32Theme
import com.raux.myapplication_32.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplication_32Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ASCIIFilterApp()
                }
            }
        }
    }
}

@Composable
fun ASCIIFilterApp() {
    val viewModel: MainViewModel = viewModel()
    
    MainScreen(
        viewModel = viewModel
    )
}