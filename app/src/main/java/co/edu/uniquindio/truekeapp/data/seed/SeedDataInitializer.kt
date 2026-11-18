package co.edu.uniquindio.truekeapp.data.seed

import android.content.Context
import android.util.Log
import co.edu.uniquindio.truekeapp.data.firebase.toMap
import co.edu.uniquindio.truekeapp.data.repository.memory.CommentRepositoryImpl
import co.edu.uniquindio.truekeapp.data.repository.memory.NotificationRepositoryImpl
import co.edu.uniquindio.truekeapp.data.repository.memory.ProposalRepositoryImpl
import co.edu.uniquindio.truekeapp.data.repository.memory.PublicationRepositoryImpl
import co.edu.uniquindio.truekeapp.data.repository.memory.UserRepositoryImpl
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// Carga UNA sola vez los datos de prueba de la Fase 2 (usuarios, publicaciones,
// comentarios, propuestas y notificaciones) en Firebase, para que la app no arranque vacía
// y se puedan mostrar el match en cadena, las sugerencias y el score de confianza.
// Las cuentas se crean con una instancia secundaria de Firebase, así no se toca
// la sesión del usuario que está usando la app.
@Singleton
class SeedDataInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
) {

    suspend fun seedIfNeeded() {
        val marker = firestore.collection("meta").document("seed")
        if (marker.get().await().exists()) return

        val seederApp = FirebaseApp.getApps(context).find { it.name == SEEDER }
            ?: FirebaseApp.initializeApp(context, FirebaseApp.getInstance().options, SEEDER)
        val seederAuth = FirebaseAuth.getInstance(seederApp)

        // 1. Cuentas de Firebase Authentication + perfiles. Los ids "u1", "m1"... pasan a ser UIDs
        val ids = mutableMapOf<String, String>()
        for (user in UserRepositoryImpl().users.value) {
            val uid = try {
                seederAuth.createUserWithEmailAndPassword(user.email, user.password).await().user?.uid
            } catch (e: FirebaseAuthUserCollisionException) {
                seederAuth.signInWithEmailAndPassword(user.email, user.password).await().user?.uid
            } ?: continue
            seederAuth.signOut()
            ids[user.id] = uid
            firestore.collection("users").document(uid).set(user.copy(id = uid).toMap()).await()
        }
        fun uidOf(id: String) = ids[id] ?: id

        // 2. El resto de datos, con los ids de usuario traducidos
        val batch = firestore.batch()
        PublicationRepositoryImpl().publications.value.forEach { p ->
            val fixed = p.copy(
                ownerId = uidOf(p.ownerId),
                interestedUserIds = p.interestedUserIds.map(::uidOf),
                reportedByUserIds = p.reportedByUserIds.map(::uidOf),
            )
            batch.set(firestore.collection("publications").document(p.id), fixed.toMap())
        }
        CommentRepositoryImpl().comments.value.forEach { c ->
            batch.set(firestore.collection("comments").document(c.id), c.copy(authorId = uidOf(c.authorId)).toMap())
        }
        NotificationRepositoryImpl().notifications.value.forEach { n ->
            batch.set(firestore.collection("notifications").document(n.id), n.copy(userId = uidOf(n.userId)).toMap())
        }
        ProposalRepositoryImpl().proposals.value.forEach { pr ->
            val fixed = pr.copy(fromUserId = uidOf(pr.fromUserId), toUserId = uidOf(pr.toUserId))
            batch.set(firestore.collection("proposals").document(pr.id), fixed.toMap())
        }
        batch.set(marker, mapOf("done" to true, "at" to System.currentTimeMillis()))
        batch.commit().await()
        Log.i("SeedData", "Datos de prueba cargados en Firebase")
    }

    private companion object {
        const val SEEDER = "seeder"
    }
}
