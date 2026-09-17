package com.clipsort.app.presentation.common

import android.content.Context
import android.content.Intent
import android.net.Uri

/** Handles older entries that stored the whole share caption instead of just the URL. */
fun sharedHttpUrl(text: String): String? {
    val candidate = Regex("https?://[^\\s<>]+", RegexOption.IGNORE_CASE).find(text)?.value
        ?.trimEnd('.', ',', ';', ')', ']', '}') ?: return null
    val uri = Uri.parse(candidate)
    if (uri.host.isNullOrBlank()) return null
    return uri.buildUpon().scheme(uri.scheme?.lowercase()).build().toString()
}

fun openClipLink(context: Context, text: String): Boolean {
    val url = sharedHttpUrl(text) ?: return false
    return runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }.isSuccess
}

fun shareClipLink(context: Context, text: String, chooserTitle: String): Boolean {
    val url = sharedHttpUrl(text) ?: return false
    return runCatching {
        val share = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, url) }
        context.startActivity(Intent.createChooser(share, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }.isSuccess
}
