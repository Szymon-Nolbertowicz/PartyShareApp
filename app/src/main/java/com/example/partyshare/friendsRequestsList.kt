package com.example.partyshare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*


class friendsRequestsList : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var userArrayList: ArrayList<user>
    private lateinit var myAdapter: UsersInvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_requests_list)

        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val recyclerView = findViewById<RecyclerView>(R.id.recycle_Viewer)
        val btnBack = findViewById<ImageView>(R.id.onBackToMenu)

        btnBack.setOnClickListener {
            val intent = Intent(this, friendsList::class.java)
            startActivity(intent)
        }

        //userAccept.setOnClickListener {
        //}
        userArrayList = arrayListOf()
        myAdapter = UsersInvAdapter(userArrayList)
        val adapter = myAdapter


        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@friendsRequestsList)
        }

        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : UsersInvAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                val currUser = auth.currentUser!!
                Toast.makeText(this@friendsRequestsList, "Invitation accepted!", Toast.LENGTH_SHORT).show()
                val clickedItem: String = userArrayList[position].uID.toString()
                val requestStatus: MutableMap<String, Any> = HashMap()
                requestStatus["status"] = "friends"
                database.collection("users").document(currUser.uid).collection("friends").document(clickedItem)
                    .update(requestStatus)
                database.collection("users").document(clickedItem).collection("friends").document(currUser.uid)
                    .update(requestStatus)

                val intent = Intent(this@friendsRequestsList, friendsList::class.java)
                startActivity(intent)
            }
        })

        EventChangeListener()
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
                        if(dc.type == DocumentChange.Type.ADDED && dc.document.data.getValue("status") == "requested") {
                            userArrayList.add((dc.document.toObject(user::class.java)))

                        }
                    }

                    myAdapter.notifyDataSetChanged()
                }
            })
    }
}

