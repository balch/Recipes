## Description

- Sometimes you need to randomly show items from a list 
- Using a random index is easy, but leads to many repeated items
- Its better to show all the items in a random list to enure freshness  
    - Save shuffled main list to randomize order
    - Return and remove items from front of saved list 
    - Add more shuffled items when capacity runs low
    - Use a Set to ensure unique items

## Code Snippet

```
private val randomRecipes = mutableListOf<CodeRecipe>()

suspend fun getRandomRecipes(count: Int): List<CodeRecipe> {
    if (count <= 0) { return emptyList() }

    if (randomRecipes.size < count) {
        randomRecipes.addAll(rawRecipes().shuffled())
    }

    // use a Set to ensure unique entries when list roles over
    val result = mutableSetOf<CodeRecipe>()
    repeat(count) {
        var addedToResult = false
        while (!addedToResult) {
            val nextIem = randomRecipes.removeAt(0)
            addedToResult = result.add(nextIem)
            if (!addedToResult) {
                // nextItem is in use, so add it to the end of the list
                randomRecipes.add(nextIem)
            }
        }
    }
    return result.toList()
}
```