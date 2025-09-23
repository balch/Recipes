package org.balch.recipes.xml.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AbstractComposeView
import org.balch.recipes.ui.widgets.FoodLoadingIndicator

class FoodLoadingIndicatorView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : AbstractComposeView(context, attrs, defStyleAttr) {

    @Composable
    override fun Content() {
        FoodLoadingIndicator()
    }
}

