package com.example.partyshare

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.module.AppGlideModule
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File


class userCreate : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var user: FirebaseUser
    private lateinit var storage: FirebaseStorage





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_create)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        var storageRef = storage.reference



        var user = auth.currentUser

        var userid = "value"

        if(user != null) {
            userid = user.uid
            Log.e("UserID", userid)
        }

        var tvYourName = findViewById(R.id.tvYourName) as TextView
        var tvEmail = findViewById(R.id.tvEmail2) as TextView
        var tvFirstName = findViewById(R.id.tvFirstName) as TextView
        var tvLastName = findViewById(R.id.tvLastName) as TextView
        var ivProfilePic = findViewById(R.id.ivUserAvatar) as ImageView

        val icPersonRef = storageRef.child("image.jpg")
        var file = Uri.parse("android.resource://$packageName/${R.drawable.obrazek}")
        var fileRef = storageRef.child("images/obrazek.jpg")
        var uploadTask = fileRef.putFile(file)
        uploadTask.addOnSuccessListener {
            Log.e("Image Upload", "Success")
        }

        var fileName = "https://esportlife.pl/wp-content/gallery/rozne/thumbs/thumbs_comment_2HHoHZvBcfuPqyDUjovsyWvJsNXrFZ1G.jpg"

        var pathRefference = storageRef.child("images/avatar.jpg")

        if(storageRef.child("images/${auth.currentUser?.uid}.jpg") != null){
            pathRefference = storageRef.child("images/${auth.currentUser?.uid}.jpg")
        }


        val profPic = File.createTempFile("images", "jpg")



        pathRefference.getFile(profPic)
            .addOnSuccessListener {
                Log.e("Image download", "Success")
            }

        var url = pathRefference.downloadUrl
        var url2 = pathRefference.downloadUrl
            .addOnSuccessListener {
                Glide.with(this)
                    .load(it)
                    .into(ivProfilePic)
                Log.e("url with to string", it.toString())
            }


        val docRef = database.collection("users")
                .whereEqualTo("uID", userid)
                .get()
                .addOnCompleteListener { it
                    val firstName: StringBuffer = StringBuffer()
                    val lastName: StringBuffer = StringBuffer()
                    val fullName: StringBuffer = StringBuffer()
                    val mail: StringBuffer = StringBuffer()
                    if(it.isSuccessful) {
                        for(document in it.result!!){
                            fullName.append(document.data.getValue("firstName")).append(" ")
                                    .append(document.data.getValue("lastName")).append(" ")
                            firstName.append(document.data.getValue("firstName")).append(" ")
                            lastName.append(document.data.getValue("lastName")).append(" ")
                            mail.append(document.data.getValue("email")).append(" ")
                        }
                        tvYourName.setText(fullName)
                        tvEmail.setText(mail)
                        tvFirstName.setText(firstName)
                        tvLastName.setText(lastName)

                    }
                }



        //var userid = auth.currentUser.uid

        val logout = findViewById(R.id.tvLogOutToolbar) as TextView
        val back = findViewById(R.id.onBackToMenu) as ImageView
        val changePass = findViewById(R.id.tvChangePass) as TextView
        val changemail = findViewById(R.id.tvChangeEmail) as TextView
        val editProfile = findViewById(R.id.tvChangeProfile) as TextView



        editProfile.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Edit Profile")
            val view = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
            val firstName = view.findViewById(R.id.currentFirstName) as EditText
            val lastName = view.findViewById(R.id.currentLastName) as EditText
            builder.setView(view)
            builder.setPositiveButton("Save", DialogInterface.OnClickListener { _, _ ->
                updateProfile(
                    firstName.text.toString(),
                    lastName.text.toString()
                )
            })
            builder.setNegativeButton("Close", DialogInterface.OnClickListener { _, _ -> })
            builder.show()
        }




        logout.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Sign-out")
            builder.setMessage("Are you sure you want to logout?")
            builder.setPositiveButton("Yes", { dialogInterface: DialogInterface, i: Int ->
                auth.signOut()
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
            })
            builder.setNegativeButton("No", { dialogInterface: DialogInterface, i: Int -> })
            builder.show()
        }



        back.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
        }

        changePass.setOnClickListener {
            val intent = Intent(this, passwordChange::class.java)
            startActivity(intent)
        }

        changemail.setOnClickListener {
            val intent = Intent(this, emailChange::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun updateProfile(firstname: String, lastname: String) {
        Log.e("firstname and lastname", firstname + " " + lastname)
        var user = auth.currentUser
        if(user != null) {
            val userInfo: MutableMap<String, Any> = HashMap()
            if(firstname.isNotEmpty()) {
                userInfo["firstName"] = firstname
            }
            if(lastname.isNotEmpty()){
                userInfo["lastName"] = lastname
            }
            database.collection("users").document(user.uid)
                    .set(userInfo, SetOptions.merge())
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            Toast.makeText(this, "User Info Updated", Toast.LENGTH_SHORT).show()
                            Log.e("firstname and lastname", firstname + " " + lastname)
                            val intent = Intent(this, userCreate::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else {
                            Log.e("Database Update", "Failed" + task.exception)
                        }
                    }

        }
    }
}