package com.example.partyshare
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView


class UsersAdapter(private val users: MutableList<user>) : RecyclerView.Adapter<UsersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.firstName.text = user.FirstName
        holder.lastName.text = user.LastName
    }

    override fun getItemCount() = users.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val firstName: TextView = itemView.findViewById(R.id.firstNameB)
        val lastName: TextView = itemView.findViewById(R.id.lastNameB)
    }

}
