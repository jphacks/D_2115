package com.totte

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/*
class MyAdapter : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
*/

class MyAdapter(private val myDataset: ArrayList<List<String?>>) :

    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val sender: TextView = view.findViewById(R.id.sender)
        val message: TextView = view.findViewById(R.id.message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
            .inflate(R.layout.mail, parent, false)
        return MyViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.message.text = myDataset[position][0]
        holder.sender.text = "from: " + myDataset[position][1]
    }

    override fun getItemCount() = myDataset.size
}
