package com.example.devpath

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.content.ContextCompat
import com.example.devpath.data.repository.LocalThemeRepository
import com.example.devpath.data.repository.ThemeRepository
import com.example.devpath.ui.DevPathNavGraph
import com.example.devpath.ui.theme.DevPathTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeRepository: ThemeRepository

    // Регистрация для запроса нескольких разрешений
    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all {
            it.value
        }

        if (granted) {
            Toast.makeText(this, "Все разрешения получены", Toast.LENGTH_SHORT).show()
        } else {
            val deniedPermissions = permissions.filter { !it.value }.keys.joinToString()
            Toast.makeText(
                this,
                "Некоторые разрешения отклонены: $deniedPermissions",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Запрос всех необходимых разрешений
        checkAndRequestAllPermissions()

        setContent {
            CompositionLocalProvider(LocalThemeRepository provides themeRepository) {
                DevPathTheme {
                    DevPathNavGraph()
                }
            }
        }
    }

    private fun checkAndRequestAllPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Разрешение на запись аудио (для всех версий Android)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        // Разрешение на уведомления для Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Разрешение на запись в хранилище для старых версий Android
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        // Запрашиваем разрешения, если есть что запрашивать
        if (permissionsToRequest.isNotEmpty()) {
            // Показываем объяснение для аудио разрешения
            if (permissionsToRequest.contains(Manifest.permission.RECORD_AUDIO) &&
                shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
            ) {
                Toast.makeText(
                    this,
                    "Для голосового ввода необходимо разрешение на запись аудио",
                    Toast.LENGTH_LONG
                ).show()
            }

            requestMultiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}