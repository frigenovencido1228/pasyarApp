package com.example.pasyarapp

import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pasyarapp.classes.Category
import com.example.pasyarapp.classes.CategoryAdapter
import com.example.pasyarapp.classes.Commons
import com.example.pasyarapp.classes.OnItemClick
import com.example.pasyarapp.classes.Place
import com.example.pasyarapp.classes.SpinnerAdapter
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
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
import java.util.Timer
import kotlin.concurrent.schedule

class AddPlaceActivity : AppCompatActivity(), OnItemClick {

    lateinit var categoryList: ArrayList<Category>
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var dbCategory: DatabaseReference
    lateinit var dbPlace: DatabaseReference
    lateinit var rvCategory: RecyclerView
    lateinit var loadingDialog: Dialog
    lateinit var ivImageView: ImageView
    lateinit var ivBack: ImageView
    lateinit var etName: TextInputEditText
    lateinit var etLocation: TextInputEditText
    lateinit var etDescription: TextInputEditText
    lateinit var etCategory: TextInputEditText

    lateinit var btnPositive: MaterialButton
    lateinit var btnNegative: MaterialButton
    lateinit var btnChoose: MaterialButton
    lateinit var storageRef: StorageReference

    lateinit var currentCategory: Category

    var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_place)

        firebaseDatabase = Firebase.database
        rvCategory = findViewById(R.id.rvCategory)
        dbCategory = firebaseDatabase.getReference("category")
        dbPlace = firebaseDatabase.getReference("places")

        storageRef = Firebase.storage.reference

        loadingDialog = Commons.loadingDialog(this)

        ivImageView = findViewById(R.id.ivImage)
        ivBack = findViewById(R.id.ivBack)
        btnNegative = findViewById(R.id.btnNegative)
        btnPositive = findViewById(R.id.btnPositive)
        btnChoose = findViewById(R.id.btnChoose)

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

        btnChoose.setOnClickListener(View.OnClickListener {
            choosePhoto()
        })

        ivBack.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        })
        categoryList = arrayListOf()

        fetchCategories()
    }

    private fun choosePhoto() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"

        imagePickerActivityResult.launch(galleryIntent)
    }

    private var imagePickerActivityResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result != null) {
                // getting URI of selected Image
                imageUri = result.data?.data

                Glide.with(applicationContext).load(imageUri).into(ivImageView)
            }
        }

    private fun addPlace() {
        val name = etName.text.toString()
        val location = etLocation.text.toString()
        val category = etCategory.text.toString()
        val description = etDescription.text.toString()

        if (imageUri == null) {
            Toast.makeText(applicationContext, "Choose image.", Toast.LENGTH_SHORT).show()
            return
        }
        if (name.isEmpty() && location.isEmpty() && category.isEmpty() && description.isEmpty()) {
            Toast.makeText(applicationContext, "Enter all fields", Toast.LENGTH_SHORT).show()
            return
        }

        loadingDialog.show()
        val key = dbPlace.push().key

        val time = System.currentTimeMillis().toString()
        val fileExtension = getFileExtension(applicationContext, imageUri!!)

        val fileName = "$time.$fileExtension"

        val uploadTask = storageRef.child("file/$fileName").putFile(imageUri!!)

        uploadTask.addOnCompleteListener(OnCompleteListener {
            storageRef.child("upload/$fileName").downloadUrl.addOnCompleteListener(
                OnCompleteListener {
                    val place = Place(key, location, description, name, currentCategory, fileName)
                    dbPlace.child(key.toString()).setValue(place).addOnCompleteListener {
                        Timer().schedule(2000) {
                        }
                        loadingDialog.dismiss()

                        Toast.makeText(applicationContext, "OK", Toast.LENGTH_SHORT).show()


                    }.addOnFailureListener {
                        Toast.makeText(
                            applicationContext,
                            "${it.message.toString()}",
                            Toast.LENGTH_SHORT
                        )
                            .show()

                    }
                })
        })


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