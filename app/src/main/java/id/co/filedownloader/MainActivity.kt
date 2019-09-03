package id.co.filedownloader

import android.app.DownloadManager
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.view.isGone
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {
    private var id = 0
    private var mNotifyManager: NotificationManager? = null
    private var mBuilder: NotificationCompat.Builder? = null

    private val PERMISSIONS = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(this@MainActivity, PERMISSIONS, 112)

        progress_circular.isGone = true
        btn_download.setOnClickListener {
            download()
        }
    }

    private fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            permissions.forEach {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        it
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    private fun download() {
        if (!hasPermissions(this@MainActivity, PERMISSIONS)) {
            val t = Toast.makeText(
                applicationContext,
                "You don't have write access !",
                Toast.LENGTH_LONG
            )
            t.show()

        } else {
            downloadUsingInputStream()
            //downloadUsingDownloadManager()
        }
    }

    private fun downloadUsingInputStream() {
        btn_download.isGone = true
        progress_circular.isGone = false

        val fileName = "test-download.pdf"
        val folder =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        compositeDisposable.add(
            Flowable.fromCallable {
                FileDownloader.downloadFile(
                    "http://maven.apache.org/maven-1.x/maven.pdf",
                    File(folder, fileName)
                ) {
                    progress_circular.progress = it
                }
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    showNotification()
                    Toast.makeText(this, "Download Selesai", Toast.LENGTH_LONG).show()
                    btn_download.isGone = !(it ?: false)
                    progress_circular.isGone = it ?: true
                }, {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                })
        )
    }

    private fun downloadUsingDownloadManager() {
        val request = DownloadManager.Request(Uri.parse("http://maven.apache.org/maven-1.x/maven.pdf")).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            setTitle("Download")
            setDescription("File is Downloading")
            allowScanningByMediaScanner()
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "test")
        }

        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }

    private fun showNotification() {
        mNotifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mBuilder = NotificationCompat.Builder(this)
        mBuilder?.setContentTitle("Downloading File")
            ?.setContentText("Test test")
            ?.setProgress(0, 100, false)
            ?.setOngoing(true)
            ?.setSmallIcon(R.mipmap.ic_launcher)
            ?.priority = Notification.PRIORITY_LOW

        mNotifyManager?.notify(id, mBuilder?.build())
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}
