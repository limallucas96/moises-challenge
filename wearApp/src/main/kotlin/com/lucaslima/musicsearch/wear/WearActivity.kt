package com.lucaslima.musicsearch.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class WearActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WearApp() }
    }
}
