package com.example.partyshare

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils.listEllipsize
import android.text.TextUtils.replace
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import com.canhub.cropper.CropImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.*
import kotlin.collections.HashMap


class receiptScanner : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var recognizer:TextRecognizer
    private lateinit var ivCroppedImage: ImageView
    private lateinit var tvResult: TextView
    private lateinit var cropActivityResultLauncher: ActivityResultLauncher<Any?>
    private lateinit var arrayOfElements: ArrayList<Any>

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

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        arrayOfElements = arrayListOf()

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
        val hashMapOfExpenses: MutableMap<String, Any> = HashMap()
        val currUser = auth.currentUser.uid
        var counter = 0
        var counter2 = -1
        val inputImage = InputImage.fromBitmap(selectedImage!!, 0)
        val recognize = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognize.process(inputImage)
                .addOnSuccessListener {
                    tvResult.text = it.text
                    for(block in it.textBlocks) {
                        Log.e("Block:", block.text)
                        counter++
                        for(line in block.lines) {

                            if (counter == 1) {
                                val uuid = UUID.randomUUID()
                                hashMapOfExpenses["expenseID"] = uuid
                                hashMapOfExpenses["expenseName"] = line.text
                                Log.e("Name of Expense", line.text)
                                arrayOfElements.add(uuid)

                            }
                            if(counter == 2) {
                                Log.e("Line:", line.text)
                                val data = line.text.replace(" ", "")
                                val dataFinal = data.replace(",", ".").drop(6)
                                val dataSumbit = dataFinal.filter { it.isDigit() || it == '.' } .toDouble()
                                hashMapOfExpenses["expenseValue"] = dataSumbit
                                Log.e("Final value of expense ", dataSumbit.toString())
                            }
                            for(element in line.elements) {
                                //Log.e("Element:", element.text)
                            }
                        }
                    }

                    Log.d(TAG, "Successful recognition")
                    Log.e("Array of elements", arrayOfElements.toString())

                }
                .addOnFailureListener {
                    Log.d(TAG, "Unsuccessful recognition", it)
                }
    }


    }












