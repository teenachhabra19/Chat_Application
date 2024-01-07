package com.example.whatsappclone

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

class SignUpActivity : AppCompatActivity() {
    private val storage by lazy {
        FirebaseStorage.getInstance()
    }
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }
    private val database by lazy {
        FirebaseFirestore.getInstance()
    }
    private lateinit var downloadUrl: String
    private lateinit var userImgView: ShapeableImageView
    private lateinit var nextBtn: MaterialButton
    private lateinit var nameEt: EditText

    private val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1001
    private val WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1002
    private val IMAGE_PICK_REQUEST_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        userImgView = findViewById(R.id.userImgView)
        nextBtn = findViewById(R.id.nextBtn)
        nameEt = findViewById(R.id.nameEt)

        userImgView.setOnClickListener {
            checkPermissionForImage()
        }

        nextBtn.setOnClickListener {
            nextBtn.isEnabled = false
            val name = nameEt.text.toString()
            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            } else if (!::downloadUrl.isInitialized) {
                Toast.makeText(this, "Image cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                val user = User(name, downloadUrl, downloadUrl, auth.uid!!)
                database.collection("users").document(auth.uid!!).set(user)
                    .addOnSuccessListener {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }.addOnFailureListener {
                        nextBtn.isEnabled = true
                    }
            }
        }
    }

    private fun checkPermissionForImage() {
        if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) &&
            (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        ) {
            val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            val permissionWrite = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            requestPermissions(permission, READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
            requestPermissions(permissionWrite, WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
        } else {
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_REQUEST_CODE) {
            data?.data?.let {
                userImgView.setImageURI(it)
                uploadImage(it)
            }
        }
    }

    private fun uploadImage(uri: Uri) {
        nextBtn.isEnabled = false
        val ref = storage.reference.child("uploads/${auth.uid}")
        val uploadTask = ref.putFile(uri)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            nextBtn.isEnabled = true
            if (task.isSuccessful) {
                downloadUrl = task.result.toString()
                Log.i("URL", "downloadURL: $downloadUrl")
            } else {
                // Handle the failure
            }
        }.addOnFailureListener {
            // Handle the failure
        }
    }
}
