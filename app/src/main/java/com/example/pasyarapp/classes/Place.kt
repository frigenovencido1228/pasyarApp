package com.example.pasyarapp.classes

data class Place(
    val id: String? = "",
    val location: String = "",
    val description: String = "",
    val name: String? = "",
    val category: Category? = Category("", "", ""),
    val imageUrl: String = ""
)
