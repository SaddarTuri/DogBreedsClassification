package com.saddar.dogbreeds

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.exifinterface.media.ExifInterface
import com.saddar.dogbreeds.ui.screens.MainScreen
import com.saddar.dogbreeds.ui.screens.SplashScreen
import com.saddar.dogbreeds.ui.theme.DogBreedsTheme
import com.saddar.dogbreeds.viewmodel.BreedViewModel
import java.io.File

enum class AppScreen { SPLASH, MAIN }

class MainActivity : ComponentActivity() {

    private val viewModel: BreedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            DogBreedsTheme {
                val uiState by viewModel.uiState.collectAsState()
                var currentScreen by rememberSaveable { mutableStateOf(AppScreen.SPLASH) }

                // URI for the current camera capture (kept in state so lambda can read it)
                var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }

                // ── Camera launcher (full-res + EXIF-corrected) ───────────
                val cameraLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.TakePicture()
                ) { success ->
                    if (success) {
                        cameraPhotoUri?.let { uri ->
                            loadBitmapFromUri(uri)?.let { bmp ->
                                viewModel.setBitmap(bmp)
                                currentScreen = AppScreen.MAIN
                            }
                        }
                    }
                }

                // ── Camera permission launcher ─────────────────────────────
                val cameraPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { granted ->
                    if (granted) {
                        cameraPhotoUri?.let { cameraLauncher.launch(it) }
                    }
                }

                // ── Gallery launcher ──────────────────────────────────────
                val galleryLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        loadBitmapFromUri(it)?.let { bmp -> viewModel.setBitmap(bmp) }
                    }
                }

                fun openCamera() {
                    // Create a fresh temp file every time
                    val photoFile = File(cacheDir, "camera_images/photo_${System.currentTimeMillis()}.jpg")
                        .also { it.parentFile?.mkdirs() }
                    val uri = FileProvider.getUriForFile(
                        this@MainActivity,
                        "${packageName}.fileprovider",
                        photoFile
                    )
                    cameraPhotoUri = uri

                    val hasPermission = ContextCompat.checkSelfPermission(
                        this@MainActivity, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) cameraLauncher.launch(uri)
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

    /**
     * Decode a bitmap from URI using BitmapFactory (no automatic EXIF orientation applied),
     * then correct rotation manually via fixExifRotation so there is no double-rotation on
     * Android P+ where ImageDecoder would have applied orientation automatically.
     */
    private fun loadBitmapFromUri(uri: Uri): Bitmap? = runCatching {
        val raw = contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        } ?: return@runCatching null
        fixExifRotation(uri, raw)
    }.getOrNull()

    /**
     * Read EXIF orientation tag from [uri] and rotate [bitmap] to match
     * the correct upright orientation.
     */
    private fun fixExifRotation(uri: Uri, bitmap: Bitmap): Bitmap {
        val degrees = runCatching {
            contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90  -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else                                 -> 0f
                }
            } ?: 0f
        }.getOrDefault(0f)

        if (degrees == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Dog Breeds AI")
            putExtra(Intent.EXTRA_TEXT, "Check out Dog Breeds — an AI-powered dog breed identifier! 🐕")
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }
}
