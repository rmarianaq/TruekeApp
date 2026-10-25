package co.edu.uniquindio.truekeapp.domain.model

// Guardamos el TIPO y los datos, no el texto: así la pantalla arma el mensaje
// desde strings.xml y la notificación sale en el idioma del celular.
data class Notification(
    val id: String,
    val userId: String,              // a quién le llega
    val type: NotificationType,
    val publicationId: String,       // sobre qué publicación
    val publicationTitle: String,
    val actorName: String = "",      // quién generó la notificación
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
