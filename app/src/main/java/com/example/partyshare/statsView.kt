package com.example.partyshare

import android.annotation.SuppressLint
import android.content.ClipDescription
import android.content.DialogInterface
import android.content.Intent
import android.media.Image
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
import kotlin.math.abs

class statsView : AppCompatActivity() {

    private lateinit var auth:FirebaseAuth
    private lateinit var database:FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats_view)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        val tvSpending = findViewById<TextView>(R.id.tvValue)
        val tvBelowOver = findViewById<TextView>(R.id.tvValuBelow)
        val textUnder = findViewById<TextView>(R.id.tvBelowOver)
        val btnBack = findViewById<ImageView>(R.id.onBackToMenu)
        val btnChangeLimit = findViewById<ImageView>(R.id.ivChangeLimit)
        val ivScoreIcon = findViewById<ImageView>(R.id.ivScoreIcon)
        val tvScore = findViewById<TextView>(R.id.tvScore)
        val tvDescription = findViewById<TextView>(R.id.tvDescription)
        val tvCurrentLimit = findViewById<TextView>(R.id.tvCurrentLimit)

        btnBack.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
        }

        btnChangeLimit.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Expense")
            val view = layoutInflater.inflate(R.layout.dialog_change_limit,null)
            val etNewLimit = view.findViewById<EditText>(R.id.etLimitChange)
            builder.setView(view)
            builder.setPositiveButton("Change", DialogInterface.OnClickListener { _, _ ->
                val newLimit = etNewLimit.text.toString().toDouble()
                changeLimit(newLimit)
                Thread.sleep(1000)
                finish()
                startActivity(intent)
                Toast.makeText(this, "Limit changed!", Toast.LENGTH_SHORT).show()

            })
            builder.setNegativeButton("Close", DialogInterface.OnClickListener { _, _ -> })
            builder.show()
        }



        countDiffer(tvBelowOver, textUnder, tvSpending, ivScoreIcon, tvScore, tvDescription, tvCurrentLimit)


    }

    private fun changeLimit(newLimit: Double) {
        database.collection("users").document(auth.currentUser!!.uid)
            .update("limitValue", newLimit)
    }

    @SuppressLint("SetTextI18n")
    private fun countDiffer(tvBeloweOver: TextView, tvTextUnder: TextView, tvSpending: TextView, ivScoreIcon: ImageView, tvScore: TextView, tvDescription: TextView, tvCurrentLimit: TextView) {
        val currUserID = auth.currentUser!!.uid
        database.collection("users").document(currUserID)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                   val limit =  it.result.data!!.getValue("limitValue").toString().toDouble()
                    val limit2show = String.format("%.2f", limit).toString().toDouble()
                    tvCurrentLimit.text = "CURRENT LIMIT: ${limit2show.toString()} zł"
                    val spend = it.result.data!!.getValue("monthlySpend").toString().toDouble()
                    val spend2show = String.format("%.2f", spend).toString().toDouble()
                    val solution2format = limit - spend
                    val solution = String.format("%.2f", solution2format).toString().toDouble()
                    tvSpending.text = "${spend2show.toString()} zł"
                    if(solution < 0) {
                        val solutionSubmit = abs(solution)
                        tvBeloweOver.text = "${solutionSubmit.toString()} zł"
                        tvTextUnder.text = "over the limit!"
                    } else {
                        tvBeloweOver.text = "${solution.toString()} zł"
                        tvTextUnder.text = "under the limit!"
                    }
                    scoreCount(ivScoreIcon, tvScore, tvDescription, spend, limit)
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun scoreCount(ivIcon: ImageView, tvScore: TextView, tvDescription: TextView, spendings: Double, limit: Double) {
        val procentage = ((spendings/limit)*100).toInt()
        Log.e("procentage: ", procentage.toString())
        if(procentage < 30) {
            ivIcon.setImageResource(R.drawable.ic_happy)
            tvDescription.text = "Your money management\nis very good."
        } else if (procentage in 30..70) {
            ivIcon.setImageResource(R.drawable.ic_neutral)
            tvDescription.text = "Your money management\nis average."
        } else if (procentage in 71..100) {
            ivIcon.setImageResource(R.drawable.ic_nothappy)
            tvDescription.text = "Your money management\nis bad. Be careful!"
        } else if (procentage > 100) {
            ivIcon.setImageResource(R.drawable.ic_warning)
            tvDescription.text = "You are over the limit!\nYou shouldn't spend more!"
        }
        if(procentage > 100) {
            tvScore.text = "Score: 0/100"
        }else {
            tvScore.text = "Score: ${(100-procentage).toString()}/100"
        }
    }
}