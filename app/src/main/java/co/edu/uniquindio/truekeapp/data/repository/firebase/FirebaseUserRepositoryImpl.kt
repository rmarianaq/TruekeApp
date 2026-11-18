package co.edu.uniquindio.truekeapp.data.repository.firebase

import android.util.Log
import co.edu.uniquindio.truekeapp.data.firebase.toMap
import co.edu.uniquindio.truekeapp.data.firebase.toUser
import co.edu.uniquindio.truekeapp.domain.model.User
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseUserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : UserRepository {

    private val collection = firestore.collection("users")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _users = MutableStateFlow<List<User>>(emptyList())
    override val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _sessionRestored = MutableStateFlow(false)
    override val sessionRestored: StateFlow<Boolean> = _sessionRestored.asStateFlow()

    init {
        // Escucha la colección: cualquier cambio (puntos, perfil...) llega solo a las pantallas
        collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error escuchando usuarios", error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { it.toUser() } ?: return@addSnapshotListener
            _users.value = list
            // Si cambió el usuario con sesión iniciada, la sesión también se actualiza
            _currentUser.value?.let { current ->
                list.find { it.id == current.id }?.let { _currentUser.value = it }
            }
        }

        // Sesión persistente: Firebase recuerda al usuario aunque se cierre la app
        scope.launch {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                _currentUser.value = runCatching { findById(uid) }.getOrNull()
                if (_currentUser.value == null) auth.signOut()
            }
            _sessionRestored.value = true
        }
    }

    override suspend fun login(email: String, password: String): User? {
        // Autenticar al usuario con Firebase Authentication
        val response = try {
            auth.signInWithEmailAndPassword(email.trim(), password).await()
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            return null // Email o contraseña incorrectos
        } catch (e: FirebaseAuthInvalidUserException) {
            return null // La cuenta no existe o fue deshabilitada
        }
        val uid = response.user?.uid ?: return null
        // Recuperar el perfil desde Firestore. Si no tiene perfil, se cierra la sesión
        val user = findById(uid) ?: run {
            auth.signOut()
            null
        }
        _currentUser.value = user
        return user
    }

    override suspend fun logout() {
        auth.signOut()
        _currentUser.value = null
    }

    override suspend fun save(user: User) {
        val exists = collection.document(user.id).get().await().exists()
        if (exists || user.password.isBlank()) {
            // Actualización de un perfil que ya existe (datos, puntos, etc.)
            collection.document(user.id).set(user.toMap()).await()
            return
        }

        // Registro: Firebase Authentication crea la cuenta y rechaza correos repetidos
        val result = try {
            auth.createUserWithEmailAndPassword(user.email.trim(), user.password).await()
        } catch (e: FirebaseAuthUserCollisionException) {
            Log.w(TAG, "El correo ya está registrado", e)
            return
        }
        val newUser = result.user ?: throw Exception("Error al obtener el UID del usuario creado")
        try {
            // El documento usa el UID de Firebase Authentication como id
            collection.document(newUser.uid).set(user.copy(id = newUser.uid).toMap()).await()
        } catch (e: Exception) {
            // Si no se pudo guardar el perfil, se borra la cuenta para no dejarla incompleta
            withContext(NonCancellable) { runCatching { newUser.delete().await() } }
            throw e
        }
        // Firebase inicia sesión al registrar; la cerramos para que entre desde el login
        auth.signOut()
    }

    override suspend fun delete(id: String) {
        collection.document(id).delete().await()
        val firebaseUser = auth.currentUser
        if (firebaseUser?.uid == id) {
            runCatching { firebaseUser.delete().await() }
            auth.signOut()
        }
        if (_currentUser.value?.id == id) _currentUser.value = null
    }

    override suspend fun findById(id: String): User? =
        _users.value.find { it.id == id } ?: collection.document(id).get().await().toUser()

    override suspend fun findByEmail(email: String): User? {
        val clean = email.trim().lowercase()
        _users.value.find { it.email.equals(clean, ignoreCase = true) }?.let { return it }
        return collection.whereEqualTo("email", clean).limit(1).get().await()
            .documents.firstOrNull()?.toUser()
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        // Firebase envía el correo con el enlace para crear una nueva contraseña.
        // Si el correo no existe no se le dice al usuario (para no revelar cuentas).
        runCatching { auth.sendPasswordResetEmail(email.trim()).await() }
            .onFailure { Log.w(TAG, "No se pudo enviar el correo de recuperación", it) }
    }

    private companion object {
        const val TAG = "UserRepository"
    }
}
