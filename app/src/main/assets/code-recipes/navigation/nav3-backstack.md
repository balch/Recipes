## Description

- In **Nav3**, you own the ***backstack***.
- Store ***backstack*** in `SnapshotStateList<NavKey>` 
- Push/Pop works for simple applications

## Code Snippet

```
@ActivityRetainedScoped
class BackstackManager @Inject constructor() {
    private val _backstack : SnapshotStateList<NavKey> = mutableStateListOf(Ideas)

    val backstack: List<NavKey>
        get() = _backstack.toList()

    /**
     * Returns `true` if there is only one screen in the backstack
     * and the app will close when the back button is pressed
     */
    val isLastScreen: Boolean
        get() = _backstack.size == 1

    fun push(destination: NavKey){
        _backstack.add(destination)
    }

    fun pop(){
        _backstack.removeLastOrNull()
    }

    fun peek(): NavKey? =
        _backstack.lastOrNull()
}
```