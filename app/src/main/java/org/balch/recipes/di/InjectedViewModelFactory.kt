package org.balch.recipes.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Provider
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * A [ViewModelProvider.Factory] that uses Metro DI to create ViewModels.
 * 
 * This factory uses injected maps of ViewModel classes to their providers,
 * allowing Metro to construct ViewModels with proper dependency injection.
 */
@ContributesBinding(AppScope::class)
class InjectedViewModelFactory @Inject constructor(
    override val viewModelProviders: Map<KClass<out ViewModel>, Provider<ViewModel>>,
    override val assistedFactoryProviders: Map<KClass<out ViewModel>, Provider<ViewModelAssistedFactory>>,
    override val manualAssistedFactoryProviders: Map<KClass<out ManualViewModelAssistedFactory>, Provider<ManualViewModelAssistedFactory>>,
) : MetroViewModelFactory()
