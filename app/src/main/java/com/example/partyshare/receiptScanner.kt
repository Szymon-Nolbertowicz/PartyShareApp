package com.example.partyshare

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import com.canhub.cropper.CropImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


class receiptScanner : AppCompatActivity() {


    private lateinit var recognizer:TextRecognizer
    private lateinit var ivCroppedImage: ImageView
    private lateinit var tvResult: TextView
    private lateinit var cropActivityResultLauncher: ActivityResultLauncher<Any?>

    private val TAG = "ML_Kit_text_recognition"
    private val testImage = R.drawable.testimage1
    private var selectedImage:Bitmap? = null

    private val cropActivityResultContract = object : ActivityResultContract<Any?, Uri?>() {
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity()
                .getIntent(this@receiptScanner)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uriContent
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt_scanner)

        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val btnChooseImage = findViewById<Button>(R.id.btnSelectPhoto)
        ivCroppedImage = findViewById<ImageView>(R.id.ivPhoto)
        val btnBack = findViewById<ImageView>(R.id.onBackToMenu)
        val btnExtract = findViewById<Button>(R.id.btnExtractText)
        tvResult = findViewById<EditText>(R.id.tvResult)
        cropActivityResultLauncher = registerForActivityResult(cropActivityResultContract) {
            it?.let {
                uri ->
                ivCroppedImage.setImageURI(uri)
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                selectedImage = bitmap
            }
        }

        btnChooseImage.setOnClickListener{
            cropActivityResultLauncher.launch(null)
        }

        btnExtract.setOnClickListener {
            startTextRecognition()
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
            finish()
        }

        ivCroppedImage.setImageResource(testImage)
        selectedImage = BitmapFactory.decodeResource(resources, testImage)
    }

    private fun startTextRecognition() {
        val inputImage = InputImage.fromBitmap(selectedImage!!, 0)
        val recognize = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognize.process(inputImage)
                .addOnSuccessListener {
                    tvResult.text = it.text
                    Log.d(TAG, "Successful recognition")
                }
                .addOnFailureListener {
                    Log.d(TAG, "Unsuccessful recognition", it)
                }
    }


    }












