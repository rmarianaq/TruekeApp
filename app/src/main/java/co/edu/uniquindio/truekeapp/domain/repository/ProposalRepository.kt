package co.edu.uniquindio.truekeapp.domain.repository

import co.edu.uniquindio.truekeapp.domain.model.ExchangeProposal
import co.edu.uniquindio.truekeapp.domain.model.ProposalStatus
import kotlinx.coroutines.flow.StateFlow

interface ProposalRepository {
    val proposals: StateFlow<List<ExchangeProposal>>
    suspend fun save(proposal: ExchangeProposal)
    suspend fun updateStatus(id: String, status: ProposalStatus)
}
