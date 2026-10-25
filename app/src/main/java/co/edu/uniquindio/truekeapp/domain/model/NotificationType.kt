package co.edu.uniquindio.truekeapp.domain.model

enum class NotificationType {
    COMMENT,                // alguien comentó tu publicación
    PROPOSAL_RECEIVED,      // te propusieron un trueque
    PROPOSAL_ACCEPTED,      // aceptaron tu propuesta
    PROPOSAL_REJECTED,      // rechazaron tu propuesta
    PUBLICATION_VERIFIED,   // un moderador verificó tu publicación
    PUBLICATION_REJECTED,   // un moderador rechazó tu publicación
}
