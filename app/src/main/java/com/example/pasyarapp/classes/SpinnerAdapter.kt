package com.example.pasyarapp.classes

import android.content.Context
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.pasyarapp.R

class SpinnerAdapter(context: Context, categoryList: ArrayList<Category>) :
    ArrayAdapter<Category>(context, 0, categoryList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    private fun initView(position: Int, convertView: View?, parent: ViewGroup): View {

        val URL =
            "https://firebasestorage.googleapis.com/v0/b/pasyarapp.appspot.com/o/file%2F"
        val EXTENSION = "?alt=media&token=5e3396be-7718-45ad-8e22-81c469bfebf9"
        val category = getItem(position)

        val view = LayoutInflater.from(context).inflate(R.layout.layout_category, parent, false)

        view.findViewById<TextView>(R.id.tvName).text = category?.name?.uppercase()
        val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)

        Glide.with(context).load("$URL${category?.icon}$EXTENSION").into(ivIcon)

        return view
    }
}