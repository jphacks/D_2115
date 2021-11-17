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

internal const val MY_MESSAGE = 0
internal const val OPPONENT_MESSAGE = 1

class MyAdapter(private val myDataset: ArrayList<Pair<String, Boolean>>) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val sender: TextView = view.findViewById(R.id.sender)
        val message: TextView = view.findViewById(R.id.message)
    }

    override fun getItemViewType(position: Int): Int {
        if(myDataset[position].second)
            return MY_MESSAGE
        else
            return OPPONENT_MESSAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        if(viewType == MY_MESSAGE){
            val inflatedView = LayoutInflater.from(parent.context)
                .inflate(R.layout.my_message, parent, false)
            return MyViewHolder(inflatedView)
        }else{
            val inflatedView = LayoutInflater.from(parent.context)
                .inflate(R.layout.opponent_message, parent, false)
            return MyViewHolder(inflatedView)
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.message.text = myDataset[position].first
        //holder.sender.text = "from: " + myDataset[position][1]
    }

    override fun getItemCount() = myDataset.size

}
