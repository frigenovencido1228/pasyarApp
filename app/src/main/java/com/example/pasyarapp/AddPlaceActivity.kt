package com.example.pasyarapp

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pasyarapp.classes.Category
import com.example.pasyarapp.classes.CategoryAdapter
import com.example.pasyarapp.classes.OnItemClick
import com.example.pasyarapp.classes.Place
import com.example.pasyarapp.classes.SpinnerAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.File

class AddPlaceActivity : AppCompatActivity(), OnItemClick {

    lateinit var categoryList: ArrayList<Category>
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var dbCategory: DatabaseReference
    lateinit var dbPlace: DatabaseReference
    lateinit var rvCategory: RecyclerView

    lateinit var etName: TextInputEditText
    lateinit var etLocation: TextInputEditText
    lateinit var etDescription: TextInputEditText
    lateinit var etCategory: TextInputEditText

    lateinit var btnPositive: MaterialButton
    lateinit var btnNegative: MaterialButton

    lateinit var currentCategory: Category

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_place)

        firebaseDatabase = Firebase.database
        rvCategory = findViewById(R.id.rvCategory)
        dbCategory = firebaseDatabase.getReference("category")
        dbPlace = firebaseDatabase.getReference("places")

        btnNegative = findViewById(R.id.btnNegative)
        btnPositive = findViewById(R.id.btnPositive)

        etName = findViewById(R.id.etName)
        etLocation = findViewById(R.id.etLocation)
        etDescription = findViewById(R.id.etDescription)
        etCategory = findViewById(R.id.etCategory)

        btnPositive.text = "Add"
        btnNegative.text = "Cancel"

        btnNegative.setOnClickListener(View.OnClickListener {
            finish()
        })
        btnPositive.setOnClickListener(View.OnClickListener {
            addPlace()
        })
        categoryList = arrayListOf()

        fetchCategories()
    }

    private fun addPlace() {
        val name = etName.text.toString()
        val location = etLocation.text.toString()
        val category = etCategory.text.toString()
        val description = etDescription.text.toString()

        if (name.isEmpty() && location.isEmpty() && category.isEmpty() && description.isEmpty()) {
            Toast.makeText(applicationContext, "Enter all fields", Toast.LENGTH_SHORT).show()
            return
        }
        val key = dbPlace.push().key

        val place = Place(key, location, description, name, currentCategory)
        dbPlace.child(key.toString()).setValue(place).addOnCompleteListener {
            Toast.makeText(applicationContext, "OK", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener {
            Toast.makeText(applicationContext, "${it.message.toString()}", Toast.LENGTH_SHORT)
                .show()

        }
    }

    private fun fetchCategories() {
        categoryList.clear()

        dbCategory.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (dataSnap in snapshot.children) {
                    val category = dataSnap.getValue(Category::class.java)
                    categoryList.add(category!!)
                }

                rvCategory.setHasFixedSize(true)
                rvCategory.layoutManager = LinearLayoutManager(
                    this@AddPlaceActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                rvCategory.adapter =
                    CategoryAdapter(categoryList, this@AddPlaceActivity, this@AddPlaceActivity)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "${error.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onCategoryClick(category: Category) {
        currentCategory = category
        etCategory.setText(category.name)
    }

    private fun getFileExtension(context: Context, uri: Uri): String {

        var extension = ""
        if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            val mimeType = MimeTypeMap.getSingleton()
            extension =
                mimeType.getExtensionFromMimeType(context.contentResolver.getType(uri)).toString()
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
        }

        return extension
    }
}