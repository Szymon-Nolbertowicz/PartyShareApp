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

class passwordChange : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_change)

        auth = FirebaseAuth.getInstance()

        var tvSave = findViewById(R.id.tvChange) as TextView
        var etNew = findViewById(R.id.etNewPassword) as EditText
        var etCurr = findViewById(R.id.currentPassword) as EditText
        var etConfirm = findViewById(R.id.etConfirm) as EditText
        var onBack = findViewById(R.id.onBackToMenu) as ImageView

        var new = etNew.text


        tvSave.setOnClickListener {

            changePassword(etCurr.text.toString(), etNew.text.toString(), etConfirm.text.toString())

        }

        onBack.setOnClickListener {
            var intent = Intent(this, userCreate::class.java)
            startActivity(intent)
        }


    }

    fun changePassword(current: String, new:String, confirm:String){
        if(current.isNotEmpty() && new.isNotEmpty() && confirm.isNotEmpty())
        {
            if(new.toString().equals(confirm.toString())) {
                    val user = auth.currentUser
                    if(user != null && user.email != null) {
                        val credential = EmailAuthProvider
                            .getCredential(user.email!!, current)

                        user?.reauthenticate(credential)
                            ?.addOnCompleteListener {
                                if(it.isSuccessful){
                                    Log.e("reAuthenticationStatus", "User re-authenticated.")
                                    user?.updatePassword(new)
                                        ?.addOnCompleteListener { task ->
                                            if(task.isSuccessful){
                                                Toast.makeText(this, "User password updated", Toast.LENGTH_SHORT).show()
                                                auth.signOut()
                                                var intent = Intent(this, Login::class.java)
                                                startActivity(intent)
                                                finish()
                                            }
                                        }
                                }
                                else{
                                    Log.e("reAuthenticationStatus", "User re-authentication failed.")
                                    Toast.makeText(this, "Wrong current password", Toast.LENGTH_SHORT).show()
                                }

                            }
                    }
            }
            else
            {
                Toast.makeText(this, "Password does not match!", Toast.LENGTH_SHORT).show()
            }
        }
        else {
            Toast.makeText(this, "Please fill all the fields!", Toast.LENGTH_SHORT).show()
        }
    }



}