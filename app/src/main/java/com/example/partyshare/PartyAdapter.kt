package com.example.partyshare
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView


class PartyAdapter(private val parties: MutableList<party>) : RecyclerView.Adapter<PartyAdapter.ViewHolder>() {

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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.party_row, parent, false)
        return ViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val party = parties[position]
        holder.name.text = party.name
        holder.ID.text = party.ID
    }

    override fun getItemCount() = parties.size

    class ViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.partyName)
        val ID: TextView = itemView.findViewById(R.id.partyID)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }


}