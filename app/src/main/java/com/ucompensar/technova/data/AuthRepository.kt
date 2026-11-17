package com.ucompensar.technova.data

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.FirebaseNetworkException
import kotlinx.coroutines.tasks.await

/** Aquí está todo lo del inicio de sesión y el registro con Firebase */
object AuthRepository {

    /** Error de inicio de sesión con un mensaje en español */
    class AuthError(message: String) : Exception(message)

    /** Esta función crea la cuenta del usuario y guarda su perfil */
    suspend fun register(name: String, email: String, phone: String, password: String, role: Role): User {
        val result = runAuth { Db.auth.createUserWithEmailAndPassword(email, password).await() }
        val uid = result.user!!.uid
        val user = User(id = uid, name = name, email = email, phone = phone, role = role, active = true)
        Db.store.collection(Db.USERS).document(uid).set(Db.fromUser(user) + ("createdAt" to System.currentTimeMillis())).await()
        Session.current = user
        return user
    }

    /** Inicia sesión con correo y contraseña */
    suspend fun login(email: String, password: String): User {
        runAuth { Db.auth.signInWithEmailAndPassword(email, password).await() }
        return loadProfile()
    }

    /** Inicia sesión con Google y si es nuevo lo crea como comprador */
    suspend fun loginWithGoogle(idToken: String): User {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = runAuth { Db.auth.signInWithCredential(credential).await() }
        val firebaseUser = result.user!!
        val doc = Db.store.collection(Db.USERS).document(firebaseUser.uid).get().await()
        if (!doc.exists()) {
            val user = User(
                id = firebaseUser.uid, name = firebaseUser.displayName.orEmpty(),
                email = firebaseUser.email.orEmpty(), role = Role.BUYER
            )
            Db.store.collection(Db.USERS).document(user.id).set(Db.fromUser(user) + ("createdAt" to System.currentTimeMillis())).await()
        }
        return loadProfile()
    }

    /** Trae el perfil del usuario y revisa que la cuenta siga activa */
    suspend fun loadProfile(): User {
        val uid = Db.uid ?: throw AuthError("No hay una sesión activa.")
        val doc = Db.store.collection(Db.USERS).document(uid).get().await()
        if (!doc.exists()) {
            logout()
            throw AuthError("Esta cuenta fue eliminada. Comunícate con el administrador.")
        }
        val user = Db.toUser(doc)
        if (!user.active) {
            logout()
            throw AuthError("Tu cuenta está suspendida. Comunícate con el administrador.")
        }
        Session.current = user
        return user
    }

    /** Manda el correo para cambiar la contraseña */
    suspend fun sendPasswordReset(email: String) {
        runAuth { Db.auth.sendPasswordResetEmail(email).await() }
    }

    /** Cierra la sesión */
    fun logout() {
        Db.auth.signOut()
        Session.current = null
        CartRepository.clearLocal()
    }

    /** Borra la cuenta del usuario con todos sus datos */
    suspend fun deleteAccount() {
        val firebaseUser = Db.auth.currentUser ?: return
        val userRef = Db.store.collection(Db.USERS).document(firebaseUser.uid)
        val cart = userRef.collection(Db.CART).get().await()
        cart.documents.forEach { it.reference.delete().await() }
        userRef.delete().await()
        try {
            firebaseUser.delete().await()
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            throw AuthError("Por seguridad, cierra sesión, vuelve a entrar e inténtalo de nuevo.")
        }
        logout()
    }

    /** Crea una cuenta desde el panel del administrador sin cerrar su sesión */
    suspend fun createAccountAsAdmin(email: String, password: String): String {
        val main = FirebaseApp.getInstance()
        val helper = FirebaseApp.getApps(main.applicationContext).find { it.name == HELPER_APP }
            ?: FirebaseApp.initializeApp(main.applicationContext, main.options, HELPER_APP)
        val helperAuth = FirebaseAuth.getInstance(helper)
        val result = runAuth { helperAuth.createUserWithEmailAndPassword(email, password).await() }
        helperAuth.signOut()
        return result.user!!.uid
    }

    /** Hace la tarea y si falla cambia el error por un mensaje en español */
    private suspend fun <T> runAuth(block: suspend () -> T): T = try {
        block()
    } catch (e: FirebaseAuthUserCollisionException) {
        throw AuthError("Ya existe una cuenta con ese correo.")
    } catch (e: FirebaseAuthWeakPasswordException) {
        throw AuthError("La contraseña es muy débil. Usa al menos 6 caracteres.")
    } catch (e: FirebaseAuthInvalidUserException) {
        throw AuthError("No existe una cuenta con ese correo o fue deshabilitada.")
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        throw AuthError("Correo o contraseña incorrectos.")
    } catch (e: FirebaseNetworkException) {
        throw AuthError("Sin conexión a internet. Revisa tu red e inténtalo de nuevo.")
    }

    private const val HELPER_APP = "admin-helper"
}
