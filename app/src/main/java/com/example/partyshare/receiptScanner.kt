package com.example.partyshare

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.yalantis.ucrop.UCrop
import java.io.File


class receiptScanner : AppCompatActivity() {


    private lateinit var recognizer:TextRecognizer
    private lateinit var uri: Uri
    private lateinit var destUri: Uri
    private lateinit var resultUri: Uri
    private lateinit var resUri: Uri
    private lateinit var ivCroppedImage: ImageView
    private lateinit var tvResult: TextView

    private val TAG = "ML_Kit_text_recognition"
    private val testImage = R.drawable.testimage1
    private var selectedImage:Bitmap? = null






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt_scanner)

        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val btnChooseImage = findViewById<Button>(R.id.btnSelectPhoto)
        ivCroppedImage = findViewById<ImageView>(R.id.ivPhoto)
        val btnBack = findViewById<ImageView>(R.id.onBackToMenu)
        val btnExtract = findViewById<Button>(R.id.btnExtractText)
        tvResult = findViewById<EditText>(R.id.tvResult)

        btnChooseImage.setOnClickListener{
            openDialog()
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
                    Log.d("TAG", "Successful recognition")
                }
                .addOnFailureListener {
                    Log.d("TAG", "Unsuccessful recognition", it)
                }
    }

    private fun openDialog() {
        val openDialog = AlertDialog.Builder(this@receiptScanner)
        openDialog.setTitle("Choose image source")
        openDialog.setPositiveButton("Camera") {
            dialog, _->
            openCamera()
            dialog.dismiss()
        }
        openDialog.setNeutralButton("Gallery") {
            dialog, _->
            openGallery()
        }
        openDialog.setNegativeButton("Cancel") {
            dialog, _->
            dialog.dismiss()
        }
        openDialog.create()
        openDialog.show()

    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(Intent.createChooser(intent, "Select Image"), 2)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        var file = File(Environment.getExternalStorageDirectory(), "file" + System.currentTimeMillis().toString() + ".jpg")
        var uri = Uri.fromFile(file)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        intent.putExtra("return-data", true)
        startActivityForResult(intent, 0)

    }

    private fun cropImages(){
        UCrop.of(uri, destUri)
            .withAspectRatio(16F, 9F)
            .withMaxResultSize(300,180)
            .start(this@receiptScanner);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode == RESULT_OK) {
            cropImages()
            if (data != null) {

                    resultUri = UCrop.getOutput(data!!)!!
            }
        } else if (requestCode == 2) {
            uri = data?.data!!
            cropImages()
        } else if (requestCode == 1) {
            if (data != null) {
                val bundle = data.extras
                val bitmap = bundle!!.getParcelable<Bitmap>("data")
                ivCroppedImage.setImageBitmap(bitmap)
            }
        }
    }
}