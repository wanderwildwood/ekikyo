package com.wanderwildwood.ekikyo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalCursorBlinkEnabled
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.mudita.mmd.ThemeMMD
import com.wanderwildwood.ekikyo.ui.IChingApp
import com.wanderwildwood.ekikyo.ui.houseTypography
import com.wanderwildwood.ekikyo.ui.monochrome
import com.wanderwildwood.ekikyo.ui.ArmedScope

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = Store(this)
        setContent {
            // A steady cursor: a blinking one is a repaint twice a second on an e-ink panel
            // for as long as a field has focus.
            CompositionLocalProvider(LocalCursorBlinkEnabled provides false) {
                ThemeMMD(colorScheme = monochrome, typography = houseTypography) {
                    ArmedScope {
                        // A debug build names its places for the emulator tests; a release names none.
                        Box(Modifier.semantics { testTagsAsResourceId = BuildConfig.DEBUG }) {
                            IChingApp(store)
                        }
                    }
                }
            }
        }
    }
}
