# Reglas de R8 para el APK release.
# R8 movía partes de las clases "fachada" de Kotlin (p. ej. StringsKt__StringNumberConversionsJVMKt)
# fuera de su paquete, pero heredan de otras partes que son privadas del paquete
# -> IllegalAccessError al escribir en un campo de texto. Se conservan sus nombres y paquete.
-keep,allowshrinking,allowoptimization class kotlin.**Kt__* { *; }
