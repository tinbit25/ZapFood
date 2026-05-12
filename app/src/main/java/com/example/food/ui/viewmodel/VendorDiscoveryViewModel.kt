package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.data.model.ServiceTag
import com.example.food.data.model.Vendor
import com.example.food.data.model.VendorType
import com.example.food.data.repository.VendorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VendorDiscoveryViewModel(
    private val vendorRepository: VendorRepository = VendorRepository()
) : ViewModel() {

    private val _vendorsState = MutableStateFlow<Resource<List<Vendor>>>(Resource.Loading())
    val vendorsState: StateFlow<Resource<List<Vendor>>> = _vendorsState.asStateFlow()

    private val _selectedType = MutableStateFlow<VendorType?>(null)
    val selectedType: StateFlow<VendorType?> = _selectedType.asStateFlow()

    private val _selectedTag = MutableStateFlow<ServiceTag?>(null)
    val selectedTag: StateFlow<ServiceTag?> = _selectedTag.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        fetchVendors()
    }

    fun fetchVendors() {
        viewModelScope.launch {
            vendorRepository.getFilteredVendors(
                type = _selectedType.value,
                tag = _selectedTag.value,
                queryText = _searchQuery.value
            ).collect {
                _vendorsState.value = it
            }
        }
    }

    fun filterByType(type: VendorType?) {
        _selectedType.value = type
        fetchVendors()
    }

    fun filterByTag(tag: ServiceTag?) {
        _selectedTag.value = tag
        fetchVendors()
    }

    fun search(query: String) {
        _searchQuery.value = query
        fetchVendors()
    }
}
