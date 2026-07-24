package com.profinanzas.features.categories

import com.profinanzas.core.domain.model.Category
import com.profinanzas.core.domain.usecase.category.CreateCategoryUseCase
import com.profinanzas.core.domain.usecase.category.DeleteCategoryUseCase
import com.profinanzas.core.domain.usecase.category.ListCategoriesUseCase
import com.profinanzas.core.domain.usecase.category.UpdateCategoryUseCase
import com.profinanzas.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedType: String? = null,
)

class CategoriesViewModel(
    private val listCategories: ListCategoriesUseCase = AppModule.listCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase = AppModule.createCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase = AppModule.updateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase = AppModule.deleteCategoryUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories(type: String? = null) {
        _uiState.value = _uiState.value.copy(selectedType = type, isLoading = true)
        scope.launch {
            listCategories(type = type)
                .onSuccess { categories ->
                    _uiState.value = _uiState.value.copy(categories = categories, isLoading = false)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
                }
        }
    }

    fun createCategory(name: String, type: String, description: String?) {
        scope.launch {
            createCategoryUseCase(name, type, description)
                .onSuccess { loadCategories(_uiState.value.selectedType) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun updateCategory(id: Int, name: String, type: String, description: String?) {
        scope.launch {
            updateCategoryUseCase(id, name, type, description)
                .onSuccess { loadCategories(_uiState.value.selectedType) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun deleteCategory(id: Int) {
        scope.launch {
            deleteCategoryUseCase(id)
                .onSuccess { loadCategories(_uiState.value.selectedType) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }
}
