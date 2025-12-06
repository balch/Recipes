# Recipes Reference App

### ⚠️ COOKING IN PROGRESS
<sup>***🥖🥖This app is still baking under the hood🥖🥖</sup>***

### WHAT IS THIS?
Keeping up with the latest Android trends is always a challenge. One of the techniques I use is to always have a reference app handy. These apps should showcase the latest Android architecture and patterns for the following areas:
- UI and Biz Logic Separation
- Navigation UX and Theme
- Data and Image Retrieval
- Threading
- Dependency Injection
- Testing

The latest incarnation of my reference app is called ***Recipes***. 

Since I love a good double entendre, this app ***conflates*** _Meal_ and _Code_ **Recipes**, creating a UX to demonstrate the techniques it describes. 

One data source for this app is from the excellent (and free) [TheMealDB](https://www.themealdb.com/) which has nice amount of categorized data with textual and visual information describing a semi-complex process (aka, a ***Meal Recipe***). The other data source was my 🧠 and describes the code used to build the app in the form of ***Code Recipes*** (sprinkled in amongst the ***Meal Recipes***)

<sub>***Code Recipes: 👉👉 [assets/code-recipes](https://github.com/balch/Recipes/tree/main/app/src/main/assets/code-recipes) 👈👈***</sub>

The goal was to brush up on the the latest **Android** tech stack components and pack as many features as I could into an app created in ~3~ ??? weekends of watching sports on the couch. **Junie** and **Claude** (and **Gem3**) actually did a lot of work, and once again came through in frontend design and impl, dependency management, and kicking off new features. This time the data model was a huge AI win as I prompted **Junie** with the [TheMealDB API](https://www.themealdb.com/api.php) url, a reference to [Ktor](https://ktor.io/), and in less than 5 minutes the API layer materialized in my _git status_ staged log. 

Another reason this app came together so nicely is due to the plethora of free and easy to use **Android Tools and Libraries** supplied by both _Big-Biz_ (hello _GOOGLE_ and _JETBRAINS_), and the **Android Community ICs** toiling away on GitHub and Medium. The ***Code Recipes*** mostly originate from these sources and describe the wiring and plumbing used to create a visually compelling **Android Application**.

### 😋🌮 Bon App...😋🌮
|                                           Dark Mode                                           |                                          Light Mode                                           |
|:---------------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------:|
| <video src="https://github.com/user-attachments/assets/4723386b-970f-4c90-8d60-919193125c62"> | <video src="https://github.com/user-attachments/assets/82fb3350-ffc5-48bd-8030-16ff4972f4b3"> |

<details>
<summary><b>Meal Recipes</b></summary>

|                                      Categories                                      |                                       Cuisine                                       |                                        Detail List                                         |                                        Detail Step By Step                                         |
|:------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------------:|:--------------------------------------------------------------------------------------------------:|
|  <img src="screenshots/darkmode-category.png" width="200" alt="Dark Mode Category">  |  <img src="screenshots/darkmode-cuisine.png" width="200" alt="Dark Mode Cuisine">   |  <img src="screenshots/darkmode-detail-list.png" width="200" alt="Dark Mode Detail List">  |  <img src="screenshots/darkmode-detail-step.png" width="200" alt="Dark Mode Detail Step By Step">  |
| <img src="screenshots/lightmode-category.png" width="200" alt="Light Mode Category"> | <img src="screenshots/lightmode-cuisine.png" width="200" alt="Light Mode Category"> | <img src="screenshots/lightmode-detail-list.png" width="200" alt="Light Mode Detail List"> | <img src="screenshots/lightmode-detail-step.png" width="200" alt="Light Mode Detail Step By Step"> |

</details>

<details>
<summary><b>Code Recipes</b></summary>

|                                    Code Recipes                                    |                                        Code Recipe Detail                                         |
|:----------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------------------:|
| <img src="screenshots/darkmode-code.png" width="200" alt="Dark Mode Code Recipes"> |  <img src="screenshots/darkmode-code-detail.png" width="200" alt="Dark Mode Code Recipe Detail">  |
|  <img src="screenshots/lightmode-code.png" width="200" alt="Light Mode Recipes">   | <img src="screenshots/lightmode-code-detail.png" width="200" alt="Light Mode Code Recipe Detail"> |

</details>

<details>
<summary><b>Detail Screens</b></summary>
  
|                                           Dark Mode                                           |                                          Light Mode                                           | 
|:---------------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------:|
| <video src="https://github.com/user-attachments/assets/36a5b9b5-0a16-4ec8-b9d1-a13194ce5234"> | <video src="https://github.com/user-attachments/assets/4efd0269-0229-47d3-9796-2cfd27a65303"> |

</details>


### Koog AI Agent Integration
This app integrates [Koog AI Agents](https://github.com/koog-ai/koog-agents) to provide an intelligent cooking assistant powered by Google's Gemini models. The AI agent can help users with recipe suggestions, cooking tips, and navigation through the app.

#### Features
- **AI-Powered Chat**: Interactive cooking assistant that understands recipe-related queries
- **Multi-Model Support**: Supports Gemini 2.5 Pro, Flash, and Flash Lite models
- **Tool Integration**: AI agent can execute tools and provide contextual responses
- **Function Calling**: Advanced agentic behavior with tool registry support

#### API Key Configuration
To use the AI features, you need a Google Gemini API key:

1. Obtain a Gemini API key from [Google AI Studio](https://aistudio.google.com/app/apikey)
2. Create a `local.properties` file in the project root (if it doesn't exist)
3. Add your API key to the file:
   ```properties
   GEMINI_API_KEY=your_api_key_here
   ```
4. The key will be automatically loaded into `BuildConfig` during build

**Note:** The `local.properties` file is gitignored and should never be committed to version control.

### Dependencies
| Dependency                                                                                                              | Description                                                                            | 
|-------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------|
| [android-youtube-player](https://github.com/PierfrancescoSoffritti/android-youtube-player)                              | YouTube Player library for Android                                                     |
| [Coil](https://coil-kt.github.io/coil/)                                                                                 | Image Loader for Jetpack Compose                                                       |
| [Compose Adaptive](https://developer.android.com/jetpack/androidx/releases/compose-material3-adaptive)                  | Adaptive Layouts for Jetpack Compose for mutliple screensize and form factor detection |
| [Compose Adaptive Navigation Suite](https://developer.android.com/jetpack/androidx/releases/compose-material3-adaptive) | Adaptive Navigation Suite for Material 3 for Navigation widget screensize support      |
| [Compose Material3](https://developer.android.com/jetpack/androidx/releases/compose-material3)                          | Material Design Components for Jetpack Compose                                         |
| [Compose Material3 Expressive](https://developer.android.com/jetpack/androidx/releases/compose-material3)               | Expressive APIs for Material 3 for new widets and dynamic motions                      |
| [Compose Navigation3](https://github.com/android/nav3-recipes)                                                          | Navigation Component for Jetpack Compose                                               | 
| [Haze](https://chrisbanes.github.io/haze/latest/)                                                                       | Chris Banes 'glassmorphism' blur library for Compose.                                  |
| [Metro](https://github.com/ZacSweers/metro)                                                                             | Compile-time Dependency Injection for Kotlin by Zac Sweers                             | 
| [KmLogging](https://github.com/LighthouseGames/KmLogging)                                                               | Kotlin Multiplatform logging library.                                                  |
| [Koog AI Agents](https://github.com/koog-ai/koog-agents)                                                                | AI Agent framework with Gemini integration                                             |
| [Ktor](https://ktor.io/)                                                                                                | HTTP Client for Android                                                                |
| [Markdown Renderer](https://github.com/mikepenz/multiplatform-markdown-renderer)                                        | Mike Penz Multiplatform Markdown Renderer                                              |
| [TheMealDB](https://www.themealdb.com/api.php)                                                                          | Free, easy to use, API for Food Recipes                                                |
| [Truth](https://truth.dev/)                                                                                             | Google Assertion Library used for Testing                                              |
| [Turbine](https://github.com/cashapp/turbine)                                                                           | Coroutine Flow Testing Library from CashApp                                            |
