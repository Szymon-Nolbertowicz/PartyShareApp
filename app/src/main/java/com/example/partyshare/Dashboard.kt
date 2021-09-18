package com.example.partyshare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
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


        val toProfile = findViewById(R.id.navToProfile) as ImageView
        val btnOCR = findViewById<Button>(R.id.btnOCR)

        toProfile.setOnClickListener {
            val intent = Intent(this, userCreate::class.java)
            startActivity(intent)
        }



        btnOCR.setOnClickListener {
            val intent = Intent(this, receiptScanner::class.java)
            startActivity(intent)
        }
    }
}