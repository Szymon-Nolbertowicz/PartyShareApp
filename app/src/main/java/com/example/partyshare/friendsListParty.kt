package com.example.partyshare

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class friendsListParty : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var userArrayList: ArrayList<user>
    private lateinit var myAdapter: AddMemberAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_list_party)

        val partyID = getIntent().getStringExtra("PARTY_ID").toString()
        val partyName = getIntent().getStringExtra("PARTY_NAME").toString()
        val btnCopyID = findViewById<TextView>(R.id.tvPartyKey)

        btnCopyID.text = partyID

        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val recyclerView = findViewById(R.id.recycleFriendsListParty) as RecyclerView
        val btnBack = findViewById<ImageView>(R.id.onBackToMenu)
        val btnCopy2Clipboard = findViewById<LinearLayout>(R.id.linear_ll2)
        val partyKey = btnCopyID.getText().toString()

        btnBack.setOnClickListener {
            val intent = Intent(this, membersList::class.java)
            intent.putExtra("PARTY_ID", partyID)
                .putExtra("PARTY_NAME", partyName)
            startActivity(intent)
        }

        btnCopy2Clipboard.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("PARTY_ID", partyKey)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
        }


        userArrayList = arrayListOf()
        myAdapter = AddMemberAdapter(userArrayList)
        var adapter = myAdapter


        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@friendsListParty)
        }

        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : AddMemberAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                addMember(position, partyID, partyName)
            }
        })

        EventChangeListener()
    }

    private fun addMember(position: Int, partyID: String, partyName:String) {
        val clickedItem: String = userArrayList[position].uID.toString()

        val updateParty: MutableMap<String, Any> = HashMap()
        val updateUser: MutableMap<String, Any> = HashMap()
        updateUser["ID"] = partyID
        updateUser["name"] = partyName
        database.collection("users").document(clickedItem).collection("parties").document(partyID)
            .set(updateUser)

        database.collection("parties").document(partyID).collection("members").document(clickedItem)
            .get()
            .addOnCompleteListener(object: OnCompleteListener<DocumentSnapshot> {
                override fun onComplete(p0: Task<DocumentSnapshot>) {
                    if(p0.isSuccessful) {
                        val doc: DocumentSnapshot = p0.result!!
                        if(doc.exists()) {
                            Toast.makeText(this@friendsListParty, "Already a member!", Toast.LENGTH_SHORT).show()
                        }

                        else {
                            database.collection("users").document(clickedItem)
                                .get()
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        updateParty["balance"] = 0
                                        updateParty["firstName"] = it.result.data!!.getValue("firstName")
                                        updateParty["lastName"] = it.result.data!!.getValue("lastName")
                                        updateParty["uID"] = it.result.data!!.getValue("uID")
                                        updateParty["role"] = "member"



                                        database.collection("parties").document(partyID).collection("members").document(clickedItem)
                                            .set(updateParty)
                                    }
                                }
                        }
                    }
                }

            })

    }

    private fun EventChangeListener() {
        val currUser = auth.currentUser!!
        database.collection("users").document(currUser.uid).collection("friends")
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
                        if(dc.type == DocumentChange.Type.ADDED && dc.document.data.getValue("status") == "friends") {
                            userArrayList.add((dc.document.toObject(user::class.java)))

                        }
                    }

                    myAdapter.notifyDataSetChanged()
                }
            })
    }
}