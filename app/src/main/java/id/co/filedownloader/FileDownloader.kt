package id.co.filedownloader

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

/**
 * Created by pertadima on 02,September,2019
 */

object FileDownloader {
    private const val MEGABYTE = 1024 * 1024

    fun downloadFile(
        fileUrl: String,
        directory: File,
        progressBar: (Int) -> Unit
    ): Boolean? {
        try {
            val url = URL(fileUrl)
            val urlConnection = (url.openConnection() as HttpURLConnection).apply {
                connect()
            }

            val inputStream = urlConnection.inputStream
            val fileOutputStream = FileOutputStream(directory)
            val buffer = ByteArray(MEGABYTE)
            var bufferLength = 0
            val lengthOfFile = urlConnection.contentLength
            var total: Long = 0

            while ({ bufferLength = inputStream.read(buffer); bufferLength }() > 0) {
                total += bufferLength

                fileOutputStream.write(buffer, 0, bufferLength)
                progressBar.invoke(((total * 100) / lengthOfFile).toInt())
            }
            fileOutputStream.close()
            return true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}