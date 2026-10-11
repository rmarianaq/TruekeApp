package co.edu.uniquindio.truekeapp.data.repository.memory

import co.edu.uniquindio.truekeapp.domain.model.Location
import co.edu.uniquindio.truekeapp.domain.model.User
import co.edu.uniquindio.truekeapp.domain.model.UserRole
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor() : UserRepository {

    // Usuarios de prueba. El moderador va precargado, como exige la guía.
    private val users = MutableStateFlow(
        listOf(
            User(
                id = "u1",
                name = "Mariana R.",
                email = "mariana@trueke.co",
                password = "Trueke123",
                address = "Cra 14 #21-30, Armenia",
                location = Location(4.5339, -75.6811),
                points = 620,
            ),
            User(
                id = "u2",
                name = "Carlos Gómez",
                email = "carlos@trueke.co",
                password = "Trueke123",
                address = "Calle 50 #18-12, Armenia",
                location = Location(4.5446, -75.6634),
                points = 1250,
            ),
            User(
                id = "u3",
                name = "Laura Pérez",
                email = "laura@trueke.co",
                password = "Trueke123",
                address = "Av. Bolívar #3-40, Armenia",
                location = Location(4.5240, -75.6920),
                points = 180,
            ),
            User(
                id = "m1",
                name = "Moderador Trueke",
                email = "moderador@trueke.co",
                password = "Admin123",
                role = UserRole.MODERATOR,
            ),
        )
    )

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    override suspend fun login(email: String, password: String): User? {
        val user = users.value.find {
            it.email.equals(email.trim(), ignoreCase = true) && it.password == password
        }
        _currentUser.value = user
        return user
    }

    override suspend fun logout() {
        _currentUser.value = null
    }

    override suspend fun save(user: User) {
        users.update { list ->
            if (list.any { it.id == user.id }) {
                list.map { if (it.id == user.id) user else it }  // ya existe: lo reemplaza
            } else {
                list + user                                       // es nuevo: lo agrega
            }
        }
    }

    override suspend fun findById(id: String): User? =
        users.value.find { it.id == id }

    override suspend fun findByEmail(email: String): User? =
        users.value.find { it.email.equals(email.trim(), ignoreCase = true) }

    override suspend fun sendPasswordResetEmail(email: String) {
        // En memoria no hay servidor de correo.
        // En la Fase 3 esta función llamará a Firebase, que envía el enlace de verdad.
    }
}