package org.balch.recipes.ui.widgets.input

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import org.balch.recipes.RecipeRoute
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.theme.ThemePreview

/**
 * An item in a floating action button menu.
 */
data class FloatingActionMenuItem(
    val icon: ImageVector,
    val label: String,
    val action: () -> Unit
)

/**
 * A floating action button menu button with a list of menu items.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FloatingActionMenuButton(
    items: List<FloatingActionMenuItem>,
    opened: Boolean = false,
    onNavigateTo: (RecipeRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isMenuOpen by rememberSaveable { mutableStateOf(opened) }
    val focusRequester = FocusRequester()

    BackHandler(isMenuOpen) { isMenuOpen = false }

    // The main container for the screen (Scaffold is common)
    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = isMenuOpen,
        button = {
            ToggleFloatingActionButton(
                modifier =Modifier
                    .semantics {
                        traversalIndex = -1f
                        stateDescription = if (isMenuOpen) "Expanded" else "Collapsed"
                        contentDescription = "Toggle menu"
                    }
                    .animateFloatingActionButton(
                        visible = true,
                        alignment = Alignment.BottomEnd,
                    )
                    .focusRequester(focusRequester),
                checked = isMenuOpen,
                onCheckedChange = { isMenuOpen = !isMenuOpen },
            ) {
                val imageVector by remember {
                    derivedStateOf {
                        if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.AutoAwesome
                    }
                }
                Icon(
                    painter = rememberVectorPainter(imageVector),
                    contentDescription = "AI Options",
                    modifier = Modifier.animateIcon({ checkedProgress }),
                )
            }
        },
    ) {
        items.forEachIndexed { i, item ->
            FloatingActionButtonMenuItem(
                modifier = Modifier
                    .semantics {
                        isTraversalGroup = true
                        // Add a custom a11y action to allow closing the menu when focusing
                        // the last menu item, since the close button comes before the first
                        // menu item in the traversal order.
                        if (i == items.size - 1) {
                            customActions =
                                listOf(
                                    CustomAccessibilityAction(
                                        label = "Close menu",
                                        action = {
                                            isMenuOpen = false
                                            true
                                        },
                                    )
                                )
                        }
                    }
                    .then(
                        if (i == 0) {
                            Modifier.onKeyEvent {
                                // Navigating back from the first item should go back to the
                                // FAB menu button.
                                if (
                                    it.type == KeyEventType.KeyDown &&
                                    (it.key == Key.DirectionUp ||
                                            (it.isShiftPressed && it.key == Key.Tab))
                                ) {
                                    focusRequester.requestFocus()
                                    return@onKeyEvent true
                                }
                                return@onKeyEvent false
                            }
                        } else {
                            Modifier
                        }
                    ),
                onClick = {
                    isMenuOpen = false
                    item.action()
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                text = { Text(text = item.label) },
            )
        }
    }
}

@ThemePreview
@Composable
fun FloatingActionMenuButtonPreview(
    opened: Boolean = false,
) {
    val items = listOf(
        FloatingActionMenuItem(Icons.AutoMirrored.Filled.Chat, "Let's Chat") { },
        FloatingActionMenuItem(Icons.AutoMirrored.Filled.Send, "Context Inquiry") { },
    )

    RecipesTheme {
        FloatingActionMenuButton(
            opened = opened,
            items = items,
            onNavigateTo = { },
        )
    }
}

@ThemePreview
@Composable
fun FloatingActionMenuButtonOpenedPreview() =
    FloatingActionMenuButtonPreview(true)
