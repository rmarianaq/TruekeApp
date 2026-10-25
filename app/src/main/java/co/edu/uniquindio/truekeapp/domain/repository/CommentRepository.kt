package co.edu.uniquindio.truekeapp.domain.repository

import co.edu.uniquindio.truekeapp.domain.model.Comment
import kotlinx.coroutines.flow.StateFlow

interface CommentRepository {
    val comments: StateFlow<List<Comment>>
    suspend fun add(comment: Comment)
}
