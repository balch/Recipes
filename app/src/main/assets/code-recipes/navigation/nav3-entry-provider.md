## Description

- Use **Nav3** `entryProvider` DSL syntax for simple App Nav
- Provides a convenient way to create ***ViewModels*** and ***Screens*** on the ***backstack***

## Code Snippet

```
entryProvider = entryProvider {
    entry<Ideas> {
        IdeasScreen(
            // ...
        )
    }
    entry<SearchRoute> { searchRoute ->
        // ...
    }
    entry<Search> {
        // ...
    }
    entry<DetailRoute> { detailRoute ->
        // ...
    }
    entry<Info> { InfoScreen() }
}
```