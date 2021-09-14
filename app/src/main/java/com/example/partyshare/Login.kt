package com.example.partyshare

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val tvRegister = findViewById(R.id.tvRegister) as TextView
        tvRegister.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val btnLogin = findViewById(R.id.btnLogin) as Button
        val mail = findViewById(R.id.editEmail) as EditText
        val pass = findViewById(R.id.editPassword) as EditText
        val btnReset = findViewById(R.id.tvResetPassword) as TextView


        btnLogin.setOnClickListener {
            if (mail.text.trim().toString().length > 0 || pass.text.trim().toString().length > 0) {
                signInUser(mail.text.trim().toString(), pass.text.trim().toString())
            } else {
                Toast.makeText(this, "Input required!", Toast.LENGTH_LONG).show()
            }
        }

        btnReset.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Forgot Password")
            val view = layoutInflater.inflate(R.layout.dialog_forgot_password, null)
            val username = view.findViewById(R.id.dialForgot) as EditText
            builder.setView(view)
            builder.setPositiveButton("Reset", DialogInterface.OnClickListener{ _, _ -> forgotPassword(username)
            })
            builder.setNegativeButton("Close", DialogInterface.OnClickListener{_, _ -> })
            builder.show()
        }



    }

    fun forgotPassword(username: EditText) {
        if(username.text.toString().isEmpty()) {
            return
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(username.text.toString()).matches()){
            return
        }

        auth.sendPasswordResetEmail(username.text.toString())
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    Log.e("Password link status", "SENT")
                }
            }
    }

    fun updateProfile(firstName: String, lastName: String){

    }

    fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, Dashboard::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Error!" + task.exception, Toast.LENGTH_LONG).show()
                }
            }
    }
}