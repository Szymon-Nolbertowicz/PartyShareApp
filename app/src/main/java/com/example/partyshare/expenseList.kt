package com.example.partyshare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class expenseList : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var expenseArrayList: ArrayList<expense>
    private lateinit var myAdapter: expenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_list)
        val partyID = getIntent().getStringExtra("PARTY_ID").toString()
        val partyName = getIntent().getStringExtra("PARTY_NAME").toString()

        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val btnBack = findViewById<ImageView>(R.id.onBackToMenu)
        val recyclerView = findViewById<RecyclerView>(R.id.recycleViewExpenses)
        val etToolbar = findViewById<TextView>(R.id.elTV)

        setInfo(etToolbar, partyID)

        btnBack.setOnClickListener {
            intent = Intent(this, party_main::class.java)
            intent.putExtra("PARTY_ID", partyID)
                .putExtra("PARTY_NAME", partyName)
            startActivity(intent)
        }

        expenseArrayList = arrayListOf()
        myAdapter = expenseAdapter(expenseArrayList)
        var adapter = myAdapter


        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@expenseList)
        }

        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : expenseAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                val clickedItem: String = expenseArrayList[position].expenseName.toString()
                Log.e("user position", clickedItem.toString())
            }
        })

        EventChangeListener(partyID)

    }

    private fun setInfo(etToolbar: TextView, partyID: String) {
        database.collection("parties").document(partyID)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    val total = it.result.data!!.getValue("total")
                    etToolbar.text = "TOTAL PARTY COST: " + total.toString()
                }
            }

    }

    private fun EventChangeListener(partyID: String) {
            val currUser = auth.currentUser!!
            database.collection("parties").document(partyID).collection("expenses")
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
                                expenseArrayList.add((dc.document.toObject(expense::class.java)))
                                Log.e("lsita", expenseArrayList.toString())

                            }
                        }

                        myAdapter.notifyDataSetChanged()
                    }
                })

    }


}