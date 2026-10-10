package co.edu.uniquindio.truekeapp.domain.model

// El ciclo de vida de una publicación, según el flujo de moderación
enum class PublicationStatus {
    PENDING,    // recién creada, esperando al moderador
    VERIFIED,   // aprobada: visible en el feed
    REJECTED,   // rechazada por el moderador (con motivo)
    FINISHED,   // el trueque ya se hizo
}