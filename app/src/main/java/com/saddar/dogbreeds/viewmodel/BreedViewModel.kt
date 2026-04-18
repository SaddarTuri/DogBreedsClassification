package com.saddar.dogbreeds.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.saddar.dogbreeds.data.BreedResult
import com.saddar.dogbreeds.data.Classifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BreedUiState(
    val currentBitmap: Bitmap? = null,
    val detectionResult: BreedResult? = null,
    val isDetecting: Boolean = false,
    val isModelReady: Boolean = false,
    val statusMessage: String = "Select a dog photo to begin",
    val error: String? = null
)

class BreedViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BreedUiState())
    val uiState: StateFlow<BreedUiState> = _uiState.asStateFlow()

    private var classifier: Classifier? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                Classifier(application.assets, MODEL_PATH, LABELS_PATH, INPUT_SIZE)
            }.onSuccess { clf ->
                classifier = clf
                _uiState.update { it.copy(isModelReady = true) }
                loadDefaultImage()
            }.onFailure { e ->
                _uiState.update { it.copy(error = "Failed to load model: ${e.message}") }
            }
        }
    }

    private suspend fun loadDefaultImage() = withContext(Dispatchers.IO) {
        runCatching {
            getApplication<Application>().assets.open(SAMPLE_IMAGE).use { stream ->
                val raw = BitmapFactory.decodeStream(stream)
                scaleImage(raw)
            }
        }.onSuccess { bitmap ->
            _uiState.update { it.copy(currentBitmap = bitmap, statusMessage = "Tap 'Identify Breed' to classify") }
        }
    }

    fun setBitmap(bitmap: Bitmap) {
        val scaled = scaleImage(bitmap)
        _uiState.update {
            it.copy(
                currentBitmap = scaled,
                detectionResult = null,
                statusMessage = "Tap 'Identify Breed' to classify"
            )
        }
    }

    fun runDetection() {
        val bitmap = _uiState.value.currentBitmap ?: return
        val clf = classifier ?: return

        _uiState.update { it.copy(isDetecting = true, detectionResult = null) }

        viewModelScope.launch(Dispatchers.Default) {
            val top = clf.recognizeImage(bitmap).firstOrNull()
            val result = top?.let {
                BreedResult(
                    breedName = it.title
                        .replace("_", " ")
                        .split(" ")
                        .joinToString(" ") { w -> w.replaceFirstChar { c -> c.uppercase() } },
                    confidence = it.confidence,
                    traits = getTraits(it.title)
                )
            }
            _uiState.update {
                it.copy(
                    isDetecting = false,
                    detectionResult = result,
                    statusMessage = if (result == null) "No breed detected. Try a clearer image." else ""
                )
            }
        }
    }

    private fun scaleImage(bitmap: Bitmap): Bitmap {
        val scaleW = INPUT_SIZE.toFloat() / bitmap.width
        val scaleH = INPUT_SIZE.toFloat() / bitmap.height
        val matrix = Matrix().apply { postScale(scaleW, scaleH) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun getTraits(breed: String): List<String> =
        BREED_TRAITS[breed.lowercase().replace(" ", "_")] ?: listOf("Friendly", "Loyal", "Intelligent")

    companion object {
        private const val INPUT_SIZE = 224
        private const val MODEL_PATH = "saddar.tflite"
        private const val LABELS_PATH = "labels.txt"
        private const val SAMPLE_IMAGE = "dog1.png"

        private val BREED_TRAITS = mapOf(
            "akita"              to listOf("Loyal", "Dignified", "Protective"),
            "beagle"             to listOf("Friendly", "Curious", "Merry"),
            "boxer"              to listOf("Playful", "Bright", "Energetic"),
            "bulldog"            to listOf("Calm", "Courageous", "Friendly"),
            "chihuahua"          to listOf("Alert", "Sassy", "Devoted"),
            "doberman"           to listOf("Alert", "Fearless", "Loyal"),
            "golden_retriever"   to listOf("Friendly", "Reliable", "Gentle"),
            "husky"              to listOf("Athletic", "Alert", "Gentle"),
            "labrador"           to listOf("Friendly", "Active", "Outgoing"),
            "yorkshire_terrier"  to listOf("Affectionate", "Spirited", "Bold")
        )
    }
}
