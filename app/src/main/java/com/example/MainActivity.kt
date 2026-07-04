package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.ui.ExamPrepMainUi
import com.example.ui.ExamPrepViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.utils.VoiceHelper

class MainActivity : ComponentActivity() {
    
    private val viewModel: ExamPrepViewModel by viewModels()
    private lateinit var voiceHelper: VoiceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize voice synthesizer and listener
        voiceHelper = VoiceHelper(
            context = this,
            onSpeechResults = { text ->
                viewModel.askDynamicGuruJi(text)
            },
            onSpeechStateChanged = { state ->
                viewModel.setSpeechState(state)
            }
        )

        // Wire voice helper to our VM
        viewModel.initVoiceHelper(voiceHelper)

        setContent {
            MyApplicationTheme {
                ExamPrepMainUi(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
    }
}
