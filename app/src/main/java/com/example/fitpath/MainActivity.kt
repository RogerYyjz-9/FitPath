// File: app/src/main/java/com/example/fitpath/MainActivity.kt
package com.example.fitpath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitpath.domain.model.LanguageMode
import com.example.fitpath.ui.AppRoot
import com.example.fitpath.ui.vm.AppViewModel
import com.example.fitpath.ui.vm.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val app = (LocalContext.current.applicationContext as FitPathApplication)
            val vm: AppViewModel = viewModel(factory = AppViewModelFactory(app.container))

            val prefs by vm.prefs.collectAsState()

            LaunchedEffect(prefs.languageMode) {
                val tags = when (prefs.languageMode) {
                    LanguageMode.SYSTEM -> ""
                    LanguageMode.EN -> "en"
                    LanguageMode.ZH -> "zh"
                }
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tags))
            }

            AppRoot(vm = vm)
        }
    }
}
