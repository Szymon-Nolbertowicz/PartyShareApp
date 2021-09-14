package com.example.partyshare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {

    private lateinit var  auth: FirebaseAuth;
    private lateinit var  database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        val btnRegister = findViewById(R.id.btnRegister) as Button
        val editEmail = findViewById(R.id.editEmail) as EditText
        val editPassword = findViewById(R.id.editPassword) as EditText
        btnRegister.setOnClickListener {
            val msg: String = editEmail.text.toString()
            val pass: String = editPassword.text.toString()


            if(msg.trim().length > 0 || pass.trim().length > 0)
            {
                createUser(msg, pass)
            } else {
                Toast.makeText(this, "Input not provided", Toast.LENGTH_LONG).show()
            }
        }

        val tvLogin = findViewById(R.id.tvLogin) as TextView

        tvLogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }


    }

    fun createUser(email:String, password:String) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.e(" Task Message", "Successful ...")
                        
                        var intent = Intent(this, welcomeView::class.java)
                        startActivity(intent)

                    } else {
                        Log.e("Task Message", "Failed ..." + task.exception)
                    }
                }
    }

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser

        if(user != null) {
            var intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
        }
    }
}