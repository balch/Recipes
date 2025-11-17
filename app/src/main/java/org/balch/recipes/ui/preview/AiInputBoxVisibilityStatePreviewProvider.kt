package org.balch.recipes.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import org.balch.recipes.ui.widgets.AiInputBoxVisibilityState

class AiInputBoxVisibilityStatePreviewProvider: PreviewParameterProvider<AiInputBoxVisibilityState> {
    override val values: Sequence<AiInputBoxVisibilityState>
        get() = sequenceOf(
            AiInputBoxVisibilityState.Gone,
            AiInputBoxVisibilityState.FloatingActionBox,
            AiInputBoxVisibilityState.Error("Whoops, not sure what happened."),
            AiInputBoxVisibilityState.Collapsed("Tell me the recipe you seek"),
            AiInputBoxVisibilityState.Collapsed("Loading that recipe now", true),
            AiInputBoxVisibilityState.Expanded("Tell me the recipe you seek"),
            AiInputBoxVisibilityState.Expanded("Loading that recipe now", true),
        )
}