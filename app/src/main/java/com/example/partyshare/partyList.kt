package com.example.partyshare

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class partyList : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var partyArrayList: ArrayList<party>
    private lateinit var myAdapter: PartyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_party_list)

        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val recyclerView = findViewById(R.id.recycle_partyList) as RecyclerView
        val btnBack = findViewById<ImageView>(R.id.onBackToMenu)
        val btnJoinParty = findViewById<FloatingActionButton>(R.id.btnJoin)
        val fullName = getIntent().getStringExtra("FULLNAME")

        btnJoinParty.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Join Party")
            val view = layoutInflater.inflate(R.layout.dialog_join_party,null)
            val etPartyID = view.findViewById<EditText>(R.id.etPartyID)
            builder.setView(view)
            builder.setPositiveButton("Add", DialogInterface.OnClickListener { _, _ ->
                val id = etPartyID.text.toString()
                partyJoin(id)
                Toast.makeText(this, "Party joined!", Toast.LENGTH_SHORT).show()

            })
            builder.setNegativeButton("Close", DialogInterface.OnClickListener { _, _ -> })
            builder.show()
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
        }

        //userAccept.setOnClickListener {
        //}
        partyArrayList = arrayListOf()
        myAdapter = PartyAdapter(partyArrayList)
        var adapter = myAdapter


        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@partyList)
        }

        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : PartyAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                val clickedItem: String = partyArrayList[position].ID.toString()
                Log.e("user position", clickedItem.toString())
                val intent = Intent(this@partyList, party_main::class.java)
                intent.putExtra("PARTY_ID", partyArrayList[position].ID)
                intent.putExtra("PARTY_NAME", partyArrayList[position].name.toString())
                    .putExtra("FULLNAME", fullName)
                startActivity(intent)
            }
        })

        EventChangeListener()
    }

    private fun partyJoin(id: String) {
        var currUser = auth.currentUser
        val party: MutableMap<String, Any> = HashMap()
        val member: MutableMap<String, Any> = HashMap()

        database.collection("users")
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    for(document in it.result!!) {
                        if(document.data.getValue("uID") == currUser.uid) {
                            member["firstName"] = document.data.getValue("firstName")
                            member["lastName"] = document.data.getValue("lastName")
                            member["uID"] = currUser.uid
                            member["role"] = "member"
                            member["balance"] = 0
                            member["transferStatus"] = "NOT REQUESTED YET"

                            database.collection("parties").document(id).collection("members")
                                .document(currUser.uid)
                                .set(member)
                        }
                    }
                }
            }

        database.collection("parties")
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful)
                {
                    for(document in it.result!!) {
                        if(document.data.getValue("ID") == id) {
                            party["name"] = document.data.getValue("name")
                            party["ID"] = document.data.getValue("ID")

                            database.collection("users").document(currUser.uid).collection("parties").document(id)
                                .set(party)
                        }
                    }
                }
            }

        database.collection("parties").document(id)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    var currMemberQty = it.result.data!!.getValue("membersQty").toString().toInt()
                    currMemberQty++
                    database.collection("parties").document(id)
                        .update("membersQty", currMemberQty)
                }
            }

    }

    private fun EventChangeListener() {
        val currUser = auth.currentUser!!
        database.collection("users").document(currUser.uid).collection("parties")
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
                        if(dc.type == DocumentChange.Type.ADDED) {
                            var partyName = dc.document.data.getValue("name")
                            Log.e("partyName", partyName.toString())
                            partyArrayList.add((dc.document.toObject(party::class.java)))
                            Log.e("lsita", partyArrayList.toString())

                        }
                    }

                    myAdapter.notifyDataSetChanged()
                }
            })
    }
}