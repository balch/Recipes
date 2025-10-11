## Description

- In **Nav3**, you manage the ***backstack***.
- Create ***backstack*** using `rememberNavBackStack(navKey)`
  - ***backstack*** survives process death and configuration changes (aka savable)
  - All route parameters must be `@Serializable`
- Push/Pop/Peek works for simple applications

## Code Snippet

```
@Composable
private fun MainContent() {

    // remember backstack in a savable way
    val backStack = rememberNavBackStack(TOP_LEVEL_ROUTES[0])
    
    // ...
    
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.pop() },

        entryProvider = entryProvider {
            entry<Ideas> {
                IdeasScreen(
                    onCategoryClick = { category ->
                        backStack.push(
                            SearchRoute(SearchType.Category(category.name))
                        )
                    },
                    onAreaClick = { area ->
                        backStack.push(
                            SearchRoute(SearchType.Area(area.name))
                        )
                    },
                    onIngredientClick = { ingredient ->
                        backStack.push(
                            SearchRoute(SearchType.Ingredient(ingredient.name))
                        )
                    },
                    onCodeRecipeClick = { codeRecipe ->
                        backStack.push(
                            DetailRoute(DetailType.CodeRecipeContent(codeRecipe))
                        )
                    },
                    onScrollChange = { firstVisibleIndex = it }
                )
            }
            entry<SearchRoute> { searchRoute ->
            
            // ..
        }
    }
}

// NavBackStack push/pop/peek extensions

fun NavBackStack<NavKey>.isLastScreen() = size == 1

fun NavBackStack<NavKey>.push(destination: NavKey){
    backStackLogger.d { "push: $destination" }
    add(destination)
}

fun NavBackStack<NavKey>.pop(): NavKey? =
    removeLastOrNull()
        .also { backStackLogger.d { "pop: $it" } }

fun NavBackStack<NavKey>.peek(): NavKey? = lastOrNull()

```