package com.profinanzas.core.data.repository

import com.profinanzas.core.api.ApiService
import com.profinanzas.core.data.model.CategoryCreateRequest
import com.profinanzas.core.domain.model.Category
import com.profinanzas.core.domain.repository.CategoryRepository

class CategoryRepositoryImpl(
    private val api: ApiService,
) : CategoryRepository {

    private fun com.profinanzas.core.data.model.CategoryDto.toDomain() = Category(
        id = id,
        name = name,
        type = type,
        description = description,
        isActive = isActive,
        transactionsCount = transactionsCount,
    )

    override suspend fun list(type: String?, isActive: Boolean?): Result<List<Category>> = runCatching {
        val response = api.getCategories(type = type, isActive = isActive)
        if (response.isSuccessful) response.body()!!.map { it.toDomain() }
        else throw Exception("Error al cargar categorías")
    }

    override suspend fun create(name: String, type: String, description: String?): Result<Category> = runCatching {
        val response = api.createCategory(CategoryCreateRequest(name, type, description))
        if (response.isSuccessful) response.body()!!.toDomain()
        else throw Exception("Error al crear categoría")
    }

    override suspend fun update(id: Int, name: String, type: String, description: String?): Result<Category> = runCatching {
        val response = api.updateCategory(id, CategoryCreateRequest(name, type, description))
        if (response.isSuccessful) response.body()!!.toDomain()
        else throw Exception("Error al actualizar categoría")
    }

    override suspend fun delete(id: Int): Result<Unit> = runCatching {
        val response = api.deleteCategory(id)
        if (!response.isSuccessful && response.code() != 204) {
            throw Exception("Error al eliminar categoría")
        }
    }
}
