package co.edu.uniquindio.truekeapp.data.repository.memory

import co.edu.uniquindio.truekeapp.domain.model.Comment
import co.edu.uniquindio.truekeapp.domain.repository.CommentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor() : CommentRepository {

    private val _comments = MutableStateFlow(seedComments())
    override val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    override suspend fun add(comment: Comment) {
        _comments.update { it + comment }
    }

    private fun seedComments(): List<Comment> {
        val now = System.currentTimeMillis()
        val minute = 60_000L
        return listOf(
            Comment("c1", "p1", "u3", "¿La bicicleta tiene guayas nuevas también o solo las pastillas de freno?", now - 120 * minute),
            Comment("c2", "p1", "u2", "Guayas y pastillas nuevas, todo el sistema de frenos revisado hace un mes.", now - 60 * minute),
            Comment("c3", "p1", "u1", "¿Aceptarías un teclado musical en buen estado?", now - 40 * minute),
            Comment("c4", "p3", "u2", "¿Todavía tienen buena batería?", now - 180 * minute),
        )
    }
}
