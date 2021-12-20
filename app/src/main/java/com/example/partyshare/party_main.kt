package com.example.partyshare

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class party_main : AppCompatActivity() {

    private lateinit var auth:FirebaseAuth
    private lateinit var db:FirebaseFirestore
    private lateinit var arrayUpdate: ArrayList<Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_party_main)

        val partyID = getIntent().getStringExtra("PARTY_ID").toString()
        val partyName = getIntent().getStringExtra("PARTY_NAME").toString()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currUser = auth.currentUser
        val etWelcomeText = findViewById<TextView>(R.id.tvName)
        val toolbarName = findViewById<TextView>(R.id.etTollbarName)
        val tvBalance = findViewById<TextView>(R.id.tvMoney)

        updateDatabase(partyID)
        setInfo(etWelcomeText, toolbarName, partyName, partyID, tvBalance)

        val btnAddExpense = findViewById<ImageView>(R.id.btnAddExpense)
        val btnMembers = findViewById<ImageView>(R.id.btnViewMembers)
        val btnScanExpense = findViewById<ImageView>(R.id.btnScanner)
        val btnConfirmPayment = findViewById<ImageView>(R.id.btnConfirmPayment)
        val btnBack = findViewById<ImageView>(R.id.onBackToMenu)
        val btnReceiptList = findViewById<ImageView>(R.id.ivReceiptList)


        btnReceiptList.setOnClickListener {
            intent = Intent(this, expenseList::class.java)
            intent.putExtra("PARTY_ID", partyID)
                .putExtra("PARTY_NAME", partyName)
            startActivity(intent)
        }

        btnAddExpense.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Expense")
            val view = layoutInflater.inflate(R.layout.dialog_add_expense,null)
            val etExpenseName = view.findViewById<EditText>(R.id.etExpenseName)
            val etExpenseCost = view.findViewById<EditText>(R.id.etCost)
            builder.setView(view)
            builder.setPositiveButton("Add", DialogInterface.OnClickListener { _, _ ->
                val name = etExpenseName.text.toString()
                val cost = etExpenseCost.text.toString().trim().toDouble()
                addExpense(name, cost, partyID)
                Thread.sleep(1000)
                finish()
                startActivity(getIntent())
                Toast.makeText(this, "Expense added!", Toast.LENGTH_SHORT).show()

            })
            builder.setNegativeButton("Close", DialogInterface.OnClickListener { _, _ -> })
            builder.show()
        }

        btnMembers.setOnClickListener {
            intent = Intent(this, membersList::class.java)
            intent.putExtra("PARTY_ID", partyID)
                .putExtra("PARTY_NAME", partyName)
            startActivity(intent)
        }

        btnScanExpense.setOnClickListener {
            TODO("Not implemented yet")
        }

        btnConfirmPayment.setOnClickListener {
            TODO("Not implemented yet")
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, partyList::class.java)
            startActivity(intent)
        }

    }

    private fun updateDatabase(partyID: String) {
        var ref = db.collection("parties").document(partyID).collection("members")
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    arrayUpdate.add(it.result.size())
                    Log.e("SIZE", it.result.size().toString())
                }
            }

        db.collection("parties").document(partyID)
            .get()
            .addOnCompleteListener {
                arrayUpdate.add(it.result.data!!.getValue("total"))
                Log.e("SIZE - total", arrayUpdate.toString())
            }

    }

    private fun addExpense(name: String, cost: Double, partyID: String) {
        var updateParty: MutableMap<String, Any> = HashMap()
        var updateUserBalance: MutableMap<String, Any> = HashMap()
        var updateExpenseList: MutableMap<String, Any> = HashMap()
        var fullname: String = ""
        val currUser = auth.currentUser
        db.collection("parties")
            .whereEqualTo("ID", partyID)
            .get()
            .addOnCompleteListener {

                if(it.isSuccessful) {
                    for(document in it.result!!) {
                        var total: Double = document.data.getValue("total").toString().toDouble()
                        total = total + cost
                        val solution2: Double = String.format("%.2f", total).toDouble()
                        updateParty["total"] = solution2
                    }
                    db.collection("parties").document(partyID)
                        .update(updateParty)
                }
            }

        db.collection("parties").document(partyID).collection("members")
            .whereEqualTo("uID", currUser.uid)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    for(document in it.result!!) {
                        var balance: Double = document.data.getValue("balance").toString().toDouble()
                        balance = balance + cost
                        val solution: Double = String.format("%.2f", balance).toDouble()
                        updateUserBalance["balance"] = solution
                    }
                    db.collection("parties").document(partyID).collection("members").document(currUser.uid)
                        .update(updateUserBalance)
                }
            }
        val expenseID = UUID.randomUUID().toString()
        updateExpenseList["ID"] = expenseID
        updateExpenseList["expenseName"] = name
        updateExpenseList["expenseValue"] = cost

        db.collection("users")
            .whereEqualTo("uID", currUser.uid)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    for(document in it.result!!) {
                        val firstName = document.data.getValue("firstName").toString()
                        val lastName = document.data.getValue("lastName").toString()
                        fullname = firstName + " " + lastName
                        updateExpenseList["addedBy"] = fullname
                        db.collection("parties").document(partyID).collection("expenses").document(expenseID)
                            .set(updateExpenseList)

                    }
                }
            }

    }

    private fun setInfo(etWelcomeText: TextView, toolbarName: TextView, partyName: String, partyID: String, tvBalance: TextView) {
        val currUser = auth.currentUser
        db.collection("users")
            .whereEqualTo("uID", currUser.uid)
            .get()
            .addOnCompleteListener { it
                val firstName: StringBuffer = StringBuffer()
                if(it.isSuccessful) {
                    for(document in it.result!!){
                        firstName.append(document.data.getValue("firstName")).append(" ")
                    }
                    etWelcomeText.text = "Hello " + firstName + "!"
                }
            }

        toolbarName.text = partyName

        db.collection("parties").document(partyID).collection("members")
            .whereEqualTo("uID", currUser.uid)
            .get()
            .addOnCompleteListener {
                val balance: StringBuffer = StringBuffer()
                if(it.isSuccessful) {
                    Log.e("in if", "in if")
                    for (document in it.result!!) {
                        var data = document.data.getValue("balance").toString()
                        balance.append(document.data.getValue("balance")).append(" zł")
                    }
                tvBalance.text = balance
                }

            }

    }
}