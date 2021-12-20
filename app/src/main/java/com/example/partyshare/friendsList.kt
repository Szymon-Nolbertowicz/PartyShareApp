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
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.auth.User

class friendsList : AppCompatActivity() {

    private lateinit var auth:FirebaseAuth
    private lateinit var database:FirebaseFirestore
    private lateinit var userArrayList: ArrayList<user>
    private lateinit var myAdapter: UsersAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_list)

        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val recyclerView = findViewById(R.id.recycleViewA) as RecyclerView
        val btnBack = findViewById<ImageView>(R.id.onBackToMenu)
        val addUser = findViewById<FloatingActionButton>(R.id.addUserBtn)
        val btnInvitations = findViewById<FloatingActionButton>(R.id.invitationsUserBtn)

        btnBack.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
        }

        btnInvitations.setOnClickListener {
            val intent = Intent(this, friendsRequestsList::class.java)
            startActivity(intent)
        }

        userArrayList = arrayListOf()
        myAdapter = UsersAdapter(userArrayList)

        /*for (i in 0..100) {
            users.add(user("First Name #" + i, "Last Name #" + i, "usermail@example.com"))
            users[i].LastName?.let { Log.e("user", it) }
        }*/

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@friendsList)
            adapter = myAdapter
        }

        EventChangeListener()

        addUser.setOnClickListener {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Add Friend")
                val view = layoutInflater.inflate(R.layout.dialog_add_friend,null)
                val etFriendMail = view.findViewById<EditText>(R.id.etFriendEmail)
                builder.setView(view)
                builder.setPositiveButton("Add", DialogInterface.OnClickListener { _, _ ->
                    val email = etFriendMail.text.toString()
                    friendRequest(email)

                })
                builder.setNegativeButton("Close", DialogInterface.OnClickListener { _, _ -> })
                builder.show()
        }

    }

    private fun EventChangeListener(){
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

                    for (dc: DocumentChange in value?.documentChanges!! ){
                        if(dc.type == DocumentChange.Type.ADDED && dc.document.data.getValue("status") == "friends") {
                            userArrayList.add((dc.document.toObject(user::class.java)))
                        }
                    }

                    myAdapter.notifyDataSetChanged()
                }
            })

    }

    private fun friendRequest(email: String){
        val requestStatusRec: MutableMap<String, Any> = HashMap()
        val currUser = auth.currentUser!!
        var friendUID: String = "x"
        database.collection("users")
            .get()
            .addOnCompleteListener {
                val mail: StringBuffer = StringBuffer()
                if(it.isSuccessful) {
                    for(document in it.result!!) {
                        if(document.data.getValue("uID").toString() == currUser.uid) {
                            requestStatusRec["firstName"] = document.data.getValue("firstName")
                            requestStatusRec["lastName"] = document.data.getValue("lastName")
                            requestStatusRec["uID"] = currUser.uid
                            Log.e("REQUESTED1", requestStatusRec.toString())
                        }

                        Log.e("REQUESTED2", requestStatusRec.toString())
                        database.collection("users").document(friendUID).collection("friends").document(currUser.uid)
                            .update(requestStatusRec)

                        if(document.data.getValue("email").toString() == email ) {
                            Log.d("User UID", document.data.getValue("uID").toString())
                            friendUID = document.data.getValue("uID").toString()
                            var user = auth.currentUser
                            Log.e("REQUESTED3", requestStatusRec.toString())
                            if(user != null)
                            {
                                val requestStatus: MutableMap<String, Any> = HashMap()
                                if(friendUID.isNotEmpty()) {
                                    Log.e("REQUESTED 4", requestStatusRec.toString())
                                    requestStatus["status"] = "pending"
                                    requestStatus["firstName"] = document.data.getValue("firstName")
                                    requestStatus["lastName"] = document.data.getValue("lastName")
                                    requestStatus["uID"] = friendUID
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

