package com.example.myappgym

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var imageViewProfile: ImageView
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val nameEditText = findViewById<EditText>(R.id.editTextName)
        val lastNameEditText = findViewById<EditText>(R.id.editTextLastName)
        val ageEditText = findViewById<EditText>(R.id.editTextAge)
        val updateButton = findViewById<Button>(R.id.buttonUpdate)
        imageViewProfile = findViewById(R.id.imageViewProfile)

        imageViewProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        updateButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val lastName = lastNameEditText.text.toString()
            val age = ageEditText.text.toString().toInt()

            val user = auth.currentUser
            if (user != null) {
                val userId = user.uid
                val userProfile = hashMapOf(
                    "name" to name,
                    "lastName" to lastName,
                    "age" to age
                )

                db.collection("users").document(userId)
                    .set(userProfile)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data
            imageViewProfile.setImageURI(selectedImageUri)
            uploadImageToFirebase(selectedImageUri)
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri?) {
        if (imageUri != null) {
            val user = auth.currentUser
            if (user != null) {
                val userId = user.uid
                val storageRef = storage.reference.child("profileImages/$userId.jpg")
                storageRef.putFile(imageUri)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            db.collection("users").document(userId)
                                .update("profileImage", uri.toString())
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
