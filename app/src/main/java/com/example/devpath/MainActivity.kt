package com.example.devpath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.example.devpath.data.repository.LocalThemeRepository
import com.example.devpath.data.repository.ThemeRepository
import com.example.devpath.ui.DevPathNavGraph
import com.example.devpath.ui.theme.DevPathTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint // ОБЯЗАТЕЛЬНО!
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeRepository: ThemeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CompositionLocalProvider(LocalThemeRepository provides themeRepository) {
                DevPathTheme {
                    DevPathNavGraph()
                }
            }
        }
    }
}