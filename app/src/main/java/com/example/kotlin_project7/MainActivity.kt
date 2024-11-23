package com.example.kotlin_project7

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Network
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.net.URL
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    // потоки Network и Disk
    val networkDispatcher = newSingleThreadContext("Network")
    val diskDispatcher = newSingleThreadContext("Disk")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val download_button = findViewById<Button>(R.id.download_button)
        val image_link = findViewById<EditText>(R.id.image_link)

        // обработка нажатия
        download_button.setOnClickListener {
            val imageUrl = image_link.text.toString()
            if (imageUrl.isNotBlank()) {
                downloadAndSaveImage(imageUrl)
            } else {
                image_link.error = "Введите ссылку"
            }
        }
    }

    // загрузить и сохраненить изображение
    private fun downloadAndSaveImage(imageUrl: String) {
        CoroutineScope(Dispatchers.Main).launch {
            // загрузить изображение в потоке Network
            val bitmap = withContext(networkDispatcher) {
                downloadImage(imageUrl)
            }

            // сохранить изображение на устройство в потоке Disk
            withContext(diskDispatcher) {
                if (bitmap != null) {
                    saveImage(bitmap)
                }
            }
        }
    }

    // загрузить изображение
    private fun downloadImage(url: String): Bitmap? {
        val inputStream = URL(url).openStream()
        return BitmapFactory.decodeStream(inputStream)
    }

    // сохранить изоражение
    private fun saveImage(bitmap: Bitmap) {
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val file = File(documentsDir, "downloaded_image.png")
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
        runOnUiThread {
            Toast.makeText(this, "Изображение сохранено: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    }
}
