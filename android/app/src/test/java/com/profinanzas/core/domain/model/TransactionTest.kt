package com.profinanzas.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionTest {
    private fun tx(type: String, amount: Double) = Transaction(
        id = 1, type = type, amount = amount, description = "d",
        categoryId = 1, categoryName = null,
        transactionDate = "2026-07-15T12:00:00", notes = null,
    )

    @Test
    fun signedAmountIsPositiveForIncome() {
        assertEquals(12.5, tx("INCOME", 12.5).signedAmount, 0.001)
    }

    @Test
    fun signedAmountIsNegativeForExpense() {
        assertEquals(-12.5, tx("EXPENSE", 12.5).signedAmount, 0.001)
    }

    @Test
    fun isExpenseFlag() {
        assertEquals(true, tx("EXPENSE", 1.0).isExpense)
        assertEquals(false, tx("INCOME", 1.0).isExpense)
    }
}
