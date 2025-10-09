## Description

- Use **Ktor** for Networking
- Create injectable `HttpClientFactory` to configure HTTP connections
- `ApiService` provides call patterns for HTTP requests
- Use `ApiService` to create specific services to remote apis
- Wrap the Specific ApiSerivce in  a Repository pattern to map raw API responses to domain objects

## Code Snippet

```
@Singleton
class ApiService @Inject constructor(
    private val httpClientFactory: HttpClientFactory,
    val dispatcherProvider: DispatcherProvider,
) {
    val client: HttpClient by lazy { httpClientFactory.create() }

    suspend inline fun <reified T> get(
        url: String,
        parameters: Map<String, String> = emptyMap()
    ): Result<T> {
        // Makes GET request and deserializes JSON response to type T
        // Wraps in Result for error handling
    }
    
    fun close() {
        client.close()
    }
}

@Singleton
class TheMealDbApi @Inject constructor(
    private val apiService: ApiService
) {
    companion object {
        private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1"        
        private const val CATEGORIES = "$BASE_URL/categories.php"
        private const val MEAL_BY_ID = "$BASE_URL/lookup.php"
    }
    
    suspend fun getCategories(): Result<CategoriesResponse> {
        return apiService.get(CATEGORIES)
    }
    
    suspend fun getMealById(id: String): Result<MealResponse> {
        return apiService.get(
            url = MEAL_BY_ID,
            parameters = mapOf("i" to id)
        )
    }
}

@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val api: TheMealDbApi
) : RecipeRepository {
    override suspend fun getCategories(): Result<List<Category>> {
        return api.getCategories().map { response ->
            response.categories
        }
    }
    override suspend fun getMealById(id: String): Result<Meal> {
        return api.getMealById(id).map { response ->
            response.meals.firstOrNull()
                ?: throw IllegalArgumentException("Meal not found")
        }
    }
}
```