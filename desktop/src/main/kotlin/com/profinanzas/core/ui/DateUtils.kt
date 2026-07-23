package com.profinanzas.core.ui

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val tz = ZoneId.of("America/Bogota")
private val displayLocale = Locale("es", "CO")

fun formatRelativeDate(isoDate: String): String {
    return try {
        val dateTime = parseIsoDate(isoDate) ?: return isoDate
        val now = LocalDateTime.now(tz)
        val today = now.toLocalDate()
        val date = dateTime.toLocalDate()

        when {
            date == today -> "Hoy, ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            date == today.minusDays(1) -> "Ayer, ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            date >= today.minusDays(6) -> {
                val dayName = dateTime.format(DateTimeFormatter.ofPattern("EEE", displayLocale))
                "${dayName.replaceFirstChar { it.uppercase() }}, ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            }
            else -> dateTime.format(DateTimeFormatter.ofPattern("d MMM yyyy", displayLocale))
        }
    } catch (_: Exception) {
        isoDate
    }
}

fun formatDate(isoDate: String): String {
    return try {
        val dateTime = parseIsoDate(isoDate) ?: return isoDate
        dateTime.format(DateTimeFormatter.ofPattern("d MMM yyyy", displayLocale))
    } catch (_: Exception) {
        isoDate
    }
}

fun formatDateTime(isoDate: String): String {
    return try {
        val dateTime = parseIsoDate(isoDate) ?: return isoDate
        dateTime.format(DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", displayLocale))
    } catch (_: Exception) {
        isoDate
    }
}

fun formatMonthYear(): String {
    return LocalDate.now(tz).format(DateTimeFormatter.ofPattern("MMMM yyyy", displayLocale))
        .replaceFirstChar { it.uppercase() }
}

private fun parseIsoDate(isoDate: String): LocalDateTime? {
    if (isoDate.isBlank()) return null

    return try {
        val offsetDateTime = Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(isoDate))
        offsetDateTime.atZone(tz).toLocalDateTime()
    } catch (_: Exception) {
        try {
            val localDateTime = LocalDateTime.parse(isoDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            localDateTime.atZone(tz).toLocalDateTime()
        } catch (_: Exception) {
            try {
                val localDate = LocalDate.parse(isoDate, DateTimeFormatter.ISO_LOCAL_DATE)
                localDate.atStartOfDay(tz).toLocalDateTime()
            } catch (_: Exception) {
                null
            }
        }
    }
}
