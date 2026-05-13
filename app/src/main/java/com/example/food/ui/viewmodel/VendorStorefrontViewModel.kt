package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.data.model.Meal
import com.example.food.data.model.Vendor
import com.example.food.data.repository.MealRepository
import com.example.food.data.repository.VendorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class StorefrontUiState(
    val vendor: Vendor? = null,
    val meals: List<Meal> = emptyList(),
    val filteredMeals: List<Meal> = emptyList(),
    val categories: List<String> = listOf("All", "Breakfast", "Lunch", "Dinner", "Drinks", "Desserts", "Traditional", "Vegan/Fasting"),
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class VendorStorefrontViewModel(
    private val vendorRepository: VendorRepository = VendorRepository(),
    private val mealRepository: MealRepository = MealRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StorefrontUiState())
    val uiState: StateFlow<StorefrontUiState> = _uiState.asStateFlow()

    fun loadVendorStorefront(vendorId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Load Vendor Info
            val vendor = vendorRepository.getVendorById(vendorId)
            _uiState.value = _uiState.value.copy(vendor = vendor)
            
            // Load Menu Items
            mealRepository.getMealsByVendor(vendorId).collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val allMeals = resource.data ?: emptyList()
                        _uiState.value = _uiState.value.copy(
                            meals = allMeals,
                            filteredMeals = allMeals,
                            isLoading = false
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = resource.message,
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun onCategorySelected(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        applyFilters()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        val allMeals = currentState.meals
        val category = currentState.selectedCategory
        val query = currentState.searchQuery

        val filtered = allMeals.filter { meal ->
            val matchesCategory = when (category) {
                "All" -> true
                "Breakfast" -> meal.mealTime.any { it.name == "BREAKFAST" }
                "Lunch" -> meal.mealTime.any { it.name == "LUNCH" }
                "Dinner" -> meal.mealTime.any { it.name == "DINNER" }
                "Drinks" -> meal.category.contains("Drink", ignoreCase = true)
                "Desserts" -> meal.category.contains("Dessert", ignoreCase = true)
                "Traditional" -> meal.traditionalCategory.isNotBlank() || meal.cuisineType.name == "ETHIOPIAN"
                "Vegan/Fasting" -> meal.fastingFriendly || meal.veganFriendly
                else -> true
            }

            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                meal.name.contains(query, ignoreCase = true) ||
                meal.description.contains(query, ignoreCase = true) ||
                meal.tags.any { it.contains(query, ignoreCase = true) }
            }

            matchesCategory && matchesQuery
        }

        _uiState.value = _uiState.value.copy(filteredMeals = filtered)
    }
}
