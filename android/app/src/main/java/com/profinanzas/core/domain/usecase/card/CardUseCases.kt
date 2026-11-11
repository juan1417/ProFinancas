package com.profinanzas.core.domain.usecase.card

import com.profinanzas.core.domain.model.StoredCard
import com.profinanzas.core.domain.repository.CardRepository
import javax.inject.Inject

class GetCardsUseCase @Inject constructor(
    private val repository: CardRepository,
) {
    suspend operator fun invoke() = repository.list()
}

class CreateCardUseCase @Inject constructor(
    private val repository: CardRepository,
) {
    suspend operator fun invoke(card: StoredCard) = repository.create(card)
}

class DeleteCardUseCase @Inject constructor(
    private val repository: CardRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
