package co.edu.uniquindio.truekeapp.domain.model

data class Publication(
    val id: String,
    val title: String,
    val description: String,
    val category: Category,
    val condition: ItemCondition,
    val wantedItems: List<String>,          // lo que el usuario quiere a cambio
    val imageUrls: List<String>,            // la guía exige al menos una imagen
    val location: Location,
    val address: String,
    val ownerId: String,                    // id del User que publicó
    val status: PublicationStatus = PublicationStatus.PENDING,
    val rejectionReason: String? = null,    // solo si fue rechazada
    val interestedUserIds: List<String> = emptyList(),  // los "Me interesa"
    val createdAt: Long = System.currentTimeMillis(),
) {
    val interestCount: Int get() = interestedUserIds.size
}