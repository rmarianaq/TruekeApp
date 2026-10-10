package co.edu.uniquindio.truekeapp.domain.model

// Los niveles de la temática 6, con los puntos mínimos para alcanzar cada uno
enum class UserLevel(val minPoints: Int) {
    NOVATO(0),
    INTERCAMBIADOR(300),
    NEGOCIANTE(1000),
    MAESTRO_DEL_TRUEQUE(2500);

    companion object {
        // Devuelve el nivel que corresponde a cierta cantidad de puntos
        fun fromPoints(points: Int): UserLevel =
            entries.last { points >= it.minPoints }
    }
}