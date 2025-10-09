## Description

- Simple Markdown Composable that renders beautiful code in Android (and other platforms)
- Support Light/Dark Theme and Code Markdown syntax
- Thank you [Mike Penz](https://github.com/mikepenz/multiplatform-markdown-renderer)!!

## Code Snippet

```
@Composable
fun MarkdownCodeSnippet(
    codeSnippet: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val highlightsBuilder = remember(isDarkTheme) {
        Highlights.Builder()
            .theme(SyntaxThemes.atom(darkMode = isDarkTheme))
            .language(SyntaxLanguage.KOTLIN)
    }
    Markdown(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        content = codeSnippet,
        components = markdownComponents(
            codeBlock = {
                MarkdownHighlightedCodeBlock(
                    content = it.content,
                    node = it.node,
                    highlightsBuilder = highlightsBuilder,
                    showHeader = true, 
                )
            },
            codeFence = {
                MarkdownHighlightedCodeFence(
                    content = it.content,
                    node = it.node,
                    highlightsBuilder = highlightsBuilder,
                    showHeader = true,
                )
            },
        )
    )
}
```