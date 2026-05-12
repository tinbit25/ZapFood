package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.data.model.Vendor
import com.example.food.data.model.VendorType
import com.example.food.data.repository.VendorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class DiscoveryUiState(
    val topRated: List<Vendor> = emptyList(),
    val popular: List<Vendor> = emptyList(),
    val fastDelivery: List<Vendor> = emptyList(),
    val newVendors: List<Vendor> = emptyList(),
    val filteredVendors: List<Vendor> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class VendorDiscoveryViewModel(
    private val vendorRepository: VendorRepository = VendorRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoveryUiState())
    val uiState: StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<VendorType?>(null)
    val selectedCategory: StateFlow<VendorType?> = _selectedCategory.asStateFlow()

    init {
        loadDiscoveryData()
    }

    private fun loadDiscoveryData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Parallel loading or sequential for simplicity here
            launch {
                vendorRepository.getTopRatedVendors().collectLatest { resource ->
                    if (resource is Resource.Success) {
                        _uiState.value = _uiState.value.copy(topRated = resource.data ?: emptyList())
                    }
                }
            }
            
            launch {
                vendorRepository.getPopularVendors().collectLatest { resource ->
                    if (resource is Resource.Success) {
                        _uiState.value = _uiState.value.copy(popular = resource.data ?: emptyList())
                    }
                }
            }

            launch {
                vendorRepository.getNewVendors().collectLatest { resource ->
                    if (resource is Resource.Success) {
                        _uiState.value = _uiState.value.copy(newVendors = resource.data ?: emptyList())
                    }
                }
            }
            
            // Fast Delivery (simulated by sorting by deliveryTimeMin)
            launch {
                vendorRepository.getVendors(sortBy = "deliveryTimeMin", limit = 10).collectLatest { resource ->
                    if (resource is Resource.Success) {
                        _uiState.value = _uiState.value.copy(fastDelivery = resource.data ?: emptyList())
                    }
                }
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        performFilteredSearch()
    }

    fun onCategorySelected(category: VendorType?) {
        _selectedCategory.value = category
        performFilteredSearch()
    }

    private fun performFilteredSearch() {
        viewModelScope.launch {
            vendorRepository.getVendors(
                type = _selectedCategory.value,
                queryText = _searchQuery.value
            ).collectLatest { resource ->
                if (resource is Resource.Success) {
                    _uiState.value = _uiState.value.copy(filteredVendors = resource.data ?: emptyList())
                }
            }
        }
    }
}
