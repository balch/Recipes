package org.balch.recipes.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import org.balch.recipes.core.models.CodeArea
import org.balch.recipes.core.models.CodeRecipe

class CodeRecipeProvider : PreviewParameterProvider<CodeRecipe> {
    override val values = sequenceOf(
        CodeRecipe(
            index = 1,
            area = CodeArea.Architecture,
            title = "Architecture Title",
            description = "`Architecture` Description",
            codeSnippet = "print('Hello, Architecture!')"
        ),
        CodeRecipe(
            index = 2,
            area = CodeArea.Navigation,
            title = "Navigation Title",
            description = "`Navigation` Description",
            codeSnippet = "print('Hello, Navigation!')"
        ),
        CodeRecipe(
            index = 3,
            area = CodeArea.Theme,
            title = "Theme Title",
            description = "`Theme` Description",
            codeSnippet = "print('Hello, Theme!')"
        ),
        CodeRecipe(
            index = 4,
            area = CodeArea.Testing,
            title = "Testing Title",
            description = "`Testing` Description",
            codeSnippet = "print('Hello, Testing!')"
        )
    )
}
