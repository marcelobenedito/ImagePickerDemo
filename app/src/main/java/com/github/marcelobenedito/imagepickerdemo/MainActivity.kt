package com.github.marcelobenedito.imagepickerdemo

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date


class MainActivity : AppCompatActivity() {

    private var imageview: ImageView? = null
    private var take: Button? = null
    private var select: Button? = null
    private var mediaPath: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageview = findViewById(R.id.imageview)
        take = findViewById(R.id.take)
        select = findViewById(R.id.select)

        take!!.setOnClickListener { takePicture() }
        select!!.setOnClickListener { pickPhoto() }
    }

    // Function to check and request permission.
    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        } else {
            Toast.makeText(this@MainActivity, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun takePicture() {
        checkPermission(android.Manifest.permission.CAMERA, 0)
        checkPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE, 0)

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            mediaPath = createNewImageFile(this)
            val uri = FileProvider.getUriForFile(this, "$packageName.provider", mediaPath!!)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            startActivityForResult(takePictureIntent, 0)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    private fun pickPhoto() {
        val pickPhoto = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(pickPhoto, 1)
    }

    @Throws(IOException::class)
    fun createNewImageFile(context: Context): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            absolutePath
        }
    }

    fun saveBitmapToFile(bitmap: Bitmap?, mimeType: String, absolutePath: String?): File? {
        if(absolutePath == null || bitmap == null){
            return null
        }

        val file = File(absolutePath)
        val stream = FileOutputStream(file)

        if (mimeType.contains("jpg", true) || mimeType.contains("jpeg", true))
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        else if (mimeType.contains("png", true))
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

        stream.close()

        return file
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            0 -> if (resultCode == RESULT_OK) {
                var resultBitmap: Bitmap = BitmapFactory.decodeFile(mediaPath?.absolutePath)
                //Save file to upload on server
//                val file = saveBitmapToFile(resultBitmap, "image/jpg", mediaPath?.absolutePath)
//                val values = ContentValues()
//
//                values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis())
//                values.put(Images.Media.MIME_TYPE, "image/jpeg")
//                values.put(MediaStore.MediaColumns.DATA, "test")
//
//                getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values)

                MediaStore.Images.Media.insertImage(getContentResolver(), resultBitmap, "test" , "test description")

                imageview?.setImageBitmap(resultBitmap)
            }

            1 -> if (resultCode == RESULT_OK) {
                val selectedImage = data?.data
                imageview?.setImageURI(selectedImage)
            }
        }
    }
}