package com.profinanzas.core.domain.repository

import com.profinanzas.core.domain.model.StoredCard

interface CardRepository {
    suspend fun list(): Result<List<StoredCard>>
    suspend fun create(card: StoredCard): Result<StoredCard>
    suspend fun delete(id: String): Result<Unit>
}
