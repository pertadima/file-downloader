package id.co.filedownloader

import android.os.AsyncTask
import android.os.Environment
import java.io.File
import java.io.IOException


/**
 * Created by pertadima on 02,September,2019
 */

class DownloadFile(
    private val onError: (String) -> Unit
) : AsyncTask<String, Void, Void>() {
    override fun doInBackground(vararg strings: String): Void? {
        val fileUrl = strings[0]
        val fileName = strings[1]
        val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        val pdfFile = File(folder, fileName)
        try {
            pdfFile.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

//        FileDownloader.downloadFile(fileUrl, pdfFile, {
//
//        }, {
//            onError.invoke(it)
//        })

        return null
    }
}
