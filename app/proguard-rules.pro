# ============================================================================
# Tienda — reglas de R8 para el build de RELEASE (minify + shrink de recursos).
#
# NOTA: hoy R8 está APAGADO (optimization { enable = false } en build.gradle.kts).
# Este archivo queda listo para cuando se active: al poner enable = true, estas
# reglas evitan que R8 rompa la (de)serialización JSON contra el backend.
#
# La mayoría del stack (Ktor, OkHttp, Okio, kotlinx-coroutines) publica sus
# PROPIAS reglas "consumer" que R8 aplica solo. Aquí van las que dependen de
# NUESTRO código (paquete com.example.tienda) o que conviene reforzar.
# ============================================================================

# --- Atributos para reflexión, serialización y stack traces legibles --------
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault
-keepattributes Signature, InnerClasses, EnclosingMethod, Exceptions
# SourceFile + LineNumberTable: stack traces desofuscables con el mapping.txt
# que R8 genera en build/outputs/mapping/release/. Guárdalo con cada release.
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================================================
# kotlinx.serialization
# Mantiene los serializadores generados y el Companion de cada @Serializable.
# (Reglas canónicas del repo oficial de kotlinx.serialization.)
# ============================================================================
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Red de seguridad para NUESTROS DTOs: conserva intactas las clases
# @Serializable de la app (evita renombrado de campos que rompa el mapeo JSON
# con el backend Ktor). El costo en tamaño es mínimo (son data classes chicas).
-keep @kotlinx.serialization.Serializable class com.example.tienda.** { *; }

# ============================================================================
# Enums de la app (usados en serialización). El default optimize.txt ya
# preserva values()/valueOf(), pero se refuerza para nuestro paquete
# (p.ej. UserRole, TipoMovimiento, EstadoVenta, ModoTema).
# ============================================================================
-keepclassmembers enum com.example.tienda.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ============================================================================
# Refuerzos defensivos: clases opcionales que el stack (OkHttp/Ktor) referencia
# por nombre y que pueden no existir en el classpath (evita "missing class").
# ============================================================================
-dontwarn org.slf4j.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
