package com.example.partyshare
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView


class UsersInvAdapter(private val users: MutableList<user>) : RecyclerView.Adapter<UsersInvAdapter.ViewHolder>() {

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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_row_inv, parent, false)
        return ViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.firstName.text = user.FirstName
        holder.lastName.text = user.LastName
        holder.uID.text = user.uID
    }

    override fun getItemCount() = users.size

    class ViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {

        val firstName: TextView = itemView.findViewById(R.id.firstNameB)
        val lastName: TextView = itemView.findViewById(R.id.lastNameB)
        val uID: TextView = itemView.findViewById(R.id.userid)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }


}