package co.edu.uniquindio.truekeapp.data.repository.memory

import co.edu.uniquindio.truekeapp.domain.model.ExchangeProposal
import co.edu.uniquindio.truekeapp.domain.model.ProposalStatus
import co.edu.uniquindio.truekeapp.domain.repository.ProposalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProposalRepositoryImpl @Inject constructor() : ProposalRepository {

    private val _proposals = MutableStateFlow(seedProposals())
    override val proposals: StateFlow<List<ExchangeProposal>> = _proposals.asStateFlow()

    override suspend fun save(proposal: ExchangeProposal) {
        _proposals.update { list ->
            if (list.any { it.id == proposal.id }) {
                list.map { if (it.id == proposal.id) proposal else it }
            } else {
                listOf(proposal) + list
            }
        }
    }

    override suspend fun updateStatus(id: String, status: ProposalStatus) {
        _proposals.update { list -> list.map { if (it.id == id) it.copy(status = status) else it } }
    }

    private fun seedProposals(): List<ExchangeProposal> {
        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        return listOf(
            // Recibidas por Mariana (u1)
            ExchangeProposal("pr1", "p3", "u3", "u1", "Parlante bluetooth", ProposalStatus.PENDING, createdAt = now - hour / 12),
            ExchangeProposal("pr2", "p3", "u2", "u1", "Teclado mecánico", ProposalStatus.REJECTED, createdAt = now - 24 * hour),
            ExchangeProposal("pr3", "p9", "u2", "u1", "Juego de sartenes", ProposalStatus.ACCEPTED, createdAt = now - 110 * hour),
            ExchangeProposal("pr4", "p9", "u3", "u1", "Plantas de interior", ProposalStatus.REJECTED, createdAt = now - 112 * hour),
            // Enviadas por Mariana (u1)
            ExchangeProposal("pr5", "p1", "u1", "u2", "Teclado musical", ProposalStatus.PENDING, createdAt = now - hour),
            ExchangeProposal("pr6", "p4", "u1", "u3", "Bolso de cuero", ProposalStatus.REJECTED, createdAt = now - 48 * hour),
        )
    }
}
