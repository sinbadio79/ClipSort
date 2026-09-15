package com.clipsort.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.clipsort.app.presentation.share.CategorizeSheet
import com.clipsort.app.presentation.theme.ClipSortTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Seule responsabilité : recevoir l'intent ACTION_SEND du système et afficher
 * la bottom sheet de catégorisation par-dessus l'app source, sans navigation complexe.
 */
@AndroidEntryPoint
class ShareReceiverActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText = intent.takeIf { it.action == Intent.ACTION_SEND }
            ?.getStringExtra(Intent.EXTRA_TEXT)
            .orEmpty()

        setContent {
            ClipSortTheme {
                CategorizeSheet(
                    sharedText = sharedText,
                    onDismiss = { finish() }
                )
            }
        }
    }
}
