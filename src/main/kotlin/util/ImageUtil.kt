package util

import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.net.URL.*

object ImageUtil {
    fun downloadImage(imageUrl: String, destinationFile: String) {
        try {
            val url = URI.create(imageUrl).toURL()
            val imageBytes = url.readBytes()
            val file = File(destinationFile)
            file.writeBytes(imageBytes)
            println("Image downloaded successfully")
        } catch (e: Exception) {
            println("Error downloading image: ${e.message}")
        }
    }
}