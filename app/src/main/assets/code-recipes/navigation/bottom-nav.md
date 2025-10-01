## Description

- Wrap `NavigationBar` in `Scaffold` 
- Use `AnimatedVisibility` to control visibility of `NavigationBar`
- `TopLevelRoute` represent displayable items in `NavigationBarItem`
- Manage ***backstack*** in the `NavigationBar`
   - Pop the current ***Screen*** off the ***backstack*** if it not the root
   - Push the new route onto the ***backstack***

## Code Snippet

```
Scaffold(
    bottomBar = {
        AnimatedVisibility(
            visible = showNavigationBar && backstackManager.peek() is TopLevelRoute,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                TOP_LEVEL_ROUTES.forEach { topLevelRoute ->
                    val isSelected = topLevelRoute == backstackManager.peek()
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            // pop the current screen off the backstack if it not the root
                            if (backstackManager.peek() != TOP_LEVEL_ROUTES[0]) {
                                backstackManager.pop()
                            }
                            // push the new route onto the backstack
                            if (backstackManager.peek() != topLevelRoute) {
                                backstackManager.push(topLevelRoute)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = topLevelRoute.icon,
                                contentDescription = topLevelRoute.contentDescription
                            )
                        }
                    )
                }
            }
        }
    }
```