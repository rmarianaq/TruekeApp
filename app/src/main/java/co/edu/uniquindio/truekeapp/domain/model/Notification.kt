package co.edu.uniquindio.truekeapp.domain.model

data class Notification(
    val id: String,
    val userId: String,          // a quién le llega
    val publicationId: String,   // sobre qué publicación
    val message: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)