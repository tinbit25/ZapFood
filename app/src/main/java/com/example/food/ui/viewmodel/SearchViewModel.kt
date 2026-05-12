package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.data.model.Meal
import com.example.food.data.model.Vendor
import com.example.food.data.model.VendorType
import com.example.food.data.repository.MealRepository
import com.example.food.data.repository.VendorRepository
import com.example.food.data.repository.RecentSearchRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SearchMode {
    FOOD, VENDOR
}

data class SearchUiState(
    val searchMode: SearchMode = SearchMode.FOOD,
    val foodResults: List<Meal> = emptyList(),
    val vendorResults: List<Vendor> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedVendorCategory: VendorType? = null,
    val isFastingFilter: Boolean = false,
    val isVeganFilter: Boolean = false,
    val isSpicyFilter: Boolean = false
)

class SearchViewModel(
    private val mealRepository: MealRepository = MealRepository(),
    private val vendorRepository: VendorRepository = VendorRepository(),
    private val recentSearchRepository: RecentSearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    init {
        viewModelScope.launch {
            recentSearchRepository.recentSearches.collect { searches ->
                _uiState.value = _uiState.value.copy(recentSearches = searches)
            }
        }
    }

    fun setMode(mode: SearchMode) {
        _uiState.value = _uiState.value.copy(searchMode = mode)
        performSearch()
    }

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
        performSearch()
    }

    fun setVendorCategory(category: VendorType?) {
        _uiState.value = _uiState.value.copy(selectedVendorCategory = category)
        performSearch()
    }

    fun toggleFastingFilter() {
        _uiState.value = _uiState.value.copy(isFastingFilter = !_uiState.value.isFastingFilter)
        performSearch()
    }

    fun addRecentSearch(query: String) {
        viewModelScope.launch {
            recentSearchRepository.addSearch(query)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            recentSearchRepository.clearHistory()
        }
    }

    private fun performSearch() {
        val currentQuery = _query.value
        val state = _uiState.value

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            if (state.searchMode == SearchMode.FOOD) {
                // Food-first discovery
                mealRepository.getMeals().collectLatest { resource ->
                    if (resource is Resource.Success) {
                        var filtered = resource.data ?: emptyList()
                        
                        // Apply Text Search
                        if (currentQuery.isNotBlank()) {
                            filtered = filtered.filter { meal: Meal ->
                                meal.name.contains(currentQuery, ignoreCase = true) || 
                                meal.tags.any { tag -> tag.contains(currentQuery, ignoreCase = true) } 
                            }
                        }

                        // Apply Ethiopian Filters
                        if (state.isFastingFilter) filtered = filtered.filter { meal: Meal -> meal.fastingFriendly }
                        if (state.isVeganFilter) filtered = filtered.filter { meal: Meal -> meal.veganFriendly }
                        
                        _uiState.value = _uiState.value.copy(foodResults = filtered, isLoading = false)
                    }
                }
            } else {
                // Vendor-first discovery
                vendorRepository.getVendors(
                    type = state.selectedVendorCategory,
                    queryText = currentQuery
                ).collectLatest { resource ->
                    if (resource is Resource.Success) {
                        _uiState.value = _uiState.value.copy(vendorResults = resource.data ?: emptyList(), isLoading = false)
                    }
                }
            }
        }
    }
}
