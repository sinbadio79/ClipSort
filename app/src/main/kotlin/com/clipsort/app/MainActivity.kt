package com.clipsort.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.clipsort.app.presentation.navigation.ClipSortNavHost
import com.clipsort.app.presentation.theme.ClipSortTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClipSortTheme {
                ClipSortNavHost()
            }
        }
    }
}
