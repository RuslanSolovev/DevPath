package com.example.devpath

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.devpath.data.repository.LocalThemeRepository
import com.example.devpath.data.repository.ThemeRepository
import com.example.devpath.ui.MainScreen
import com.example.devpath.ui.theme.DevPathTheme
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeRepository: ThemeRepository

    private lateinit var insetsController: WindowInsetsControllerCompat

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }

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
        // 🔥 Инициализация Firebase App Check ДО super.onCreate()
        try {
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
            println("DEBUG: Firebase App Check initialized successfully")
        } catch (e: Exception) {
            println("DEBUG: Firebase App Check initialization failed: ${e.message}")
            // Продолжаем работу даже если App Check не инициализировался
        }

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        insetsController = WindowInsetsControllerCompat(window, window.decorView)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("DEBUG: onBackPressedDispatcher - перехвачено в MainActivity")
            }
        })

        if (savedInstanceState != null) {
            println("DEBUG: MainActivity onCreate - восстановление после поворота/сворачивания")
        } else {
            println("DEBUG: MainActivity onCreate - первый запуск")
        }

        checkAndRequestAllPermissions()

        setContent {
            CompositionLocalProvider(LocalThemeRepository provides themeRepository) {
                DevPathTheme {
                    MainScreen()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        println("DEBUG: MainActivity onStart - MapKit started")
    }

    override fun onStop() {
        MapKitFactory.getInstance().onStop()
        super.onStop()
        println("DEBUG: MainActivity onStop - MapKit stopped")
    }

    override fun onResume() {
        super.onResume()
        println("DEBUG: MainActivity onResume")
    }

    override fun onPause() {
        super.onPause()
        println("DEBUG: MainActivity onPause")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        println("DEBUG: MainActivity onSaveInstanceState - сохраняем состояние")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        println("DEBUG: MainActivity onRestoreInstanceState - восстанавливаем состояние")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("DEBUG: MainActivity onDestroy - Activity уничтожена")
    }

    fun setFullScreen(enabled: Boolean) {
        val decorView = window.decorView

        if (enabled) {
            decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    private fun checkAndRequestAllPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
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