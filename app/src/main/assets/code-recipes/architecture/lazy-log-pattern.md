## Description

- Efficient logging is necessary for stable app performance and low memory consumption 
- Declare logger with class name or other distinct identifier
- Create logging strings in _lambda_ functions   
- _Lambda_ functions don't get called when log level is not enabled
- Use `logger.v { }` for targeted debugging 
- Make sure to disable logging in ***release*** version
   - Or default to off for configurable ***release*** scenarios
- [KmLogging](https://github.com/LighthouseGames/KmLogging) is an excellent multiplatform logging library

## Code Snippet

```
@Singleton
class CodeRecipes @Inject constructor() {

    // declare logger with class name
    private val logger = logging(this::class.simpleName)
    suspend fun getRandomRecipes(count: Int): List<CodeRecipe> {
        if (count <= 0) {
            logger.w { "Empty list returned for count: $count" }
            return emptyList()
        }

        if (randomRecipes.size < count) {
            randomRecipes.addAll(rawRecipes().shuffled())
            logger.d { "Reshuffled Code Recipes Pool" }
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
                    logger.d { "Edge Case Alert!!! - $nextIem already in use" }
                    randomRecipes.add(nextIem)
                }
            }
        }
        return result.toList()
            .also { list -> logger.v { "getRandomRecipes: ${list.map { it.title }}" } }
    }
}
```