package co.edu.uniquindio.truekeapp.domain.model

// Una propuesta de trueque: fromUser ofrece "offeredItem" a cambio de la publicación de toUser
data class ExchangeProposal(
    val id: String,
    val publicationId: String,
    val fromUserId: String,
    val toUserId: String,
    val offeredItem: String,
    val status: ProposalStatus = ProposalStatus.PENDING,
    val isChain: Boolean = false,    // true si nació de un match en cadena
    val createdAt: Long = System.currentTimeMillis(),
)
