package com.begateway.mobilepayments.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.webkit.WebView
import com.begateway.mobilepayments.models.network.request.BrowserInfo
import java.util.*

private const val ONE_MINUTE_IN_MILLISECONDS = 60_000
private const val BROWSER_ACCEPT_HEADER = "application/json, text/plain, */*"

internal fun Context.findDefaultLocalActivityForIntent(intent: Intent): ActivityInfo? {
    val applicationContext = this.applicationContext
    val applicationPackage = applicationContext.packageName
    val flags = PackageManager.GET_META_DATA
    return applicationContext.packageManager.queryIntentActivities(intent, flags)
        .asSequence()
        .map { it.activityInfo }
        .find { applicationPackage == it.packageName }
}

internal fun Context.getBrowserInfo(): BrowserInfo {
    val displayMetrics = this.resources.displayMetrics
    val webView = WebView(this)
    val userAgent = webView.settings.userAgentString
    val timeZone = TimeZone.getDefault()
    return BrowserInfo(
        acceptHeader = BROWSER_ACCEPT_HEADER,
        screenWidth = displayMetrics.widthPixels,
        screenHeight = displayMetrics.heightPixels,
        screenColorDepth = 24,
        windowWidth = displayMetrics.widthPixels,
        windowHeight = displayMetrics.heightPixels,
        language = Locale.getDefault().language,
        javaEnabled = true,
        userAgent = userAgent,
        timeZone = timeZone.rawOffset / ONE_MINUTE_IN_MILLISECONDS,
        timeZoneName = timeZone.id,
    )
}