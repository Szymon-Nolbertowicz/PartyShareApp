package com.example.partyshare

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlin.properties.Delegates

class membersList : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var userArrayList: ArrayList<user>
    private lateinit var myAdapter: UsersAdapter
    private var permissionGrantedFlag: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members_list)

        val partyID = getIntent().getStringExtra("PARTY_ID").toString()
        val partyName = getIntent().getStringExtra("PARTY_NAME").toString()

        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val recyclerView = findViewById(R.id.recycle_membersList) as RecyclerView
        val btnBack = findViewById<ImageView>(R.id.onBackToMenu)
        val btnAddMember = findViewById<FloatingActionButton>(R.id.addMemberBtn)

        permissionCheck(btnAddMember, partyID)

        btnBack.setOnClickListener {
                val intent = Intent(this, party_main::class.java)
                intent.putExtra("PARTY_ID", partyID)
                    .putExtra("PARTY_NAME", partyName)
                startActivity(intent)
        }

        btnAddMember.setOnClickListener {
            if(!permissionGrantedFlag) Toast.makeText(this, "You don't have sufficient permissions", Toast.LENGTH_SHORT).show()
            else {
                val intent = Intent(this, friendsListParty::class.java)
                intent.putExtra("PARTY_ID", partyID)
                    .putExtra("PARTY_NAME", partyName)
                startActivity(intent)
            }
        }

        userArrayList = arrayListOf()
        myAdapter = UsersAdapter(userArrayList)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@membersList)
            adapter = myAdapter
        }

        EventChangeListener(partyID)
    }

    @SuppressLint("ResourceAsColor")
    private fun permissionCheck(btnAddMember: FloatingActionButton, partyID: String) {
        val currUserID = auth.currentUser.uid
        var role: String = ""
        database.collection("parties").document(partyID).collection("members").document(currUserID)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    role = it.result.data!!.getValue("role").toString()
                    if(role != "host") {
                        permissionGrantedFlag = false
                        var greenColorValue = Color.parseColor("#CCCCCC")
                        btnAddMember.backgroundTintList = ColorStateList.valueOf(greenColorValue)
                    }
                    else permissionGrantedFlag = true
                }
            }
    }

    private fun EventChangeListener(partyID: String){
        val currUser = auth.currentUser!!
        database.collection("parties").document(partyID).collection("members")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(
                    value: QuerySnapshot?,
                    error: FirebaseFirestoreException?
                ) {
                    if (error != null) {
                        Log.e("FireStore error", error.message.toString())
                        return
                    }

                    for (dc: DocumentChange in value?.documentChanges!! ){
                        if(dc.type == DocumentChange.Type.ADDED) {
                            userArrayList.add((dc.document.toObject(user::class.java)))
                        }
                    }

                    myAdapter.notifyDataSetChanged()
                }
            })

    }
}