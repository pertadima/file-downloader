package id.co.filedownloader

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isGone
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {
    private var notificationManager: NotificationManager? = null
    private lateinit var mNotification: Notification

    private val PERMISSIONS = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(this@MainActivity, PERMISSIONS, 112)
        notificationManager =
            getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
        progress_circular.isGone = true

        btn_download.setOnClickListener {
            btn_download.isGone = true
            progress_circular.isGone = false
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
                        btn_download.isGone = !(it ?: false)
                        progress_circular.isGone = it ?: true
                    }, {
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    })
            )
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}
