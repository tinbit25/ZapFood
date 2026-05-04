package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.data.model.User
import com.example.food.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        fetchUser()
    }

    private fun fetchUser() {
        viewModelScope.launch {
            userRepository.getUserProfile().collect { profile ->
                _user.value = profile
            }
        }
    }

    fun updateRewardPoints(points: Int) {
        val currentUser = _user.value ?: return
        _user.value = currentUser.copy(rewardPoints = currentUser.rewardPoints + points)
    }

    fun updateRole(newRole: com.example.food.data.model.UserRole) {
        val currentUser = _user.value ?: return
        _user.value = currentUser.copy(role = newRole)
    }
}
