package com.example.partyshare

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


class welcomeView : AppCompatActivity() {

        private lateinit var auth: FirebaseAuth
        private lateinit var database: FirebaseFirestore
        private lateinit var storage: FirebaseStorage
        private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_view)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference


        val user = auth.currentUser

        var userID = "value"
        //var userEmail = mail@op.pl
        if (user != null) {
            userID = user.uid
            //userEmail = user.email
        }

        val mFirstName = findViewById(R.id.etFirstName) as EditText
        val mLastName = findViewById(R.id.etLastName) as EditText
        val saveBtn = findViewById(R.id.tvSave) as TextView
        val pickPhoto = findViewById(R.id.ivPhoto) as ImageView
        val limitSeekBar = findViewById<SeekBar>(R.id.sbLimit)
        val etLimitValue = findViewById<TextView>(R.id.etLimitValue)

        pickPhoto.setOnClickListener {
            selectPhoto()
            if(storageRef.child("images/${auth.currentUser?.uid}.jpg") != null){
               var pathRefference = storageRef.child("images/${auth.currentUser?.uid}.jpg")
                pathRefference.downloadUrl
                    .addOnSuccessListener {
                        Glide.with(this)
                            .load(it)
                            .into(pickPhoto)
                    }
            }
        }

        saveBtn.setOnClickListener {
            var firstName = mFirstName.text.toString()
            var lastName = mLastName.text.toString()
            var userid = userID.toString()
            //var usermail = userEmail


            saveFireStore(firstName, lastName, userid, etLimitValue.text.toString().toInt())
            var intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
        }

        limitSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                etLimitValue.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

    }

    fun saveFireStore(firstname: String, lastname: String, uid: String, limitValue: Number) {
        val user: MutableMap<String, Any> = HashMap()
        var usercheck = auth.currentUser
        var email = "value"
        if (usercheck != null) {
            email = usercheck.email.toString()
        }
        user["firstName"] = firstname
        user["lastName"] = lastname
        user["uID"] = uid
        user["email"] = email
        user["limitValue"] = limitValue
        user["monthlySpend"] = 0

        if (usercheck != null) {

        database.collection("users").document(usercheck.uid)
                .set(user)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.e("Task Message", "Sucess, user added to database")
                    } else {
                        Log.e("Task Message", "There is error: " + task.exception)
                    }
                }
        }
    }

    fun selectPhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(intent, 100)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100 && resultCode == RESULT_OK){
            Toast.makeText(this, "Upload Success", Toast.LENGTH_SHORT).show()
            var file = data?.data
            var fileRef = storage.reference.child("images/${auth.currentUser?.uid}.jpg")
            if(file != null){
                var uploadTask = fileRef.putFile(file)
                uploadTask.addOnSuccessListener {
                    Log.e("Image Upload", "Success")
                }
            }

        }
    }

    fun uploadPhoto() {

    }
}