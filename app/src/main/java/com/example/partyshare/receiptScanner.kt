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
import com.google.gson.Gson
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class receiptScanner : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var recognizer:TextRecognizer
    private lateinit var ivCroppedImage: ImageView
    private lateinit var tvResult: TextView
    private lateinit var cropActivityResultLauncher: ActivityResultLauncher<Any?>
    private lateinit var arrayOfElements: ArrayList<receipt>

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

        val partyID = getIntent().getStringExtra("PARTY_ID").toString()
        val partyName = getIntent().getStringExtra("PARTY_NAME").toString()
        val fullName = getIntent().getStringExtra("FULLNAME").toString()
        Log.e("FullName in receipt", fullName)

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
            startTextRecognition(partyID, partyName, fullName)
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
            finish()
        }

        ivCroppedImage.setImageResource(testImage)
        selectedImage = BitmapFactory.decodeResource(resources, testImage)
    }

    private fun startTextRecognition(partyID: String, partyName: String, fullName:String) {
        var sum : Double = 0.0
        var counter = 0
        var i = 0;
        val inputImage = InputImage.fromBitmap(selectedImage!!, 0)
        val recognize = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognize.process(inputImage)
                .addOnSuccessListener {
                    for(block in it.textBlocks) {
                        counter++
                        for(line in block.lines) {
                            if (counter == 1) {
                                if(line.text.length > 2) {
                                    val receiptItem = receipt()
                                    val uuid = UUID.randomUUID()
                                    receiptItem.expenseID = uuid.toString()
                                    receiptItem.expenseName = line.text
                                    arrayOfElements.add(receiptItem)
                                } else counter--

                            }
                            if(counter > 1 && line.text.length > 5) {
                                val size = line.text.replace(" ", "").length
                                val data = line.text.replace(" ", "")
                                val dataFinal = data.replace(",", ".").drop(size-5)
                                val dataSumbit = dataFinal.filter { it.isDigit() || it == '.' } .toDouble()
                                sum += dataSumbit
                                arrayOfElements[i].expenseValue = dataSumbit
                                i++;
                            }
                        }
                    }

                    Log.d(TAG, "Successful recognition")
                    Log.e("Array of elements", arrayOfElements.toString())
                    Log.e("Sum", sum.toString())
                    val intent = Intent(this, receiptDataSend::class.java)
                    val ArrayAsString = Gson().toJson(arrayOfElements)
                    intent.putExtra("Array", ArrayAsString)
                        .putExtra("Sum", sum)
                        .putExtra("PARTY_ID", partyID)
                        .putExtra("PARTY_NAME", partyName)
                        .putExtra("FULLNAME", fullName)
                    startActivity(intent)


                }
                .addOnFailureListener {
                    Log.d(TAG, "Unsuccessful recognition", it)
                }
    }


    }












