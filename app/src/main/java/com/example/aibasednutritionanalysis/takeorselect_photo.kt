package com.example.aibasednutritionanalysis

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class takeorselect_photo : AppCompatActivity() {
    private val OPEN_GALLERY = 1
    private val REQUEST_IMAGE_CAPTURE: Int = 100
    private var currentPhotoPath: String = ""
    private var return_label = 0
    companion object{
        var bitmap: Bitmap? =null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_takeorselect_photo)
        val photobox:ImageView=findViewById(R.id.img_picture)
        val select_photo: Button = findViewById(R.id.select_photo)
        val take_photo: Button = findViewById(R.id.take_photo)
        val end = findViewById<Button>(R.id.button3)

        select_photo.setOnClickListener { openGallery() }
        take_photo.setOnClickListener {
            startCapture()
        }

        photobox.setOnClickListener{
            Toast.makeText(this, "사진을 등록해주세요", Toast.LENGTH_LONG).show()
        }

        end.setOnClickListener {
            if(bitmap==null) {
                Toast.makeText(this, "사진을 등록해주세요", Toast.LENGTH_LONG).show()
            }
            else {
                end.setBackgroundColor(Color.parseColor("#66CCFF"))
                val nextIntent = Intent(this, result::class.java)
                nextIntent.putExtra("데이터베이스로 전달할 라벨 값",return_label)
                startActivity(nextIntent)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun openGallery() {
        val intent: Intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        startActivityForResult(intent, OPEN_GALLERY)
    }

    fun startCapture() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "com.example.aibasednutritionanalysis.fileprovider",
                            it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val img_picture = findViewById<ImageView>(R.id.img_picture)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val file = File(currentPhotoPath)
            if (Build.VERSION.SDK_INT < 28) {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(file))
                img_picture.setImageBitmap(bitmap)
            } else {
                val decode = ImageDecoder.createSource(this.contentResolver,
                        Uri.fromFile(file))
                bitmap = ImageDecoder.decodeBitmap(decode)
                img_picture.setImageBitmap(bitmap)
            }
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == OPEN_GALLERY) {
                var currentImageUrl: Uri? = data?.data
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver, currentImageUrl)
                    img_picture.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            Log.d("ActivityResult", "something wrong")
        }
    }

    //백버튼 누르면 메뉴화면으로 이동
    override fun onBackPressed() {
        super.onBackPressed()
        val nextIntent = Intent(this, menu::class.java)
        startActivity(nextIntent)
    }
}