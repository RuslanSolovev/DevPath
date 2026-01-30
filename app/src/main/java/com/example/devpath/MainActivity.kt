package com.example.devpath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.devpath.ui.DevPathNavGraph
import com.example.devpath.ui.theme.DevPathTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            DevPathTheme {
                DevPathNavGraph()
            }
        }
    }

}