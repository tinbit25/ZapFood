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
    val traditional: List<Vendor> = emptyList(),
    val cafes: List<Vendor> = emptyList(),
    val budget: List<Vendor> = emptyList(),
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
            
            // Parallel loading of discovery sections
            val discoveryJobs = listOf(
                launch { fetchSection("rating") { _uiState.value = _uiState.value.copy(topRated = it) } },
                launch { fetchSection("totalOrders") { _uiState.value = _uiState.value.copy(popular = it) } },
                launch { fetchSection("createdAt") { _uiState.value = _uiState.value.copy(newVendors = it) } },
                launch { 
                    vendorRepository.getVendors(sortBy = "deliveryTimeMin", limit = 10).collectLatest { resource ->
                        if (resource is Resource.Success) {
                            _uiState.value = _uiState.value.copy(fastDelivery = resource.data ?: emptyList())
                        }
                    }
                },
                launch {
                    vendorRepository.getVendors(type = VendorType.TRADITIONAL_ETHIOPIAN, limit = 10).collectLatest { resource ->
                        if (resource is Resource.Success) {
                            _uiState.value = _uiState.value.copy(traditional = resource.data ?: emptyList())
                        }
                    }
                },
                launch {
                    vendorRepository.getVendors(type = VendorType.CAFE, limit = 10).collectLatest { resource ->
                        if (resource is Resource.Success) {
                            _uiState.value = _uiState.value.copy(cafes = resource.data ?: emptyList())
                        }
                    }
                },
                launch {
                    // Budget friendly simulated by low delivery fee for now
                    vendorRepository.getVendors(sortBy = "deliveryFee", limit = 10).collectLatest { resource ->
                        if (resource is Resource.Success) {
                            _uiState.value = _uiState.value.copy(budget = resource.data?.reversed() ?: emptyList())
                        }
                    }
                }
            )
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private suspend fun fetchSection(sortBy: String, update: (List<Vendor>) -> Unit) {
        vendorRepository.getVendors(sortBy = sortBy, limit = 10).collectLatest { resource ->
            if (resource is Resource.Success) {
                update(resource.data ?: emptyList())
            }
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
