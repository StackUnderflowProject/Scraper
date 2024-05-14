package util

import java.io.File
import java.net.URI

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