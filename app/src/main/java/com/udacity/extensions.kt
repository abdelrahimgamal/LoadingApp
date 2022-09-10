package com.udacity

import android.animation.AnimatorSet
import android.app.DownloadManager
import android.app.NotificationManager
import android.content.Context
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat

fun Context.getNotificationManager(): NotificationManager = ContextCompat.getSystemService(
    this,
    NotificationManager::class.java
) as NotificationManager

fun Context.getDownloadManager(): DownloadManager = ContextCompat.getSystemService(
    this,
    DownloadManager::class.java
) as DownloadManager

