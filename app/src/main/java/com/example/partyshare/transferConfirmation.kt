package com.example.partyshare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class transferConfirmation : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var userArrayList: ArrayList<member>
    private lateinit var myAdapter: MemberAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_confirmation)

        val partyID = getIntent().getStringExtra("PARTY_ID").toString()
        val partyName = getIntent().getStringExtra("PARTY_NAME").toString()

        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val recyclerView = findViewById(R.id.recycle_transferConf) as RecyclerView
        val btnBack = findViewById<ImageView>(R.id.onBackToMenu)

        btnBack.setOnClickListener {
            val intent = Intent(this, party_main::class.java)
            intent.putExtra("PARTY_ID", partyID)
                .putExtra("PARTY_NAME", partyName)
            startActivity(intent)
        }

        userArrayList = arrayListOf()
        myAdapter = MemberAdapter(userArrayList)
        var adapter = myAdapter

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@transferConfirmation)
            adapter = myAdapter
        }

        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : MemberAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                var currUser = auth.currentUser!!
                val clickedItem = userArrayList[position]
                takeAction(clickedItem, partyID)
                val requestStatus: MutableMap<String, Any> = HashMap()
            }
        })

        EventChangeListener(partyID)
    }

    private fun takeAction(clickedItem: member, partyID: String) {
        when(clickedItem.TransferStatus) {
            "NOT REQUESTED YET" -> Toast.makeText(this, "User didn't requested confirmation yet!", Toast.LENGTH_SHORT).show()
            "REQUESTED" -> {
                database.collection("parties").document(partyID).collection("members").document(clickedItem.uID.toString())
                    .update("transferStatus", "TRANSFER CONFIRMED")
                Toast.makeText(this, "Transfer has been confirmed", Toast.LENGTH_SHORT).show()
            }
            "TRANSFER CONFIRMED" -> Toast.makeText(this, "You already confirmed that transfer!", Toast.LENGTH_SHORT).show()
            else -> Toast.makeText(this, "There's been some error, try again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun EventChangeListener(partyID: String) {
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

                   for (dc: DocumentChange in value?.documentChanges!!){
                       if(dc.type == DocumentChange.Type.ADDED && dc.document.data.getValue("transferStatus") != "null") {
                           userArrayList.add((dc.document.toObject(member::class.java)))
                       }
                   }

                   myAdapter.notifyDataSetChanged()
               }
           })
    }
}