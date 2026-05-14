package com.example.devpath

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
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
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.devpath.data.repository.LocalThemeRepository
import com.example.devpath.data.repository.ThemeRepository
import com.example.devpath.data.repository.YdbRepository
import com.example.devpath.ui.MainScreen
import com.example.devpath.ui.theme.DevPathTheme
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeRepository: ThemeRepository
    @Inject
    lateinit var ydbRepository: YdbRepository

    private lateinit var insetsController: WindowInsetsControllerCompat
    private lateinit var mapView: MapView

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            Toast.makeText(this, "Все разрешения получены", Toast.LENGTH_SHORT).show()
        } else {
            val deniedPermissions = permissions.filter { !it.value }.keys.joinToString()
            Toast.makeText(this, "Некоторые разрешения отклонены: $deniedPermissions", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        insetsController = WindowInsetsControllerCompat(window, window.decorView)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("DEBUG: onBackPressedDispatcher - перехвачено в MainActivity")
            }
        })

        mapView = MapView(this)

        lifecycleScope.launch {
            try {
                val usersOk = ydbRepository.initDatabase()
                val chatsOk = ydbRepository.initChatTables()

                if (usersOk && chatsOk) {
                    println("DEBUG: ✅ YDB база данных готова к работе")
                } else {
                    println("DEBUG: ⚠️ Частичная инициализация YDB")
                }
            } catch (e: Exception) {
                println("DEBUG: ❌ Ошибка инициализации YDB: ${e.message}")
            }
        }

        checkAndRequestAllPermissions()
        requestBatteryOptimizationIfNeeded()

        setContent {
            CompositionLocalProvider(LocalThemeRepository provides themeRepository) {
                DevPathTheme {
                    MainScreen(mapView = mapView)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        if (::mapView.isInitialized) {
            mapView.onStart()
        }
        println("DEBUG: MainActivity onStart - MapKit started")
    }

    override fun onStop() {
        if (::mapView.isInitialized) {
            mapView.onStop()
        }
        MapKitFactory.getInstance().onStop()
        super.onStop()
        println("DEBUG: MainActivity onStop - MapKit stopped")
    }

    override fun onDestroy() {
        if (::mapView.isInitialized) {
            mapView.onStop()
        }
        super.onDestroy()
        println("DEBUG: MainActivity onDestroy - Activity уничтожена")
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED)
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)

        if (permissionsToRequest.isNotEmpty()) {
            if (permissionsToRequest.contains(Manifest.permission.RECORD_AUDIO) && shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO))
                Toast.makeText(this, "Для голосового ввода необходимо разрешение на запись аудио", Toast.LENGTH_LONG).show()
            if (permissionsToRequest.contains(Manifest.permission.ACTIVITY_RECOGNITION) && shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION))
                Toast.makeText(this, "Для подсчёта шагов необходимо разрешение на физическую активность", Toast.LENGTH_LONG).show()
            requestMultiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun requestBatteryOptimizationIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }
}