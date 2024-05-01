package com.example.pasyarapp

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.Gravity
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.pasyarapp.classes.Category
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    lateinit var database: FirebaseDatabase
    var imageUri: Uri? = null
    lateinit var dbCategory: DatabaseReference
    lateinit var etCategory: EditText
    lateinit var btnAddCategory: Button
    lateinit var categorySpinner: Spinner
    lateinit var categoryList: ArrayList<String>
    lateinit var btnAddPlace: Button
    lateinit var btnAddIcon: Button
    lateinit var ivIcon: ImageView
    lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = Firebase.database
        dbCategory = database.getReference("category")
        storageRef = Firebase.storage.reference

        etCategory = findViewById(R.id.etCategory)
        btnAddCategory = findViewById(R.id.btnAddCategory)
        btnAddPlace = findViewById(R.id.btnAddPlace)
        ivIcon = findViewById(R.id.ivIcon)
        btnAddIcon = findViewById(R.id.btnAddIcon)

        categorySpinner = findViewById(R.id.categorySpinner)

        categoryList = arrayListOf()

        fetchCategories()

        btnAddIcon.setOnClickListener(View.OnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK)
            galleryIntent.type = "image/*"

            imagePickerActivityResult.launch(galleryIntent)
        })

        btnAddCategory.setOnClickListener(View.OnClickListener {
            addCategory()
        })

        btnAddPlace.setOnClickListener(View.OnClickListener {
            val name = categorySpinner.selectedItem

            Toast.makeText(applicationContext, "Selected $name", Toast.LENGTH_SHORT).show()

        })
    }

    private var imagePickerActivityResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result != null) {
                // getting URI of selected Image
                imageUri = result.data?.data

                Glide.with(applicationContext).load(imageUri).into(ivIcon)
            }
        }

    private fun fetchCategories() {
        categoryList.clear()

        dbCategory.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (dataSnap in snapshot.children) {
                    val category = dataSnap.getValue(Category::class.java)
                    val name = category?.name
                    categoryList.add(name!!)
                }

                var arrayAdapter =
                    ArrayAdapter(
                        applicationContext,
                        android.R.layout.simple_list_item_1,
                        categoryList
                    )

                with(categorySpinner) {
                    adapter = arrayAdapter
                    setSelection(0, false)
                    onItemSelectedListener = this@MainActivity
                    prompt = "Select category"
                    gravity = Gravity.CENTER
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun addCategory() {
        val categoryName = etCategory.text.toString().trim()

        val key = dbCategory.push().key!!


        if (imageUri == null) {
            Toast.makeText(applicationContext, "Select image.", Toast.LENGTH_SHORT).show()
            return
        }
        val time = System.currentTimeMillis().toString()
        val fileExtension = getFileExtension(applicationContext, imageUri!!)

        val fileName = "$time.$fileExtension"

        val uploadTask = storageRef.child("file/$fileName").putFile(imageUri!!)

        uploadTask.addOnCompleteListener {
            storageRef.child("upload/$fileName").downloadUrl.addOnCompleteListener {

                val category = Category(key, categoryName, fileName)

                dbCategory.child(key).setValue(category).addOnCompleteListener {
                    Toast.makeText(applicationContext, "Added Category.", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(applicationContext, "Error: ${it.message.toString()}", Toast.LENGTH_SHORT).show()
                }
            }
        }

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

//    @SuppressLint("Range")
//    private fun getFileName(context: Context, uri: Uri): String? {
//        if (uri.scheme == "content") {
//            val cursor = context.contentResolver.query(uri, null, null, null, null)
//            cursor.use {
//                if (cursor != null) {
//                    if (cursor.moveToFirst()) {
//                        return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
//                    }
//                }
//            }
//        }
//        return uri.path?.lastIndexOf('/')?.let { uri.path?.substring(it) }
//    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//        Toast.makeText(applicationContext, "${categoryList[position]}", Toast.LENGTH_SHORT).show()

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}