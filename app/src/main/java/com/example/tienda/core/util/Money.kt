package com.example.tienda.core.util

import java.math.BigDecimal
import java.text.DecimalFormat

/** Convierte texto de dinero del backend a BigDecimal (nunca float/double). */
internal fun String?.toMoney(): BigDecimal =
    this?.trim()?.toBigDecimalOrNull() ?: BigDecimal.ZERO

private val MoneyFormat = DecimalFormat("$#,##0.00")

/** Formatea un monto para mostrar (ej. "$1,234.50"). */
internal fun BigDecimal.aMoneda(): String = MoneyFormat.format(this)
