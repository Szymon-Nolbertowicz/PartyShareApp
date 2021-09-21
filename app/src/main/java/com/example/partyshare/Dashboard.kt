package com.example.partyshare

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Dashboard : AppCompatActivity() {

    private lateinit var auth:FirebaseAuth
    private lateinit var database:FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()


        val toProfile = findViewById(R.id.navToProfile) as ImageView
        val btnOCR = findViewById<Button>(R.id.btnOCR)
        val btnAddFriend = findViewById<Button>(R.id.btnAddFriend)


        toProfile.setOnClickListener {
            val intent = Intent(this, userCreate::class.java)
            startActivity(intent)
        }

        btnOCR.setOnClickListener {
            val intent = Intent(this, receiptScanner::class.java)
            startActivity(intent)
        }

        btnAddFriend.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Friend")
            val view = layoutInflater.inflate(R.layout.dialog_add_friend,null)
            val etFriendMail = view.findViewById<EditText>(R.id.etFriendEmail)
            builder.setView(view)
            builder.setPositiveButton("Add", DialogInterface.OnClickListener { _, _ ->
                var email = etFriendMail.text.toString()
                friendRequest(email)

            })
            builder.setNegativeButton("Close", DialogInterface.OnClickListener { _, _ -> })
            builder.show()
        }
    }

    private fun friendRequest(email: String){
        val ref = database.collection("users")
            .get()
            .addOnCompleteListener {
                val mail: StringBuffer = StringBuffer()
                if(it.isSuccessful) {
                    for(document in it.result!!) {
                        if(document.data.getValue("email").toString() == email ) {
                            Log.d("User UID", document.data.getValue("uID").toString())
                            var friendUID = document.data.getValue("uID").toString()
                            var user = auth.currentUser
                            if(user != null)
                            {
                                val requestStatus: MutableMap<String, Any> = HashMap()
                                val requestStatusRec: MutableMap<String, Any> = HashMap()
                                if(friendUID.isNotEmpty()) {
                                    requestStatus["status"] = "pending"
                                    requestStatusRec["status"] = "requested"
                                }

                                database.collection("users").document(user.uid).collection("friends").document(friendUID)
                                    .set(requestStatus)

                                database.collection("users").document(friendUID).collection("friends").document(user.uid)
                                    .set(requestStatusRec)
                                    .addOnCompleteListener { task ->
                                        if(task.isSuccessful){
                                            Toast.makeText(this, "User Friend Request Sent", Toast.LENGTH_SHORT).show()
                                        }
                                        else {
                                            Log.e("Database Update", "Failed" + task.exception)
                                        }
                                    }

                            }
                        }
                    }
                }
            }
    }

}