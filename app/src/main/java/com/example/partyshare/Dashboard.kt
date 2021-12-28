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
import java.util.*
import kotlin.collections.HashMap

class Dashboard : AppCompatActivity() {

    private lateinit var auth:FirebaseAuth
    private lateinit var database:FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()


        val toProfile = findViewById(R.id.navToProfile) as ImageView
        val btnFriendsList = findViewById<Button>(R.id.btnFriendsList)
        val btnCreateParty = findViewById<Button>(R.id.btnCreate)
        val btnPartyList = findViewById<Button>(R.id.btnPartyList)



        btnPartyList.setOnClickListener {
            var fullName: String
            database.collection("users").document(auth.currentUser.uid)
                .get()
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        val firstName = it.result.data!!.getValue("firstName").toString()
                        val lastName = it.result.data!!.getValue("lastName").toString()
                        fullName = firstName + " " + lastName
                        val intent = Intent(this,partyList::class.java)
                            .putExtra("FULLNAME", fullName)
                        startActivity(intent)
                    }
                }
        }

        toProfile.setOnClickListener {
            val intent = Intent(this, userCreate::class.java)
            startActivity(intent)
        }

        btnFriendsList.setOnClickListener {
            val intent = Intent(this, friendsList::class.java)
            startActivity(intent)
        }

        btnCreateParty.setOnClickListener {
            var newUUID = UUID.randomUUID().toString()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Create a Party")
            val view = layoutInflater.inflate(R.layout.dialog_create_party,null)
            val etFriendMail = view.findViewById<EditText>(R.id.etPartyName)
            builder.setView(view)
            builder.setPositiveButton("Add", DialogInterface.OnClickListener { _, _ ->
                val name = etFriendMail.text.toString()
                partyCreate(name, newUUID)
                Toast.makeText(this, "Party created!", Toast.LENGTH_SHORT).show()

            })
            builder.setNegativeButton("Close", DialogInterface.OnClickListener { _, _ -> })
            builder.show()
        }


    }


    private fun partyCreate(name: String, partyID: String) {
        var currUser = auth.currentUser
        val party: MutableMap<String, Any> = HashMap()
        val host: MutableMap<String, Any> = HashMap()
        party["name"] = name
        party["ID"] = partyID
        party["total"] = 0
        party["membersQty"] = 1

        val ref = database.collection("parties").document(partyID)
            .set(party)

        database.collection("users")
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful)
                {
                    for(document in it.result!!)
                    {
                        if(document.data.getValue("uID") == currUser.uid)
                        {
                            host["firstName"] = document.data.getValue("firstName")
                            host["lastName"] = document.data.getValue("lastName")
                            host["uID"] = currUser.uid
                            host["role"] = "host"
                            host["balance"] = 0

                            database.collection("parties").document(partyID).collection("members").document(currUser.uid)
                                .set(host);

                        }
                    }
                }
            }

        database.collection("users").document(currUser.uid).collection("parties").document(partyID)
            .set(party)
    }


}