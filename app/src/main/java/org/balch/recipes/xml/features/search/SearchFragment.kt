package org.balch.recipes.xml.features.search

import android.R.attr.category
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.balch.recipes.R
import org.balch.recipes.core.coroutines.DispatcherProvider
import org.balch.recipes.core.models.DetailType
import org.balch.recipes.core.models.SearchType
import org.balch.recipes.core.repository.RecipeRepository
import org.balch.recipes.databinding.FragmentSearchBinding
import org.balch.recipes.features.search.SearchUiState
import org.balch.recipes.features.search.SearchViewModel
import org.balch.recipes.xml.features.details.DetailFragment
import javax.inject.Inject


@Suppress("UNCHECKED_CAST")
@AndroidEntryPoint
class SearchFragment : Fragment() {

    @Inject
    lateinit var repository: RecipeRepository

    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    private val searchType: SearchType by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_SEARCH_TYPE, SearchType::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_SEARCH_TYPE)
        } ?: SearchType.Search("")
    }

    private val viewModel: SearchViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SearchViewModel(
                    searchType = searchType,
                    repository = repository,
                    dispatcherProvider = dispatcherProvider,
                ) as T
            }
        }
    }
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchAdapter: SearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeUiState()
//        setupSearchView()

        binding.retryButton.setOnClickListener {
            // Retry logic can be implemented in the ViewModel
        }
    }

    private fun setupRecyclerView() {
        searchAdapter = SearchAdapter(
            onItemClicked = { mealDescriptor ->
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.root_layout,
                        DetailFragment.newInstance(DetailType.Lookup(mealDescriptor.id))
                    )
                    .addToBackStack(null)
                    .commit()
            }
        )
        binding.recyclerView.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.updateSearchQuery(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.updateSearchQuery(it) }
                return true
            }
        })

        if (searchType.searchText.isNotEmpty()) {
            binding.searchView.setQuery(searchType.searchText, true)
        }
   }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.uiState.collect { state ->
                    withContext(dispatcherProvider.main) {
                        handleState(state)
                    }
                }
            }
        }
    }

    private fun handleState(state: SearchUiState) {
        binding.progressBar.isVisible = state is SearchUiState.Loading || (state is SearchUiState.Show && state.isFetching)
        binding.recyclerView.isVisible = state is SearchUiState.Show
        binding.errorMessage.isVisible = state is SearchUiState.Error
        binding.retryButton.isVisible = state is SearchUiState.Error
        binding.welcomeMessage.isVisible = state is SearchUiState.Welcome

        when (state) {
            is SearchUiState.Show -> {
                searchAdapter.submitList(state.meals)
                binding.searchView.isVisible = true
            }
            is SearchUiState.Error -> {
                binding.errorMessage.text = state.message
            }
            is SearchUiState.Loading -> {
                binding.searchView.isVisible = state.showSearchBar
            }
            is SearchUiState.Welcome -> {
                binding.searchView.isVisible = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_SEARCH_TYPE = "search_type"

        @JvmStatic
        fun newInstance(searchType: SearchType) = SearchFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_SEARCH_TYPE, searchType)
            }
        }
    }
}
