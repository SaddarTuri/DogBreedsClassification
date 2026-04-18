package com.saddar.dogbreeds.data

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.PriorityQueue

class Classifier(
    assetManager: AssetManager,
    modelPath: String,
    labelPath: String,
    inputSize: Int
) {
    private val interpreter: Interpreter
    private val labelList: List<String>
    private val inputSize: Int = inputSize
    private val pixelSize = 3
    private val imageMean = 0
    private val imageStd = 255.0f
    private val maxResults = 3
    private val threshold = 0.4f

    data class Recognition(
        val id: String = "",
        val title: String = "",
        val confidence: Float = 0f
    )

    init {
        interpreter = Interpreter(loadModelFile(assetManager, modelPath))
        labelList = loadLabelList(assetManager, labelPath)
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    private fun loadLabelList(assetManager: AssetManager, labelPath: String): List<String> =
        assetManager.open(labelPath).bufferedReader().useLines { it.toList() }

    fun recognizeImage(bitmap: Bitmap): List<Recognition> {
        val scaled = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, false)
        val byteBuffer = convertBitmapToByteBuffer(scaled)
        val result = Array(1) { FloatArray(labelList.size) }
        interpreter.run(byteBuffer, result)
        return getSortedResult(result)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * pixelSize)
        buffer.order(ByteOrder.nativeOrder())
        val pixels = IntArray(inputSize * inputSize)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var idx = 0
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val v = pixels[idx++]
                buffer.putFloat(((v shr 16 and 0xFF) - imageMean) / imageStd)
                buffer.putFloat(((v shr 8 and 0xFF) - imageMean) / imageStd)
                buffer.putFloat(((v and 0xFF) - imageMean) / imageStd)
            }
        }
        return buffer
    }

    private fun getSortedResult(probArray: Array<FloatArray>): List<Recognition> {
        Log.d("Classifier", "Outputs: ${probArray[0].size}, Labels: ${labelList.size}")
        val pq = PriorityQueue<Recognition>(maxResults) { a, b ->
            b.confidence.compareTo(a.confidence)
        }
        for (i in labelList.indices) {
            val confidence = probArray[0][i]
            if (confidence >= threshold) {
                pq.add(Recognition("$i", if (i < labelList.size) labelList[i] else "Unknown", confidence))
            }
        }
        return buildList {
            repeat(minOf(pq.size, maxResults)) { pq.poll()?.let { add(it) } }
        }
    }
}
