package com.example.practico10

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.practico10.ui.theme.Practico10Theme
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Practico10Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ImageDownloader(context = this)
                }
            }
        }
    }
}

@Composable
fun ImageDownloader(context: ComponentActivity) {
    var imageUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1518791841217-8f162f1e1131?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=800&q=60") }
    var downloadedImage by remember { mutableStateOf<Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("Enter Image URL") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (imageUrl.isNotEmpty()) {
                    downloadAndSaveImage(context, imageUrl) { bitmap, error ->
                        downloadedImage = bitmap
                        errorMessage = error
                    }
                } else {
                    errorMessage = "Please enter a URL"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Download Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error
            )
        }

        LazyColumn {
            item {
                downloadedImage?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Downloaded Image",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun downloadAndSaveImage(context: ComponentActivity, imageUrl: String, callback: (Bitmap?, String?) -> Unit) {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            val bitmap = withContext(Dispatchers.IO) {
                // Загрузка изображения в фоновом потоке (Network)
                Log.d("NetworkThread", "Downloading image in thread: ${Thread.currentThread().name}")
                downloadImage(imageUrl)
            }

            withContext(Dispatchers.IO) {
                // Сохранение изображения во внутренней памяти (Disk)
                Log.d("DiskThread", "Saving image in thread: ${Thread.currentThread().name}")
                saveImageToInternalStorage(context, bitmap)
            }

            callback(bitmap, null)
        } catch (e: Exception) {
            callback(null, "Error: ${e.message}")
        }
    }
}

private fun downloadImage(imageUrl: String): Bitmap {
    return URL(imageUrl).openStream().use {
        BitmapFactory.decodeStream(it)
    }
}

private fun saveImageToInternalStorage(context: ComponentActivity, bitmap: Bitmap) {
    val file = File(context.filesDir, "downloaded_image.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
    }
    Log.d("ImageSavePath", "Image saved to: ${file.absolutePath}")
}