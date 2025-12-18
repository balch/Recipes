package org.balch.recipes.ui.widgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import kotlinx.coroutines.delay
import java.text.BreakIterator
import java.text.StringCharacterIterator


@Composable
fun TypewriterText(
    text: String,
    textId: Any,
    animate: Boolean,
    onAnimationComplete: () -> Unit = {},
    content: @Composable (String) -> Unit,
) {
    // Track the current character position in the source text
    var currentCharIndex by rememberSaveable(textId) {
        mutableStateOf(if (animate) 0 else text.length)
    }
    
    // Track if animation has completed for this text
    var animationCompleted by remember(textId) { mutableStateOf(!animate) }

    // The text we actually render
    val renderedText = if (currentCharIndex >= text.length) {
        text
    } else {
        text.take(currentCharIndex)
    }

    val breakIterator = remember { BreakIterator.getCharacterInstance() }

    LaunchedEffect(text, animate) {
        if (animate && !animationCompleted) {
            val targetText = text
            breakIterator.text = StringCharacterIterator(targetText)

            // Ensure index is within bounds of the potentially changed text
            val safeIndex = currentCharIndex.coerceAtMost(targetText.length)
            var nextIndex = if (safeIndex < targetText.length) {
                breakIterator.following(safeIndex)
            } else {
                BreakIterator.DONE
            }

            while (nextIndex != BreakIterator.DONE) {
                currentCharIndex = nextIndex
                // Wait for a frame to ensure composition/draw can happen
                withFrameMillis { }
                // Then add a small delay for the typewriter effect
                delay(30)
                nextIndex = breakIterator.next()
            }
            // Ensure we show the complete text at the end
            currentCharIndex = targetText.length
            animationCompleted = true
            onAnimationComplete()
        } else if (!animate) {
            currentCharIndex = text.length
        }
    }

    content(renderedText)
}
