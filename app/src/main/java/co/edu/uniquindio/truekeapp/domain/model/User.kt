package co.edu.uniquindio.truekeapp.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val password: String,
    val role: UserRole = UserRole.USER,
    val address: String = "",
    val location: Location? = null,
    val points: Int = 0,
    val photoUrl: String? = null,
) {
    // Propiedad calculada: no se guarda, se deduce de los puntos
    val level: UserLevel get() = UserLevel.fromPoints(points)
}