package com.profinanzas.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.profinanzas.core.ui.components.ErrorBanner
import com.profinanzas.core.ui.theme.ProFinanzasTheme
import org.junit.Rule
import org.junit.Test

class ErrorBannerTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysMessageWhenProvided() {
        composeTestRule.setContent {
            ProFinanzasTheme {
                ErrorBanner(message = "Fallo de red", onDismiss = {})
            }
        }
        composeTestRule.onNodeWithText("Fallo de red").assertIsDisplayed()
    }

    @Test
    fun hidesWhenMessageIsNull() {
        composeTestRule.setContent {
            ProFinanzasTheme {
                ErrorBanner(message = null)
            }
        }
        composeTestRule.onNodeWithText("Cerrar").assertDoesNotExist()
    }
}
