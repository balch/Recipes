## Description

- Create extension functions for custom assertions to improve test readability and reusability

## Code Snippet

```
private fun UiState.assertValidShowState(meal: Meal) {
    assertThat(this).isInstanceOf(UiState.ShowMeal::class.java)
    val state = this as UiState.ShowMeal
    assertThat(state.meal).isEqualTo(meal)
}

private fun UiState.assertErrorState(message: String) {
    assertThat(this).isInstanceOf(UiState.Error::class.java)
    val state = this as UiState.Error
    assertThat(state.message).isEqualTo(message)
}
```