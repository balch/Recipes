package org.balch.recipes.features

import org.balch.recipes.core.models.CodeArea
import org.balch.recipes.core.models.CodeRecipe
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CodeRecipes @Inject constructor() {
    private val recipes = listOf(
        CodeRecipe(
            area = CodeArea.Theme,
            title = "colorScheme",
            description = "Use `isSystemInDarkTheme` and `dynamicColor` to control color scheme",
            fileName = "RecipesTheme.kt",
            codeSnippet =
"""
val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build
    .VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
}
"""
        ),
        CodeRecipe(
            area = CodeArea.Theme,
            title = "colorScheme",
            fileName = "ThemePreview.kt",
            description = "Create annotation with sn `@Preview` for each theme",
            codeSnippet =
                """
val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build
    .VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
}
"""
        ),
        CodeRecipe(
            area = CodeArea.Navigation,
            title = "Bottom Navigation",
            description = "Wrap `NavigationBar` in `Scaffold` and `AnimatedVisibility` to position and display the `NavigationBarItem`",
            fileName = "MainActivity.kt",
            codeSnippet =
                """
val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build
    .VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
}
"""
        ),
        CodeRecipe(
            area = CodeArea.Navigation,
            title = "entryDecorators",
            description = "Define `entryDecorators` to provide state management and to facilitate ViewModel creation.",
            fileName = "MainActivity.kt",
            codeSnippet =
                """
val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build
    .VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
}
"""
        ),
        CodeRecipe(
            area = CodeArea.Navigation,
            title = "entryProvider DSL syntax",
            description = "For simple apps, the `entryProvider` DSL syntax provides a convenient way to create ViewModels and push screens on the Backstack.",
            fileName = "MainActivity.kt",
            codeSnippet =
                """
val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build
    .VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
}
"""
        ),
        CodeRecipe(
            area = CodeArea.Navigation,
            title = "backstack",
            description = "You own the backstack. Simple push/pop works for simple applications",
            fileName = "BackstackManager.kt",
            codeSnippet =
                """
val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build
    .VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
}
"""
        ),
        CodeRecipe(
            area = CodeArea.Architecture,
            title = "ViewModel creation",
            description = "Use `HiltViewModel` and `assistedFactory` to creat unique ViewModel per screen to push on the backstack.",
            fileName = "SearchViewModel.kt",
            codeSnippet =
                """
val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build
    .VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
}
"""
        ),
    )

    fun getRandomRecipes(count: Int): List<CodeRecipe> =
        recipes.shuffled().take(count)
}