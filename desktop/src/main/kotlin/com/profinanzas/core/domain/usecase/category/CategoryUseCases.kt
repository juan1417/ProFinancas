package com.profinanzas.core.domain.usecase.category

import com.profinanzas.core.domain.repository.CategoryRepository

class ListCategoriesUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(type: String? = null, isActive: Boolean? = null) =
        repository.list(type, isActive)
}

class CreateCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(name: String, type: String, description: String?) =
        repository.create(name, type, description)
}

class UpdateCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(id: Int, name: String, type: String, description: String?) =
        repository.update(id, name, type, description)
}

class DeleteCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(id: Int) = repository.delete(id)
}
