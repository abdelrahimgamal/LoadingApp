package com.udacity

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*


class DetailActivity : AppCompatActivity() {
    var title: String = ""
    var status: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        val fileNameTxt = findViewById<TextView>(R.id.fileNameTxt)
        val fileStatusTxt = findViewById<TextView>(R.id.fileStatusTxt)
        val ok_button = findViewById<Button>(R.id.ok_button)
        intent?.let {
            title = it.extras?.getString("title").toString()
            status = it.extras?.getString("status").toString()

            Log.e("TAG", "onCreate: " + title + "   " + status)
        }
        fileNameTxt.text = title
        fileStatusTxt.text = status
        when (status) {
            DownloadStatus.FAILED.toString() -> fileStatusTxt.setTextColor(resources.getColor(R.color.red))
            DownloadStatus.SUCCESSFUL.toString() -> fileStatusTxt.setTextColor(resources.getColor(R.color.colorPrimaryDark))
        }

        ok_button.setOnClickListener {
            finish()
        }

    }

}
