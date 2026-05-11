package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.data.model.Address
import com.example.food.data.repository.AddressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddressViewModel(
    private val addressRepository: AddressRepository = AddressRepository()
) : ViewModel() {

    private val _addresses = MutableStateFlow<List<Address>>(emptyList())
    val addresses: StateFlow<List<Address>> = _addresses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun fetchAddresses(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _addresses.value = addressRepository.getAddresses(userId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addAddress(userId: String, address: Address) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                addressRepository.addAddress(userId, address)
                fetchAddresses(userId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAddress(userId: String, addressId: String) {
        viewModelScope.launch {
            addressRepository.deleteAddress(userId, addressId)
            fetchAddresses(userId)
        }
    }

    fun setDefaultAddress(userId: String, addressId: String) {
        viewModelScope.launch {
            addressRepository.setDefaultAddress(userId, addressId)
            fetchAddresses(userId)
        }
    }
}
