package com.example.partyshare

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text
import java.util.*
import kotlin.collections.HashMap

class party_main : AppCompatActivity() {

    private lateinit var auth:FirebaseAuth
    private lateinit var db:FirebaseFirestore
    private var permissionGrantedFlag: Boolean = false

    val CHANNEL_ID = "channelID"
    val CHANNEL_NAME = "channelName"
    val NOTIFICATION_ID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_party_main)
        createNotifitactionChannel()

        val partyID = intent.getStringExtra("PARTY_ID").toString()
        val partyName = intent.getStringExtra("PARTY_NAME").toString()
        val fullName = intent.getStringExtra("FULLNAME")

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        checkLimit()

        val etWelcomeText = findViewById<TextView>(R.id.tvName)
        val toolbarName = findViewById<TextView>(R.id.etTollbarName)
        val tvBalance = findViewById<TextView>(R.id.tvMoney)
        val tvStatusValue = findViewById<TextView>(R.id.tvStatusValue)
        val btnAddExpense = findViewById<ImageView>(R.id.btnAddExpense)
        val btnMembers = findViewById<ImageView>(R.id.btnViewMembers)
        val btnScanExpense = findViewById<ImageView>(R.id.btnScanner)
        val btnConfirmPayment = findViewById<ImageView>(R.id.btnConfirmPayment)
        val btnBack = findViewById<ImageView>(R.id.onBackToMenu)
        val btnReceiptList = findViewById<ImageView>(R.id.ivReceiptList)
        val tvConfirmPayment = findViewById<TextView>(R.id.tvConfirmPayment)
        val tvStatusText = findViewById<TextView>(R.id.tvTransStat)

        permissionCheck(partyID, tvConfirmPayment)
        setInfo(etWelcomeText, toolbarName, partyName, partyID, tvBalance, tvStatusValue, tvStatusText)

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
                startActivity(intent)
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
            intent = Intent(this, receiptScanner::class.java)
            intent.putExtra("PARTY_ID", partyID)
                .putExtra("PARTY_NAME", partyName)
                .putExtra("FULLNAME", fullName)
            startActivity(intent)
        }

        btnConfirmPayment.setOnClickListener {
            if(!permissionGrantedFlag) {
                requestTransferConfirmation(partyID)
            } else {
                val intent = Intent(this, transferConfirmation::class.java)
                    .putExtra("PARTY_ID", partyID)
                    .putExtra("PARTY_NAME", partyName)
                startActivity(intent)
            }
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, partyList::class.java)
            startActivity(intent)
        }
        Log.e("FLAG?!", permissionGrantedFlag.toString())
        //setInfo(etWelcomeText, toolbarName, partyName, partyID, tvBalance, tvStatusValue, tvStatusText)

        refreshBalance(tvBalance, partyID)
    }

    private fun refreshBalance(tvBalance: TextView, partyID: String) {
        Thread.sleep(500)
        db.collection("parties").document(partyID).collection("members").document(auth.currentUser!!.uid)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    val text = it.result.data!!.getValue("balance").toString().toDouble()
                    val text2send = String.format("%.2f", text).toString()
                    tvBalance.text = text2send
                    findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.GONE;
                }
            }
        //findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.GONE;
    }

    private fun requestTransferConfirmation(partyID: String) {
        val currUserID = auth.currentUser!!.uid
        db.collection("parties").document(partyID).collection("members").document(currUserID)
            .update("transferStatus", "REQUESTED")

        Toast.makeText(this, "Request has been sent to host!", Toast.LENGTH_LONG).show()

        finish();
        overridePendingTransition(500, 500);
        startActivity(intent);
        overridePendingTransition(500, 500);
    }

    @SuppressLint("SetTextI18n")
    private fun permissionCheck(partyID: String, tvConfirmPayment: TextView) {
        val currUserID = auth.currentUser!!.uid
        var role: String = ""
        db.collection("parties").document(partyID).collection("members").document(currUserID)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    role = it.result.data!!.getValue("role").toString()
                    if(role != "host") {
                        permissionGrantedFlag = false
                        tvConfirmPayment.text = "Request transfer confirmation"
                    }
                    else {
                        permissionGrantedFlag = true
                        tvConfirmPayment.text = "Check members balance and confirm transfer"
                    }
                }
            }
    }

    private fun checkLimit() {
        db.collection("users").document(auth.currentUser!!.uid)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                   val currSpendings = it.result.data!!.getValue("monthlySpend").toString().toDouble()
                   val userLimit = it.result.data!!.getValue("limitValue").toString().toDouble()

                   if(currSpendings > userLimit) {
                       val intent = Intent(this, statsView::class.java)
                       val pendingIntent = TaskStackBuilder.create(this).run {
                           addNextIntentWithParentStack(intent)
                           getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
                       }

                       val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                           .setContentTitle("You are over your monthly budget!")
                           .setContentText("Check out statistics in your App or click here")
                           .setContentIntent(pendingIntent)
                           .setSmallIcon(R.drawable.ic_warning)
                           .setPriority(NotificationCompat.PRIORITY_HIGH)
                           .build()

                       val notificationManager = NotificationManagerCompat.from(this)
                       notificationManager.notify(NOTIFICATION_ID, notification)

                   }
                }
            }
    }

    private fun createNotifitactionChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH).apply {
                lightColor = Color.RED
                enableLights(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun updateDatabase(partyID: String, value: Double) {
        db.collection("parties").document(partyID)
            .get()
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    val membersQty = task.result.data!!.getValue("membersQty").toString().toInt()
                    val perMember: Double = Math.round(value/membersQty * 100.0) / 100.0

                    db.collection("parties").document(partyID).collection("members")
                        .get()
                        .addOnCompleteListener {
                            if(it.isSuccessful) {
                                for(document in it.result) {
                                    val uID = document.data.getValue("uID").toString()
                                    val currBalance = document.data.getValue("balance").toString().toDouble()
                                    var solution = currBalance - perMember
                                    solution = String.format("%.2f", solution).toDouble()
                                    db.collection("parties").document(partyID).collection("members").document(uID)
                                        .update("balance", solution)

                                    db.collection("users").document(uID)
                                        .get()
                                        .addOnCompleteListener {
                                            if(it.isSuccessful){
                                                val currMonthlySpendings = it.result.data!!.getValue("monthlySpend").toString().toDouble()
                                                val updatedMonthlySpendings = currMonthlySpendings + perMember
                                                Log.e("Updated in func:", updatedMonthlySpendings.toString())
                                                db.collection("users").document(uID)
                                                    .update("monthlySpend", updatedMonthlySpendings)
                                            }
                                        }
                                }
                            }
                        }
                }
            }
    }

    private fun addExpense(name: String, cost: Double, partyID: String) {
        val updateParty: MutableMap<String, Any> = HashMap()
        val updateUserBalance: MutableMap<String, Any> = HashMap()
        val updateExpenseList: MutableMap<String, Any> = HashMap()
        var fullname: String
        val currUser = auth.currentUser
        db.collection("parties")
            .whereEqualTo("ID", partyID)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    for(document in it.result) {
                        var total: Double = document.data.getValue("total").toString().toDouble()
                        total += cost
                        val solution2: Double = String.format("%.2f", total).toDouble()
                        updateParty["total"] = solution2
                        updateDatabase(partyID, cost)
                    }
                    db.collection("parties").document(partyID)
                        .update(updateParty)
                }
            }
        db.collection("parties").document(partyID).collection("members")
            .whereEqualTo("uID", currUser!!.uid)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    for(document in it.result) {
                        var balance: Double = document.data.getValue("balance").toString().toDouble()
                        balance += cost
                        val solution: Double = String.format("%.2f", balance).toDouble()
                        updateUserBalance["balance"] = solution
                    }
                    db.collection("parties").document(partyID).collection("members").document(
                        currUser.uid)
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
                    for(document in it.result) {
                        val firstName = document.data.getValue("firstName").toString()
                        val lastName = document.data.getValue("lastName").toString()
                        fullname = "$firstName $lastName"
                        updateExpenseList["addedBy"] = fullname
                        db.collection("parties").document(partyID)
                            .collection("expenses").document(expenseID)
                            .set(updateExpenseList)

                    }
                }
            }

        finish();
        overridePendingTransition(1, 1);
        startActivity(intent);
        overridePendingTransition(1, 1);

    }

    @SuppressLint("SetTextI18n")
    private fun setInfo(etWelcomeText: TextView, toolbarName: TextView, partyName: String, partyID: String, tvBalance: TextView, transferStatusValue: TextView, transferStatusText: TextView) {
        val currUser = auth.currentUser
        db.collection("users")
            .whereEqualTo("uID", currUser!!.uid)
            .get()
            .addOnCompleteListener { it
                val firstName = StringBuffer()
                if(it.isSuccessful) {
                    for(document in it.result){
                        firstName.append(document.data.getValue("firstName")).append(" ")
                    }
                    etWelcomeText.text = "Hello $firstName!"
                }
            }

        toolbarName.text = partyName

        db.collection("parties").document(partyID).collection("members")
            .whereEqualTo("uID", currUser.uid)
            .get()
            .addOnCompleteListener {
                val balance = StringBuilder()
                if(it.isSuccessful) {
                    Log.e("in if", "in if")
                    for (document in it.result) {
                        if(document.data.getValue("transferStatus") != "null") {
                            val transferStatus = document.data.getValue("transferStatus")
                            transferStatusText.text = "Transfer status"
                            transferStatusValue.text = transferStatus.toString()
                        }
                        val balance2send = document.data.getValue("balance").toString().toDouble()
                        val formattedBalance = String.format("%.2f", balance2send).toDouble()
                        balance.append(formattedBalance).append(" zł")
                    }
                //tvBalance.text = balance
                }

            }

    }
}