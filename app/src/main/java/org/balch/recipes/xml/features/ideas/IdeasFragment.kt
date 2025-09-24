package org.balch.recipes.xml.features.ideas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.balch.recipes.R
import org.balch.recipes.core.models.SearchType
import org.balch.recipes.databinding.FragmentIdeasBinding
import org.balch.recipes.features.ideas.IdeasUiState
import org.balch.recipes.features.ideas.IdeasViewModel
import org.balch.recipes.xml.features.search.SearchFragment

/**
 * A fragment representing a list of Items.
 */
@AndroidEntryPoint
class IdeasFragment : Fragment() {

    private val viewModel: IdeasViewModel by viewModels()
    private var _binding: FragmentIdeasBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIdeasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeUiState()

        binding.retryButton.setOnClickListener {
            viewModel.retry()
        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            onItemClicked = { category ->
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.root_layout,
                        SearchFragment.newInstance(SearchType.Category(category.name))
                    )
                    .addToBackStack(null)
                    .commit()
            }
        )

        binding.recyclerView.apply {
            adapter = categoryAdapter
            // a more reasonable column count
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleState(state)
                }
            }
        }
    }

    private fun handleState(state: IdeasUiState) {
        binding.progressBar.isVisible = state is IdeasUiState.Loading
        binding.recyclerView.isVisible = state is IdeasUiState.Categories
        binding.errorMessage.isVisible = state is IdeasUiState.Error
        binding.retryButton.isVisible = state is IdeasUiState.Error

        when (state) {
            is IdeasUiState.Categories -> {
                categoryAdapter.submitList(state.categories)
            }
            is IdeasUiState.Error -> {
                binding.errorMessage.text = state.message
            }
            is IdeasUiState.Loading -> {
                // handled by isVisible
            }
            is IdeasUiState.Areas -> {
                // not handled in this fragment
            }
            is IdeasUiState.Ingredients -> {
                // not handled in this fragment
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            IdeasFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
