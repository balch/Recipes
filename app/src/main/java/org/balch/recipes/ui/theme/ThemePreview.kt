package org.balch.recipes.ui.theme

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    group = "Theme",
    name = "ThemeDark",
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    group = "Theme",
    name = "ThemeLight",
)
annotation class ThemePreview