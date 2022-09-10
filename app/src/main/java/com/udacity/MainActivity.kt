package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.udacity.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager

        binding.content.customButton.setOnClickListener {
            if (binding.content.downloadOptionRadioGroup.checkedRadioButtonId == View.NO_ID)
                Toast.makeText(this, "Please Select What to download", Toast.LENGTH_SHORT).show()
            else
                download()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            id?.let {
                createDownloadNotificationChannel(this@MainActivity)
                val radioButton:RadioButton=findViewById(binding.content.downloadOptionRadioGroup.checkedRadioButtonId)
                Log.e("TAG", "onReceive: ${radioButton.text}" )
                val downloadStatus = getDownloadManager().queryStatus(it)
                notificationManager.sendNotification(radioButton.text.toString(), downloadStatus.toString(), this@MainActivity)

                when (downloadStatus) {
                    DownloadStatus.SUCCESSFUL -> binding.content.customButton.changeState(
                        ButtonState.Completed
                    )
                    DownloadStatus.UNKNOWN -> binding.content.customButton.changeState(ButtonState.Completed)
                    DownloadStatus.FAILED -> binding.content.customButton.changeState(ButtonState.Completed)

                }


            }
        }
    }

    private fun DownloadManager.queryStatus(id: Long): DownloadStatus {
        query(DownloadManager.Query().setFilterById(id)).use {
            with(it) {
                if (this != null && moveToFirst()) {
                    return when (getInt(getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.SUCCESSFUL
                        DownloadManager.STATUS_FAILED -> DownloadStatus.FAILED
                        else -> DownloadStatus.UNKNOWN
                    }
                }
                return DownloadStatus.UNKNOWN
            }
        }
    }


    private fun download() {
        binding.content.customButton.changeState(ButtonState.Loading)
        Log.e("TAG", "download: ")
        val request =
            DownloadManager.Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
    }

}
