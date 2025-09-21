# Recipes Project Development Guidelines


### Build Commands
```bash
# Clean and build the project
./gradlew clean build

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install debug build on connected device
./gradlew installDebug
```

### Dependency Management
This project uses **Gradle Version Catalogs** (`gradle/libs.versions.toml`) for centralized dependency management. All dependencies are declared in the version catalog and referenced using `libs.` notation in build files.

## Additional Development Information

### Code Architecture
- **UI Framework**: Jetpack Compose with Material 3
- **Activity**: Single Activity architecture using ComponentActivity
- **Theme System**: Custom theme implementation in `ui.theme` package
- **Edge-to-Edge**: Enabled by default for modern Android UI experience

## App Overview

The Recipes app is a modern Android application that helps users discover and explore recipes from different cuisines around the world. The app fetches recipe data from [TheMealDB API](https://www.themealdb.com/api.php) and presents it through an intuitive interface with three main sections:

### Core Features
- **Ideas Tab**: Displays recipe categories in a grid layout with different sized cells representing various nationalities (American, Italian, etc.)
- **Search Tab**: Shows a searchable list of recipes with detailed filtering capabilities
- **Recipe Details**: Full recipe information including ingredients, instructions, and metadata
- **Info Tab**: Additional app information and resources

### Technical Architecture
- **Architecture Pattern**: MVVM (Model-View-ViewModel) with UDF (Unidirectional Data Flow)
- **State Management**: State hoisting from ViewModels to Composable screens
- **Data Layer**: Repository pattern for data abstraction
- **Network Client**: Ktor for HTTP requests to TheMealDB API
- **UI Framework**: Jetpack Compose with Material 3 design system

### Project Structure
```
app/
├── src/
│   ├── main/java/org/balch/recipes/
│   │   ├── MainActivity.kt                    # Main activity with bottom navigation
│   │   ├── RecipesApplication.kt             # Application class
│   │   ├── features/                         # Feature-based organization
│   │   │   ├── details/
│   │   │   │   └── DetailScreen.kt          # Recipe detail screen
│   │   │   ├── ideas/
│   │   │   │   └── IdeasScreen.kt           # Categories grid screen
│   │   │   ├── info/
│   │   │   │   └── InfoScreen.kt            # Information screen
│   │   │   └── search/
│   │   │       └── SearchScreen.kt          # Recipe search screen
│   │   ├── ui/
│   │   │   ├── theme/                       # Material 3 theme configuration
│   │   │   │   ├── Color.kt
│   │   │   │   ├── Theme.kt
│   │   │   │   └── Type.kt
│   │   │   ├── utils/                       # UI utilities
│   │   │   │   ├── EdgeToEdgeCompat.kt     # Edge-to-edge configuration
│   │   │   │   └── TopLevelBackStack.kt    # Navigation back stack management
│   │   │   └── widgets/                     # Reusable UI components
│   │   │       └── WebViewScreen.kt        # Web view component
│   │   └── res/                             # Android resources
│   ├── test/java/                           # Unit tests
│   └── androidTest/java/                    # Instrumented tests
├── build.gradle.kts                         # App-level build configuration
└── proguard-rules.pro                      # ProGuard configuration
```

### Key Dependencies
- **Compose BOM**: Manages all Compose library versions
- **Activity Compose**: Integration between Activity and Compose
- **Material 3**: Modern Material Design components
- **Core KTX**: Kotlin extensions for Android APIs

### Development Best Practices
- Use Compose Previews (`@Preview`) for UI development
- Leverage the version catalog for dependency management
- Follow the existing package structure (`org.balch.recipes`)
- Use the provided theme system for consistent styling
- Test both UI components (with Compose testing) and business logic (with unit tests)

