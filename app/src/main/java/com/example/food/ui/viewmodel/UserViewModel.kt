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
    private val userRepository: UserRepository = UserRepository(),
    private val profileRepository: com.example.food.data.repository.UserProfileRepository = com.example.food.data.repository.UserProfileRepository()
) : ViewModel() {

    private val _uploadProgress = MutableStateFlow<Int?>(null)
    val uploadProgress: StateFlow<Int?> = _uploadProgress.asStateFlow()

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

    fun updateProfile(updatedUser: User) {
        viewModelScope.launch {
            try {
                profileRepository.updateProfile(updatedUser)
                _user.value = updatedUser
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun uploadProfilePicture(uri: android.net.Uri) {
        val userId = _user.value?.userId ?: return
        viewModelScope.launch {
            profileRepository.uploadProfilePicture(userId, uri).collect { status ->
                when (status) {
                    is com.example.food.data.repository.UploadStatus.Progress -> {
                        _uploadProgress.value = status.percentage
                    }
                    is com.example.food.data.repository.UploadStatus.Success -> {
                        _uploadProgress.value = null
                        profileRepository.updatePhotoUrl(userId, status.downloadUrl)
                        _user.value = _user.value?.copy(photoUrl = status.downloadUrl)
                    }
                    is com.example.food.data.repository.UploadStatus.Error -> {
                        _uploadProgress.value = null
                    }
                }
            }
        }
    }
}
