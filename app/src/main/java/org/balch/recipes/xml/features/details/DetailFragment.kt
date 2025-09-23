package org.balch.recipes.xml.features.details

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil3.load
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.balch.recipes.core.coroutines.DispatcherProvider
import org.balch.recipes.core.models.DetailType
import org.balch.recipes.core.repository.RecipeRepository
import org.balch.recipes.databinding.FragmentDetailBinding
import org.balch.recipes.features.details.DetailsViewModel
import org.balch.recipes.features.details.UiState
import javax.inject.Inject
import kotlin.getValue

@Suppress("UNCHECKED_CAST")
@AndroidEntryPoint
class DetailFragment : Fragment() {

    @Inject
    lateinit var repository: RecipeRepository

    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    private val detailType: DetailType by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_DETAIL_TYPE, DetailType::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_DETAIL_TYPE)
        } ?: DetailType.Random
    }

    private val viewModel: DetailsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DetailsViewModel(
                    detailType = detailType,
                    repository = repository,
                    dispatcherProvider = dispatcherProvider
                ) as T
            }
        }
    }
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.recipeImage.visibility = View.GONE
                        }

                        is UiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.recipeImage.visibility = View.GONE
                            binding.errorMessage.visibility = View.VISIBLE
                            binding.errorMessage.text = state.message
                        }

                        is UiState.Show -> {
                            binding.progressBar.visibility = View.GONE
                            binding.recipeImage.visibility = View.VISIBLE
                            binding.recipeImage.load(state.meal.thumbnail)
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val ARG_DETAIL_TYPE = "detailType"

        @JvmStatic
        fun newInstance(detailType: DetailType) =
            DetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_DETAIL_TYPE, detailType)
                }
            }
    }
}