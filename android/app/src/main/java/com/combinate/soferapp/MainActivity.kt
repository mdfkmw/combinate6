package com.combinate.soferapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.combinate.soferapp.ui.SoferApp
import com.combinate.soferapp.ui.SoferAppViewModel
import com.combinate.soferapp.ui.theme.SoferTheme

class MainActivity : ComponentActivity() {
    private val viewModel: SoferAppViewModel by viewModels {
        val app = application as SoferApplication
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SoferAppViewModel(app.container.repository, app.container.syncEngine) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SoferTheme {
                SoferApp(viewModel = viewModel)
            }
        }
    }
}
