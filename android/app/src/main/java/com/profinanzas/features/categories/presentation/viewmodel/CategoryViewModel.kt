package com.profinanzas.features.categories.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profinanzas.core.domain.model.Category
import com.profinanzas.core.domain.usecase.category.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val listCategories: ListCategoriesUseCase,
    private val createCategory: CreateCategoryUseCase,
    private val updateCategory: UpdateCategoryUseCase,
    private val deleteCategory: DeleteCategoryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll(type: String? = null, isActive: Boolean? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            listCategories(type, isActive)
                .onSuccess { categories ->
                    _uiState.value = _uiState.value.copy(
                        categories = categories,
                        isLoading = false,
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message,
                    )
                }
        }
    }

    fun create(name: String, type: String, description: String?) {
        viewModelScope.launch {
            createCategory(name, type, description)
                .onSuccess { category ->
                    _uiState.value = _uiState.value.copy(
                        categories = _uiState.value.categories + category,
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
        }
    }

    fun update(id: Int, name: String, type: String, description: String?) {
        viewModelScope.launch {
            updateCategory(id, name, type, description)
                .onSuccess { updated ->
                    _uiState.value = _uiState.value.copy(
                        categories = _uiState.value.categories.map {
                            if (it.id == updated.id) updated else it
                        },
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            deleteCategory(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        categories = _uiState.value.categories.filter { it.id != id },
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
