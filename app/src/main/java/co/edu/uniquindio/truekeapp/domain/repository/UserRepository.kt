package co.edu.uniquindio.truekeapp.domain.repository

import co.edu.uniquindio.truekeapp.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {

    // El usuario con sesión iniciada (null = nadie ha iniciado sesión)
    val currentUser: StateFlow<User?>

    suspend fun login(email: String, password: String): User?
    suspend fun logout()
    suspend fun save(user: User)
    suspend fun findById(id: String): User?
    suspend fun findByEmail(email: String): User?
}