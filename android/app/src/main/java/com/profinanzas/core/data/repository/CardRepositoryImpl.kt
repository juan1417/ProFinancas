package com.profinanzas.core.data.repository

import com.profinanzas.core.api.ApiService
import com.profinanzas.core.data.model.CardCreateRequest
import com.profinanzas.core.data.model.CardDto
import com.profinanzas.core.domain.model.StoredCard
import com.profinanzas.core.domain.repository.CardRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepositoryImpl @Inject constructor(
    private val api: ApiService,
) : CardRepository {

    private fun CardDto.toDomain(): StoredCard {
        val epoch = try {
            java.time.Instant.parse(createdAt ?: "").toEpochMilli()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
        return StoredCard(
            id = id.toString(),
            brand = brand,
            bank = bank,
            last4 = last4,
            expiry = expiry,
            holderName = holderName,
            colorHex = colorHex,
            createdAt = epoch,
        )
    }

    override suspend fun list(): Result<List<StoredCard>> = runCatching {
        val response = api.getCards()
        if (response.isSuccessful) response.body()!!.map { it.toDomain() }
        else throw Exception("Error al cargar tarjetas")
    }

    override suspend fun create(card: StoredCard): Result<StoredCard> = runCatching {
        val response = api.createCard(
            CardCreateRequest(
                brand = card.brand,
                bank = card.bank,
                last4 = card.last4,
                expiry = card.expiry,
                holderName = card.holderName,
                colorHex = card.colorHex,
            )
        )
        if (response.isSuccessful) response.body()!!.toDomain()
        else throw Exception("Error al crear tarjeta")
    }

    override suspend fun delete(id: String): Result<Unit> = runCatching {
        val response = api.deleteCard(id.toInt())
        if (!response.isSuccessful && response.code() != 204) {
            throw Exception("Error al eliminar tarjeta")
        }
    }
}
