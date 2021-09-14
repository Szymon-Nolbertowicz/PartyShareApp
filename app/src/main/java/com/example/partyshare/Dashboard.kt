package com.example.partyshare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class Dashboard : AppCompatActivity() {

    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        auth = FirebaseAuth.getInstance()

        val tvLogOut = findViewById(R.id.tvLogOut) as TextView
        val toProfile = findViewById(R.id.navToProfile) as ImageView

        toProfile.setOnClickListener {
            val intent = Intent(this, userCreate::class.java)
            startActivity(intent)
        }

        tvLogOut.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            Toast.makeText(this, "Successfuly logged out!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}