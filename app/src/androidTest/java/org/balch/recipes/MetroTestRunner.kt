package org.balch.recipes

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/**
 * Custom test runner for Metro instrumented tests.
 * Metro doesn't require a special test application - we use the standard RecipesApplication.
 */
class MetroTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        // Use the regular RecipesApplication since Metro doesn't need special test setup
        return super.newApplication(cl, RecipesApplication::class.java.name, context)
    }
}
