package com.clipsort.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.key
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.UUID
import com.clipsort.app.presentation.share.CategorizeSheet
import com.clipsort.app.presentation.theme.ClipSortTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Seule responsabilité : recevoir l'intent ACTION_SEND du système et afficher
 * la bottom sheet de catégorisation par-dessus l'app source, sans navigation complexe.
 */
@AndroidEntryPoint
class ShareReceiverActivity : ComponentActivity() {
    private var sharedText by mutableStateOf("")
    private var shareSession by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shareSession = savedInstanceState?.getString("shareSession") ?: UUID.randomUUID().toString()
        sharedText = intent.takeIf { it.action == Intent.ACTION_SEND }
            ?.getStringExtra(Intent.EXTRA_TEXT)
            .orEmpty()

        setContent {
            ClipSortTheme {
                key(shareSession) {
                    CategorizeSheet(
                        sharedText = sharedText,
                        onDismiss = { finish() },
                        viewModel = hiltViewModel(key = shareSession)
                    )
                }
            }
        }
    }

    // singleTask delivers subsequent shares here. A new session cannot inherit
    // the previous note, category or a completed save that would close the sheet.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        shareSession = UUID.randomUUID().toString()
        sharedText = intent.takeIf { it.action == Intent.ACTION_SEND }
            ?.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("shareSession", shareSession)
        super.onSaveInstanceState(outState)
    }
}
