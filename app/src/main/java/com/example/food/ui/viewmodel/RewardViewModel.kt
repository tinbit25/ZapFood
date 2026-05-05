package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.domain.usecase.MPCodeUseCase
import com.example.food.domain.usecase.RewardUseCase
import com.example.food.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RewardViewModel(
    private val rewardUseCase: RewardUseCase = RewardUseCase(),
    private val mpCodeUseCase: MPCodeUseCase = MPCodeUseCase()
) : ViewModel() {

    private val _pointsBalance = MutableStateFlow(0)
    val pointsBalance: StateFlow<Int> = _pointsBalance.asStateFlow()

    private val _importState = MutableStateFlow<Resource<MealPlan>?>(null)
    val importState: StateFlow<Resource<MealPlan>?> = _importState.asStateFlow()

    private val _generatedCode = MutableStateFlow<Resource<String>?>(null)
    val generatedCode: StateFlow<Resource<String>?> = _generatedCode.asStateFlow()

    fun fetchBalance(userId: String) {
        viewModelScope.launch {
            val result = rewardUseCase.getBalance(userId)
            if (result is Resource.Success) {
                _pointsBalance.value = result.data ?: 0
            }
        }
    }

    fun generateCode(user: User, planId: String) {
        viewModelScope.launch {
            _generatedCode.value = Resource.Loading()
            _generatedCode.value = mpCodeUseCase.generateCode(user, planId)
        }
    }

    fun importPlan(user: User, code: String) {
        viewModelScope.launch {
            _importState.value = Resource.Loading()
            val result = mpCodeUseCase.importPlan(user, code)
            _importState.value = result
            if (result is Resource.Success) {
                fetchBalance(user.userId)
            }
        }
    }

    fun redeemPoints(userId: String, points: Int) {
        viewModelScope.launch {
            val result = rewardUseCase.redeemPoints(userId, points)
            if (result is Resource.Success) {
                fetchBalance(userId)
            }
        }
    }
}
