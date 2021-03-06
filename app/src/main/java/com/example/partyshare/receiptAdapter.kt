package com.example.partyshare
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView


class receiptAdapter(private val receipts: MutableList<Receipt>) : RecyclerView.Adapter<receiptAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.receipt_row, parent, false)
        return ViewHolder(view)
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val receipt = receipts[position]
        holder.expenseName.setText(receipt.expenseName)
        holder.expenseValue.setText(receipt.expenseValue.toString())
    }

    override fun getItemCount() = receipts.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val expenseName: EditText = itemView.findViewById(R.id.expenseNameEt)
        val expenseValue: EditText = itemView.findViewById(R.id.expenseValueEt)
    }



}
