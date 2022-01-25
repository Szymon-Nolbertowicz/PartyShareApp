package com.example.partyshare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class emailChange : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_change)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etCurrent = findViewById<EditText>(R.id.currentPassword)
        val etConfirm = findViewById<EditText>(R.id.etConfirmEmail)
        val etNewMail = findViewById<EditText>(R.id.etNewEmail)
        val tvChange = findViewById<TextView>(R.id.tvChangeMail)
        val btnBack = findViewById<ImageView>(R.id.onBackToMenu)

        tvChange.setOnClickListener {
            changeEmail(etCurrent.text.toString(), etNewMail.text.toString(), etConfirm.text.toString())
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, userCreate::class.java)
            startActivity(intent)
        }

    }

    private fun changeEmail(current: String, new:String, confirm:String){
        if(current.isNotEmpty() && new.isNotEmpty() && confirm.isNotEmpty())
        {
            if(new == confirm) {
                val user = auth.currentUser
                if(user != null && user.email != null) {
                    val credential = EmailAuthProvider
                        .getCredential(user.email!!, current)

                    user.reauthenticate(credential)
                        .addOnCompleteListener {
                            if(it.isSuccessful){
                                Log.e("reAuthenticationStatus", "User re-authenticated.")
                                user.updateEmail(new)
                                    .addOnCompleteListener { task ->
                                        if(task.isSuccessful){
                                            Toast.makeText(this, "User mail updated", Toast.LENGTH_SHORT).show()
                                            val data = hashMapOf("email" to new)
                                            db.collection("users").document(user.uid)
                                                .set(data, SetOptions.merge())

                                        }
                                    }
                            } else{
                                Log.e("reAuthenticationStatus", "User re-authentication failed.")
                                Toast.makeText(this, "Wrong current password", Toast.LENGTH_SHORT).show()
                            }

                        }
                }
            }
            else
            {
                Toast.makeText(this, "E-mails does not match!", Toast.LENGTH_SHORT).show()
            }
        }
        else {
            Toast.makeText(this, "Please fill all the fields!", Toast.LENGTH_SHORT).show()
        }
    }
}