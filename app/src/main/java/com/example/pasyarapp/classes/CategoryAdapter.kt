package com.example.pasyarapp.classes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pasyarapp.R

class CategoryAdapter(
    private val categoryList: ArrayList<Category>,
    private val context: Context,
    private val onItemClick: OnItemClick
) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon = itemView.findViewById<ImageView>(R.id.ivIcon)
        val name = itemView.findViewById<TextView>(R.id.tvName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_category, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val URL =
            "https://firebasestorage.googleapis.com/v0/b/pasyarapp.appspot.com/o/file%2F"
        val EXTENSION = "?alt=media&token=5e3396be-7718-45ad-8e22-81c469bfebf9"

        val currentItem = categoryList[position]
        holder.name.text = currentItem.name
        Glide.with(context).load(URL + currentItem.icon + EXTENSION).into(holder.icon)

        holder.itemView.setOnClickListener(View.OnClickListener {
            onItemClick.onCategoryClick(currentItem)
        })
    }
}