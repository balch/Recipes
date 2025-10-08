## Description

- Encapsulate feature functionality into Modules
- Each Module contains separate classes for Biz Logic and UI
   - Use ViewModels for BizLogic
   - Use Compose Screens for UI
- Hoist State from ***ViewModel*** to UI to render the UX
- Send user action back to ***ViewModel*** to generate new state 
- **Separation of Concerns** facilitates **Single Responsibility** principle

## Code Snippet

**App File Structure**
```
app/src/main/java/org/balch/recipes/
├── MainActivity.kt                 # Entry point with navigation
├── RecipesApplication.kt           # App-level configuration
├── NavRoutes.kt                    # Route definitions
│
├── features/                       # 🎯 FEATURE MODULES
│   ├── ideas/                      # Ideas feature (Single Responsibility)
│   │   ├── IdeasScreen.kt          # ✨ UI Layer - Compose Screen
│   │   └── IdeasViewModel.kt       # 🧠 Business Logic Layer
│   ├── search/                     # Search feature (Single Responsibility)  
│   │   ├── SearchScreen.kt         # ✨ UI Layer - Compose Screen
│   │   └── SearchViewModel.kt      # 🧠 Business Logic Layer
│   └── details/                    # Details feature (Single Responsibility)
│       ├── DetailScreen.kt         # ✨ UI Layer - Compose Screen
│       └── DetailsViewModel.kt     # 🧠 Business Logic Layer
│
├── core/                           # 🔧 SHARED BUSINESS LOGIC
│   ├── models/                     # Data models used across features
│   │   ├── Meal.kt                 
│   │   ├── Category.kt             
│   │   └── CodeRecipe.kt           
│   ├── network/                    # API communication layer
│   │   ├── ApiService.kt           
│   │   ├── HttpClientFactory.kt           
│   │   └── TheMealDbApi.kt         
│   ├── repository/                 # Data access abstraction
│   │   ├── RecipeRepository.kt     
│   │   └── RepositoryModule.kt     
│   └── coroutines/                 # Async utilities
│       └── DispatcherProvider.kt   
│
└── ui/                             # 🎨 SHARED UI COMPONENTS
    ├── theme/                      # Material 3 theming
    │   ├── Theme.kt                
    │   ├── Color.kt                
    │   └── ThemePreview.kt         
    ├── widgets/                    # Reusable UI components
    │   ├── FoodLoadingIndicator.kt
    │   └── WebViewScreen.kt        
    └── nav/                        # Navigation utilities
        └── NavBackStackExt.kt     
        
🎯 Each FEATURE has dedicated ViewModel + Screen (Single Responsibility)
🔧 CORE contains shared business logic and data access
🎨 UI contains shared visual components and theming
```