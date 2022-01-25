package com.example.partyshare

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.HashMap

class Dashboard : AppCompatActivity() {

    private lateinit var auth:FirebaseAuth
    private lateinit var database:FirebaseFirestore

    private val CHANNEL_ID = "channelID"
    private val CHANNEL_NAME = "channelName"
    private val NOTIFICATION_ID = 1

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        createNotificationChannel()

        val toProfile = findViewById<ImageView>(R.id.navToProfile)
        val btnFriendsList = findViewById<TextView>(R.id.btnFriendsList)
        val btnCreateParty = findViewById<TextView>(R.id.btnCreate)
        val btnPartyList = findViewById<TextView>(R.id.btnPartyList)
        val btnStatistics = findViewById<TextView>(R.id.btnStats)

        resetStatsMonthly()

        btnPartyList.setOnClickListener {
            var fullName: String
            database.collection("users").document(auth.currentUser!!.uid)
                .get()
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        val firstName = it.result.data!!.getValue("firstName").toString()
                        val lastName = it.result.data!!.getValue("lastName").toString()
                        fullName = "$firstName $lastName"
                        val intent = Intent(this,partyList::class.java)
                            .putExtra("FULLNAME", fullName)
                        startActivity(intent)
                    }
                }
        }

        toProfile.setOnClickListener {
            val intent = Intent(this, userCreate::class.java)
            startActivity(intent)
        }

        btnFriendsList.setOnClickListener {
            val intent = Intent(this, friendsList::class.java)
            startActivity(intent)
        }

        btnStatistics.setOnClickListener {
            val intent = Intent(this, statsView::class.java)
            startActivity(intent)
        }

        btnCreateParty.setOnClickListener {
            val newUUID = UUID.randomUUID().toString()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Create a Party")
            val view = layoutInflater.inflate(R.layout.dialog_create_party,null)
            val etFriendMail = view.findViewById<EditText>(R.id.etPartyName)
            builder.setView(view)
            builder.setPositiveButton("Add", DialogInterface.OnClickListener { _, _ ->
                val name = etFriendMail.text.toString()
                partyCreate(name, newUUID)
                Toast.makeText(this, "Party created!", Toast.LENGTH_SHORT).show()

            })
            builder.setNegativeButton("Close", DialogInterface.OnClickListener { _, _ -> })
            builder.show()
        }

        checkInvitations()

    }

    private fun checkInvitations() {

        database.collection("users").document(auth.currentUser!!.uid).collection("friends")
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    var counter = 0
                    for(doc in it.result) {
                        if(doc.data.getValue("status") == "requested") {
                            counter++
                        }
                    }
                    if(counter > 0) {
                        val intent = Intent(this, friendsRequestsList::class.java)
                        val pendingIntent = TaskStackBuilder.create(this).run {
                            addNextIntentWithParentStack(intent)
                            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
                        }

                        val invitation: String = if(counter > 1) {
                            "invitations"
                        } else {
                            "invitation"
                        }

                        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                            .setContentTitle("You have $counter new $invitation pending.")
                            .setContentText("Check out your friends list inside app or click here.")
                            .setContentIntent(pendingIntent)
                            .setSmallIcon(R.drawable.ic_person)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .build()

                        val notificationManager = NotificationManagerCompat.from(this)
                        notificationManager.notify(NOTIFICATION_ID, notification)
                    }
                }
            }
    }

    private fun createNotificationChannel() {
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

    private fun resetStatsMonthly() {
        val c = Calendar.getInstance()
        val day = c.get(Calendar.DAY_OF_MONTH)

        if(day == 1) {
            database.collection("users")
                .get()
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        for (user in it.result) {
                            database.collection("users").document(user.data.getValue("uID").toString())
                                .update("monthlySpend", 0)
                        }
                    }
                }
        }

    }


    private fun partyCreate(name: String, partyID: String) {
        val currUser = auth.currentUser
        val party: MutableMap<String, Any> = HashMap()
        party["name"] = name
        party["ID"] = partyID
        party["total"] = 0
        party["membersQty"] = 1

        database.collection("parties").document(partyID)
            .set(party)


        val host: MutableMap<String, Any> = HashMap()

        database.collection("users")
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful)
                {
                    for(document in it.result)
                    {
                        if(document.data.getValue("uID") == currUser!!.uid)
                        {
                            host["firstName"] = document.data.getValue("firstName")
                            host["lastName"] = document.data.getValue("lastName")
                            host["uID"] = currUser.uid
                            host["role"] = "host"
                            host["balance"] = 0
                            host["transferStatus"] = "null"

                            database.collection("parties").document(partyID).
                            collection("members").document(currUser.uid)
                                .set(host)

                        }
                    }
                }
            }

        database.collection("users").document(currUser!!.uid).collection("parties").document(partyID)
            .set(party)
    }


}