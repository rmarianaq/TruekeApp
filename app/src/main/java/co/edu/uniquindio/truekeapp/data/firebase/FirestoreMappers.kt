package co.edu.uniquindio.truekeapp.data.firebase

import co.edu.uniquindio.truekeapp.domain.model.Category
import co.edu.uniquindio.truekeapp.domain.model.Comment
import co.edu.uniquindio.truekeapp.domain.model.ExchangeProposal
import co.edu.uniquindio.truekeapp.domain.model.ItemCondition
import co.edu.uniquindio.truekeapp.domain.model.Location
import co.edu.uniquindio.truekeapp.domain.model.Notification
import co.edu.uniquindio.truekeapp.domain.model.NotificationType
import co.edu.uniquindio.truekeapp.domain.model.ProposalStatus
import co.edu.uniquindio.truekeapp.domain.model.Publication
import co.edu.uniquindio.truekeapp.domain.model.PublicationStatus
import co.edu.uniquindio.truekeapp.domain.model.User
import co.edu.uniquindio.truekeapp.domain.model.UserRole
import com.google.firebase.firestore.DocumentSnapshot

// Conversión entre los modelos del dominio y los documentos de Firestore.
// Se usan mapas en vez de toObject() para no tocar los modelos del dominio
// (no tienen constructor vacío) y para que el APK release con R8 no rompa nada.
// Los enums se guardan por nombre ("BOOKS", "PENDING"...).

private inline fun <reified T : Enum<T>> enumOf(name: String?, default: T): T =
    runCatching { enumValueOf<T>(name ?: "") }.getOrDefault(default)

@Suppress("UNCHECKED_CAST")
private fun DocumentSnapshot.stringList(field: String): List<String> =
    (get(field) as? List<String>) ?: emptyList()

private fun Location.toMap() = mapOf("latitude" to latitude, "longitude" to longitude)

@Suppress("UNCHECKED_CAST")
private fun DocumentSnapshot.location(field: String): Location? {
    val map = get(field) as? Map<String, Any?> ?: return null
    val lat = (map["latitude"] as? Number)?.toDouble() ?: return null
    val lng = (map["longitude"] as? Number)?.toDouble() ?: return null
    return Location(lat, lng)
}

private fun DocumentSnapshot.long(field: String, default: Long = 0L) =
    getLong(field) ?: default

// ─── Usuario ─── (la contraseña NO se guarda: solo la conoce Firebase Authentication)
fun User.toMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "email" to email.trim().lowercase(),
    "role" to role.name,
    "address" to address,
    "location" to location?.toMap(),
    "points" to points,
    "photoUrl" to photoUrl,
    "bio" to bio,
)

fun DocumentSnapshot.toUser(): User? {
    if (!exists()) return null
    return User(
        id = id,
        name = getString("name") ?: "",
        email = getString("email") ?: "",
        password = "",
        role = enumOf(getString("role"), UserRole.USER),
        address = getString("address") ?: "",
        location = location("location"),
        points = long("points").toInt(),
        photoUrl = getString("photoUrl"),
        bio = getString("bio") ?: "",
    )
}

// ─── Publicación ───
fun Publication.toMap(): Map<String, Any?> = mapOf(
    "title" to title,
    "description" to description,
    "category" to category.name,
    "condition" to condition.name,
    "wantedItems" to wantedItems,
    "imageUrls" to imageUrls,
    "location" to location.toMap(),
    "address" to address,
    "ownerId" to ownerId,
    "status" to status.name,
    "rejectionReason" to rejectionReason,
    "interestedUserIds" to interestedUserIds,
    "reportedByUserIds" to reportedByUserIds,
    "createdAt" to createdAt,
)

fun DocumentSnapshot.toPublication(): Publication? {
    if (!exists()) return null
    return Publication(
        id = id,
        title = getString("title") ?: "",
        description = getString("description") ?: "",
        category = enumOf(getString("category"), Category.OTHER),
        condition = enumOf(getString("condition"), ItemCondition.GOOD),
        wantedItems = stringList("wantedItems"),
        imageUrls = stringList("imageUrls"),
        location = location("location") ?: Location(4.5339, -75.6811),
        address = getString("address") ?: "",
        ownerId = getString("ownerId") ?: "",
        status = enumOf(getString("status"), PublicationStatus.PENDING),
        rejectionReason = getString("rejectionReason"),
        interestedUserIds = stringList("interestedUserIds"),
        reportedByUserIds = stringList("reportedByUserIds"),
        createdAt = long("createdAt"),
    )
}

// ─── Comentario ───
fun Comment.toMap(): Map<String, Any?> = mapOf(
    "publicationId" to publicationId,
    "authorId" to authorId,
    "text" to text,
    "createdAt" to createdAt,
)

fun DocumentSnapshot.toComment(): Comment? {
    if (!exists()) return null
    return Comment(
        id = id,
        publicationId = getString("publicationId") ?: "",
        authorId = getString("authorId") ?: "",
        text = getString("text") ?: "",
        createdAt = long("createdAt"),
    )
}

// ─── Notificación ───
fun Notification.toMap(): Map<String, Any?> = mapOf(
    "userId" to userId,
    "type" to type.name,
    "publicationId" to publicationId,
    "publicationTitle" to publicationTitle,
    "actorName" to actorName,
    "isRead" to isRead,
    "createdAt" to createdAt,
)

fun DocumentSnapshot.toNotification(): Notification? {
    if (!exists()) return null
    return Notification(
        id = id,
        userId = getString("userId") ?: "",
        type = enumOf(getString("type"), NotificationType.COMMENT),
        publicationId = getString("publicationId") ?: "",
        publicationTitle = getString("publicationTitle") ?: "",
        actorName = getString("actorName") ?: "",
        isRead = getBoolean("isRead") ?: false,
        createdAt = long("createdAt"),
    )
}

// ─── Propuesta de intercambio ───
fun ExchangeProposal.toMap(): Map<String, Any?> = mapOf(
    "publicationId" to publicationId,
    "fromUserId" to fromUserId,
    "toUserId" to toUserId,
    "offeredItem" to offeredItem,
    "status" to status.name,
    "isChain" to isChain,
    "createdAt" to createdAt,
)

fun DocumentSnapshot.toProposal(): ExchangeProposal? {
    if (!exists()) return null
    return ExchangeProposal(
        id = id,
        publicationId = getString("publicationId") ?: "",
        fromUserId = getString("fromUserId") ?: "",
        toUserId = getString("toUserId") ?: "",
        offeredItem = getString("offeredItem") ?: "",
        status = enumOf(getString("status"), ProposalStatus.PENDING),
        isChain = getBoolean("isChain") ?: false,
        createdAt = long("createdAt"),
    )
}
