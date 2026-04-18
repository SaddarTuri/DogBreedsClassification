package com.saddar.dogbreeds

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.saddar.dogbreeds.ui.screens.MainScreen
import com.saddar.dogbreeds.ui.screens.SplashScreen
import com.saddar.dogbreeds.ui.theme.CanineIntelTheme
import com.saddar.dogbreeds.viewmodel.BreedViewModel

enum class AppScreen { SPLASH, MAIN }

class MainActivity : ComponentActivity() {

    private val viewModel: BreedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge rendering
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            CanineIntelTheme {
                val uiState by viewModel.uiState.collectAsState()
                var currentScreen by rememberSaveable { mutableStateOf(AppScreen.SPLASH) }

                // ── Camera launcher (thumbnail preview) ───────────────────
                val cameraLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.TakePicturePreview()
                ) { bitmap: Bitmap? ->
                    bitmap?.let { viewModel.setBitmap(it) }
                    if (bitmap != null) currentScreen = AppScreen.MAIN
                }

                // ── Camera permission launcher ─────────────────────────────
                val cameraPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { granted ->
                    if (granted) cameraLauncher.launch(null)
                }

                // ── Gallery launcher ──────────────────────────────────────
                val galleryLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        val bitmap = loadBitmapFromUri(it)
                        bitmap?.let { bmp -> viewModel.setBitmap(bmp) }
                    }
                }

                fun openCamera() {
                    val hasPerm = ContextCompat.checkSelfPermission(
                        this@MainActivity, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPerm) cameraLauncher.launch(null)
                    else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }

                fun openGallery() {
                    galleryLauncher.launch("image/*")
                }

                // ── Screen routing ────────────────────────────────────────
                when (currentScreen) {
                    AppScreen.SPLASH -> SplashScreen(
                        isModelReady = uiState.isModelReady,
                        onBeginCurating = { currentScreen = AppScreen.MAIN }
                    )
                    AppScreen.MAIN -> MainScreen(
                        uiState = uiState,
                        onCameraClick = { openCamera() },
                        onGalleryClick = { openGallery() },
                        onDetectClick = { viewModel.runDetection() },
                        onShareClick = { shareApp() }
                    )
                }
            }
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri)) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    }.getOrNull()

    private fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Canine Intel")
            putExtra(Intent.EXTRA_TEXT, "Check out Canine Intel — an AI-powered dog breed identifier! 🐕")
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }
}
