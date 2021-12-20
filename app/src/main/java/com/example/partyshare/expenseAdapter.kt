package com.example.partyshare
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView


class expenseAdapter(private val expenses: MutableList<expense>) : RecyclerView.Adapter<expenseAdapter.ViewHolder>() {

    private lateinit var mListener : onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int) {

        }
    }

    fun setOnItemClickListener(listener: onItemClickListener)
    {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.expense_row, parent, false)
        return ViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = expenses[position]
        holder.expenseName.text = expense.expenseName
        holder.expenseValue.text = expense.expenseValue.toString()
        holder.addedBy.text = expense.addedBy
    }

    override fun getItemCount() = expenses.size

    class ViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {

        val expenseName: TextView = itemView.findViewById(R.id.expenseName)
        val expenseValue: TextView = itemView.findViewById(R.id.expenseValue)
        val addedBy: TextView = itemView.findViewById(R.id.addedBy)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }


}