package com.example.pasyarapp.classes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import com.example.pasyarapp.R
import org.w3c.dom.Text

class PlacesAdapter(private val placesList: ArrayList<Place>) :
    RecyclerView.Adapter<PlacesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.tvName)
        val icon: ImageView = itemView.findViewById(R.id.ivIcon)
        val loc: TextView = itemView.findViewById(R.id.tvLoc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_places, parent, false)

        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return placesList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = placesList[position]

        holder.name.text = currentItem.name
        holder.loc.text = currentItem.category?.icon
    }
}