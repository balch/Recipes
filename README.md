# Recipe Reference App

### WHAT IS THIS?
Keeping up with the latest Android trends is always a challenge. One of the techniques I use is to always have a reference app handy. These apps should showcase the latest Android architecture and patterns for the following areas:
- UI and Biz Logic Separation
- Navigation UX and Theme
- Data and Image Retrieval
- Threading
- Dependency Injection
- Testing

The latest incarnation for my reference app is called Recipes. 

### Screenshots
|                                      Categories                                      |                                      Cuisine                                       |                                        Detail List                                         |                                        Detail Step By Step                                         |
|:------------------------------------------------------------------------------------:|:----------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------------:|:--------------------------------------------------------------------------------------------------:|
|  <img src="screenshots/darkmode-category.png" width="200" alt="Dark Mode Category">  |  <img src="screenshots/darkmode-cuisine.png" width="200" alt="Dark Mode Cuisine">  |  <img src="screenshots/darkmode-detail-list.png" width="200" alt="Dark Mode Detail List">  |  <img src="screenshots/darkmode-detail-step.png" width="200" alt="Dark Mode Detail Step By Step">  |
| <img src="screenshots/lightmode-category.png" width="200" alt="Light Mode Category"> | <img src="screenshots/lightmode-desert.png" width="200" alt="Light Mode Category"> | <img src="screenshots/lightmode-detail-list.png" width="200" alt="Light Mode Detail List"> | <img src="screenshots/lightmode-detail-step.png" width="200" alt="Light Mode Detail Step By Step"> |


### Notable Dependencies
| Dependency               | Description             | 
|--------------------------|-------------------------|
| [TheMealDB](https://www.themealdb.com/api.php) | Free, easy to use, API for Food Recipes |
| [Compose Navigation3](https://github.com/android/nav3-recipes) | Navigation Component for Jetpack Compose | 
| [Compose Material3](https://developer.android.com/jetpack/androidx/releases/compose-material3) | Material Design Components for Jetpack Compose | 
| [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) | Dependency Injection for Android | 
| [Ktor](https://ktor.io/) | HTTP Client for Android | 
| [Coil](https://coil-kt.github.io/coil/) | Image Loader for Jetpack Compose |  
| [KmLogging](https://github.com/LighthouseGames/KmLogging) |  Kotlin Multiplatform logging library (v2.0.3). |
| [Haze](https://chrisbanes.github.io/haze/latest/) | Chris Banes 'glassmorphism' blur library for Compose. |
| [Turbine](https://github.com/cashapp/turbine) | Couroutine Flow Testing Library from CashApp |
| [Truth](https://truth.dev/) | Google Assertion Library used for Testing |
