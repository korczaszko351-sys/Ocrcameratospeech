package pl.example.cameratotextspeech

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer

class OcrAnalyzer(
    private val recognizer: TextRecognizer,
    private val onTextRecognized: (String) -> Unit
) : ImageAnalysis.Analyzer {
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        recognizer.process(image)
            .addOnSuccessListener { onTextRecognized(it.text) }
            .addOnFailureListener { onTextRecognized("") }
            .addOnCompleteListener { imageProxy.close() }
    }
}
