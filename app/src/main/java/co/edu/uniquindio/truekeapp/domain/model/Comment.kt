package co.edu.uniquindio.truekeapp.domain.model

data class Comment(
    val id: String,
    val publicationId: String,
    val authorId: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
)