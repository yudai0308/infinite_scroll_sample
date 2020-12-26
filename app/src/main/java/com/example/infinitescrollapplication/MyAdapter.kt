package com.example.infinitescrollapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val listData: MutableList<String>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val textView = inflater.inflate(R.layout.list_item, parent, false) as TextView
        return MyViewHolder(textView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.textView.text = listData[position]
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    fun add(listData: List<String>) {
        this.listData += listData
        notifyDataSetChanged()

    }

    class MyViewHolder(val textView: TextView): RecyclerView.ViewHolder(textView)
}
