## Description

- Create annotation with an `@Preview` for each theme
- Define instances of `PreviewParameterProvider` to feed values in Composable arguments 
- Combine these techniques to create multiple Previews for each theme and argument combo

## Code Snippet

```
// Combine multiple `@Preview` into a new `@ThemePreview` annotation      
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

// Define `PreviewParameterProvider` with values for a `@Preview` argument      
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
        // ...
    )
}

// Combine `@ThemePreview` and `@PreviewParameter(CodeRecipeProvider::class)` to create multiple Previews      
@Preview
@ThemePreview
@Composable
fun CodeRecipePreview(
    @PreviewParameter(CodeRecipeProvider::class) codeRecipe: CodeRecipe
) {
    // ...
}
```