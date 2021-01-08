package com.begateway.mobilepayments.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager

internal fun Context.findDefaultLocalActivityForIntent(intent: Intent): ActivityInfo? {
    val applicationContext = this.applicationContext
    val applicationPackage = applicationContext.packageName
    val flags = PackageManager.GET_META_DATA
    return applicationContext.packageManager.queryIntentActivities(intent, flags)
        .asSequence()
        .map { it.activityInfo }
        .find { applicationPackage == it.packageName }
}