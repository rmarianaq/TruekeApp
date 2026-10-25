package co.edu.uniquindio.truekeapp.domain.service

import co.edu.uniquindio.truekeapp.domain.model.ExchangeProposal
import co.edu.uniquindio.truekeapp.domain.model.ProposalStatus
import co.edu.uniquindio.truekeapp.domain.model.Publication
import co.edu.uniquindio.truekeapp.domain.model.PublicationStatus
import kotlin.math.roundToInt

// Funcionalidad innovadora 2: Score de confianza.
// Se calcula con el COMPORTAMIENTO del usuario en la app, no con calificaciones manuales.
data class TrustScore(
    val responsiveness: Int,   // % de propuestas recibidas que respondió
    val completion: Int,       // % de trueques aceptados que terminó
    val reputation: Int,       // 100 menos 20 por cada reporte recibido
) {
    val total: Int
        get() = (responsiveness * 0.35 + completion * 0.35 + reputation * 0.30).roundToInt()
}

object TrustScoreCalculator {

    fun calculate(
        userId: String,
        publications: List<Publication>,
        proposals: List<ExchangeProposal>,
    ): TrustScore {
        val received = proposals.filter { it.toUserId == userId }
        val responsiveness = if (received.isEmpty()) {
            100
        } else {
            received.count { it.status != ProposalStatus.PENDING } * 100 / received.size
        }

        val accepted = proposals.filter {
            it.status == ProposalStatus.ACCEPTED && (it.fromUserId == userId || it.toUserId == userId)
        }
        val finishedIds = publications
            .filter { it.status == PublicationStatus.FINISHED }
            .map { it.id }
            .toSet()
        val completion = if (accepted.isEmpty()) {
            100
        } else {
            accepted.count { it.publicationId in finishedIds } * 100 / accepted.size
        }

        val reports = publications.filter { it.ownerId == userId }.sumOf { it.reportedByUserIds.size }
        val reputation = (100 - reports * 20).coerceAtLeast(0)

        return TrustScore(responsiveness, completion, reputation)
    }
}
