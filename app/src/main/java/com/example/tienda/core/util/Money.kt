package com.example.tienda.core.util

import java.math.BigDecimal
import java.text.DecimalFormat

/** Convierte texto de dinero del backend a BigDecimal (nunca float/double). */
internal fun String?.toMoney(): BigDecimal =
    this?.trim()?.toBigDecimalOrNull() ?: BigDecimal.ZERO

// DecimalFormat NO es thread-safe: cada hilo recibe su propia instancia vía
// ThreadLocal. Así aMoneda() es seguro aunque se llame desde coroutines (no solo
// desde composables en el main thread) sin corrupción ni crashes intermitentes.
private val MoneyFormat = ThreadLocal.withInitial { DecimalFormat("$#,##0.00") }

/** Formatea un monto para mostrar (ej. "$1,234.50"). */
internal fun BigDecimal.aMoneda(): String = MoneyFormat.get().format(this)
