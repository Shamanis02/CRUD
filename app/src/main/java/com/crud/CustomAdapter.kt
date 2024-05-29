package com.crud

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crud.databinding.TvBinding

class MyAdapter(private val items: Map<String, TODOData>, private val activity: MainActivity) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    class MyViewHolder(b: TvBinding) :
        RecyclerView.ViewHolder(b.root) {
        var binding : TvBinding

        init {
            binding = b
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(TvBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val todo = items.toList()[position]

        holder.binding.todo=todo.second
        holder.binding.activity=activity
        holder.binding.key=todo.first
    }

    override fun getItemCount(): Int {
        return items.size
    }
}