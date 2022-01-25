package com.example.partyshare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class receiptDataSend : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var expenseArrayList: ArrayList<Receipt>
    private lateinit var myAdapter: receiptAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt_data_send)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        val ArrayAsString = getIntent().getExtras()!!.getString("Array")
        val partyID = getIntent().getStringExtra("PARTY_ID").toString()
        val partyName = getIntent().getStringExtra("PARTY_NAME").toString()
        val fullName = getIntent().getStringExtra("FULLNAME").toString()

        val typeToken = object : TypeToken<ArrayList<Receipt>>() {}.type

        val recyclerView = findViewById<RecyclerView>(R.id.recycleViewDataSend)
        val btnBack = findViewById<ImageView>(R.id.onBackToMenu)
        val btnConfirmSend = findViewById<Button>(R.id.btnSend)
        val etName = findViewById<EditText>(R.id.expenseNameEt)
        val etValue = findViewById<EditText>(R.id.expenseValueEt)

        btnBack.setOnClickListener {
            val intent = Intent(this, receiptScanner::class.java)
                .putExtra("PARTY_ID", partyID)
                .putExtra("PARTY_NAME", partyName)
                .putExtra("FULLNAME", fullName)
            startActivity(intent)
        }


        expenseArrayList = Gson().fromJson(ArrayAsString, typeToken)
        myAdapter = receiptAdapter(expenseArrayList as MutableList<Receipt>)
        var adapter = myAdapter


        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@receiptDataSend)
        }
        recyclerView.adapter = adapter

        btnConfirmSend.setOnClickListener {
                //Log.e("EditText", etValue.text.toString())
                sendData(expenseArrayList, partyID, fullName, partyName)

        }

    }

    private fun sendData(arrayOfExpenses: ArrayList<Receipt>, partyID: String, fullName: String, partyName: String) {

        //Upload confirmed data to FirebaseFirestore
        var currUserUID = auth.currentUser!!.uid
        val dataToSend: MutableMap<String, Any> = HashMap()
        var sum = 0.0
        for(i in 0 until arrayOfExpenses.size) {
            dataToSend["expenseName"] = arrayOfExpenses[i].expenseName.toString()
            dataToSend["expenseValue"] = arrayOfExpenses[i].expenseValue!!.toDouble()
            sum += arrayOfExpenses[i].expenseValue!!.toDouble()
            val id = arrayOfExpenses[i].expenseID.toString()
            dataToSend["ID"] = arrayOfExpenses[i].expenseID.toString()
            dataToSend["addedBy"] = fullName
            Log.e("Data Sent:", dataToSend.toString())
            database.collection("parties").document(partyID).collection("expenses").document(id)
                .set(dataToSend)

            //Update total cost of party

            database.collection("parties").document(partyID)
                .get()
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        val currTotal = it.result.data!!.getValue("total").toString().toDouble()
                        val sumToSend = currTotal + sum
                        val solution: Double = String.format("%.2f", sumToSend).toDouble()
                        database.collection("parties").document(partyID)
                            .update("total", solution)
                    }
                }

            //Update balance of adding member

            database.collection("parties").document(partyID).collection("members").document(currUserUID)
                .get()
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        val currBalance = it.result.data!!.getValue("balance").toString().toDouble()
                        val balanceToSend = currBalance + sum
                        val solution: Double = String.format("%.2f", balanceToSend).toDouble()
                        Log.e("SUM SUM SUM", sum.toString())
                        database.collection("parties").document(partyID).collection("members").document(currUserUID)
                            .update("balance", solution)
                    }
                }


        }
        Toast.makeText(this, "Data sent", Toast.LENGTH_LONG).show()
        updateDatabase(partyID, sum)
        val intent = Intent(this, party_main::class.java)
            .putExtra("PARTY_NAME", partyName)
            .putExtra("PARTY_ID", partyID)
            .putExtra("FULLNAME", fullName)
        startActivity(intent)

    }
    private fun updateDatabase(partyID: String, sum:Double) {
        database.collection("parties").document(partyID)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    val total = it.result.data!!.getValue("total").toString().toDouble()
                    val membersQty = it.result.data!!.getValue("membersQty").toString().toInt()
                    Log.e("MembersQty?", membersQty.toString())
                    val perMember: Double = Math.round(sum/membersQty * 100.0) / 100.0
                    Log.e("perMember", perMember.toString())


                    database.collection("parties").document(partyID).collection("members")
                        .get()
                        .addOnCompleteListener {
                            if(it.isSuccessful) {
                                for(document in it.result) {
                                    val currBalance = document.data.getValue("balance").toString().toDouble()
                                    val ID = document.data.getValue("uID").toString()
                                    Log.e("currBalance", currBalance.toString())
                                    var solution = currBalance - perMember
                                    solution = String.format("%.2f", solution).toDouble()
                                    Log.e("balance poprawiony", solution.toString())

                                    //Pozniej sprawdz czy da sie to sprowadzic do latwirjszej postaci

                                    database.collection("parties").document(partyID).collection("members").document(ID)
                                        .update("balance", solution)
                                    Log.e("balance poprawiony", solution.toString())

                                    database.collection("users").document(ID)
                                        .get()
                                        .addOnCompleteListener {
                                            if(it.isSuccessful){
                                                val currMonthlySpendings = it.result.data!!.getValue("monthlySpend").toString().toDouble()
                                                val updatedMonthlySpendings = currMonthlySpendings + perMember
                                                database.collection("users").document(ID)
                                                    .update("monthlySpend", updatedMonthlySpendings)
                                            }
                                        }
                                }

                            }
                        }
                }
            }
    }
}