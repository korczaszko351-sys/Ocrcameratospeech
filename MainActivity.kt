package pl.example.cameratotextspeech

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import pl.example.cameratotextspeech.databinding.ActivityMainBinding
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityMainBinding
    private val executor = Executors.newSingleThreadExecutor()
    private val recognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var currentText = ""
    private var candidate = ""
    private var candidateSince = 0L
    private var lastSpoken = ""
    private var lastSpokenAt = 0L

    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startCamera() else toast("Do działania potrzebny jest dostęp do kamery.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tts = TextToSpeech(this, this)
        binding.readButton.setOnClickListener { speak(currentText) }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) startCamera()
        else cameraPermission.launch(Manifest.permission.CAMERA)
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) { toast("Nie udało się uruchomić syntezy mowy."); return }
        val result = tts?.setLanguage(Locale("pl", "PL")) ?: TextToSpeech.ERROR
        ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        if (!ttsReady) toast("Zainstaluj polski głos TTS w ustawieniach telefonu.")
        tts?.setSpeechRate(0.92f)
    }

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            val preview = Preview.Builder().build().also { it.surfaceProvider = binding.previewView.surfaceProvider }
            val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
            analysis.setAnalyzer(executor, OcrAnalyzer(recognizer) { onText(it) })
            try {
                providerFuture.get().unbindAll()
                providerFuture.get().bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
            } catch (e: Exception) { toast("Nie udało się uruchomić kamery: ${e.message}") }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun onText(raw: String) = runOnUiThread {
        currentText = raw
        binding.recognizedText.text = raw.ifBlank { "Nie wykryto tekstu" }
        if (binding.autoReadSwitch.isChecked) speakStable(raw)
    }

    private fun speakStable(raw: String) {
        val text = clean(raw)
        if (text.length < 3) return
        val now = System.currentTimeMillis()
        if (text != candidate) { candidate = text; candidateSince = now; return }
        val stable = now - candidateSince >= 900
        val newText = text != lastSpoken
        val pause = now - lastSpokenAt >= 2500
        if (stable && newText && pause) { speak(text); lastSpoken = text; lastSpokenAt = now }
    }

    private fun speak(raw: String) {
        val text = clean(raw)
        if (ttsReady && text.isNotBlank()) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ocr_${System.currentTimeMillis()}")
    }

    private fun clean(text: String) = text.replace(Regex("\\s+"), " ").trim().take(TextToSpeech.getMaxSpeechInputLength())
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    override fun onDestroy() {
        recognizer.close(); executor.shutdown(); tts?.stop(); tts?.shutdown(); super.onDestroy()
    }
}
