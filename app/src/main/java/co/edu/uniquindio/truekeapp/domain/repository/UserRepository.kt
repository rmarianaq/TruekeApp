package co.edu.uniquindio.truekeapp.domain.repository

import co.edu.uniquindio.truekeapp.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {

    // Todos los usuarios (para mostrar nombres de autores, dueños, etc.)
    val users: StateFlow<List<User>>

    // El usuario con sesión iniciada (null = nadie ha iniciado sesión)
    val currentUser: StateFlow<User?>

    // true cuando ya se revisó si había una sesión guardada (para elegir la pantalla inicial)
    val sessionRestored: StateFlow<Boolean>

    suspend fun login(email: String, password: String): User?
    suspend fun logout()
    suspend fun save(user: User)
    suspend fun delete(id: String)
    suspend fun findById(id: String): User?
    suspend fun findByEmail(email: String): User?

    suspend fun sendPasswordResetEmail(email: String)
}
