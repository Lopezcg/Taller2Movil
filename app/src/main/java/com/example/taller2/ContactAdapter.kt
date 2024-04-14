package com.example.taller2

import android.content.Context
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactAdapter(private val context: Context, private val contactList: List<Contact>) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contactIcon: ImageView = view.findViewById(R.id.imageView)
        val contactid: TextView = view.findViewById(R.id.text1)
        val contactname: TextView = view.findViewById(R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.adaptercontacto, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contactList[position]
        holder.contactid.text = contact.id.toString()
        holder.contactname.text = contact.name
    }

    override fun getItemCount(): Int = contactList.size
}

data class Contact(val id: Long, val name: String)
