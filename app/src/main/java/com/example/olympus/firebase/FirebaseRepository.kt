package com.example.olympus

// Repositorio centralizado para todas las operaciones con Firebase Firestore y Authentication
// Abstrae las interacciones con Firebase proporcionando una API limpia

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class FirebaseRepository private constructor() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val roleRequestsCollection = firestore.collection("roleRequests")
    private val serviceRequestsCollection = firestore.collection("serviceRequests")
    private val mailCollection = firestore.collection("mail")
    private val trainerAssignments = firestore.collection("trainerAssignments")
    private val nutritionistAssignments = firestore.collection("nutritionistAssignments")
    private val plansCollection = firestore.collection("nutritionPlans")
    private val routinesCollection = firestore.collection("routines")
    private val notesCollection = firestore.collection("notes")
    private val comprasCollection = firestore.collection("compras")

    companion object {
        val instance: FirebaseRepository by lazy { FirebaseRepository() }
    }

    // Devuelve el UID del usuario actual autenticado o null si no hay sesion
    fun getCurrentUserUid(): String? = auth.currentUser?.uid

    // Cierra la sesion del usuario en Firebase Authentication
    fun signOut() {
        auth.signOut()
    }

    // Envia un email de recuperacion de contrasena al email proporcionado
    fun sendPasswordResetEmail(
        email: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        auth.setLanguageCode("es")
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Actualiza la contrasena del usuario actual (requiere reautenticacion previa)
    fun updateCurrentUserPassword(
        currentPassword: String,
        newPassword: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        val user = auth.currentUser
        val email = user?.email

        if (user == null || email.isNullOrBlank()) {
            onResult(Result.failure(IllegalStateException("No hay un usuario autenticado.")))
            return
        }

        // Requiere reautenticacion antes de cambiar el password
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener { onResult(Result.success(Unit)) }
                    .addOnFailureListener { onResult(Result.failure(it)) }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Crea una solicitud de rol (Entrenador o Nutricionista) y envia notificacion por email
    fun submitRoleRequest(
        userUid: String?,
        fullName: String,
        email: String,
        phone: String,
        requestedRoles: List<String>,
        details: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        // Datos a guardar en Firestore
        val payload = hashMapOf(
            "userUid" to userUid,
            "fullName" to fullName,
            "email" to email,
            "phone" to phone,
            "requestedRoles" to requestedRoles,
            "details" to details,
            "status" to "Pendiente",
            "createdAt" to FieldValue.serverTimestamp()
        )

        roleRequestsCollection.add(payload)
            .addOnSuccessListener {
                // Enviar email de notificacion al administrador
                val requestedRole = requestedRoles.firstOrNull().orEmpty()
                val subject = "Nuevo usuario $requestedRole que quiere registrarse: $email"
                val body = "Nuevo usuario $requestedRole que quiere registrarse: $email"

                enqueueRoleRequestEmail(
                    subject = subject,
                    body = body,
                    replyTo = email
                ) { emailResult ->
                    emailResult
                        .onSuccess { onResult(Result.success(Unit)) }
                        .onFailure { onResult(Result.failure(it)) }
                }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Agrega un documento a la coleccion de mail para ser procesado por Cloud Function
    private fun enqueueRoleRequestEmail(
        subject: String,
        body: String,
        replyTo: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        // Estructura del payload para el servicio de email
        val payload = hashMapOf(
            "to" to listOf("tuyoloxd@gmail.com"),
            "replyTo" to replyTo,
            "message" to mapOf(
                "subject" to subject,
                "text" to body
            ),
            "metadata" to mapOf(
                "type" to "roleRequest"
            ),
            "createdAt" to FieldValue.serverTimestamp()
        )

        mailCollection.add(payload)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Permite a un usuario solicitar un servicio a un profesional (Entrenador o Nutricionista)
    // Si ya existe una solicitud pendiente, retorna false sin crear otra
    // Si ya existe una solicitud pendiente, retorna false sin crear otra
    fun submitServiceRequestToProfessional(
        userUid: String,
        userName: String,
        userEmail: String,
        professionalUid: String,
        professionalName: String,
        requestedRole: String,
        onResult: (Result<Boolean>) -> Unit
    ) {
        // Usar composite ID para evitar duplicados
        val documentId = "${professionalUid}_$userUid"
        val document = serviceRequestsCollection.document(documentId)

        document.get()
            .addOnSuccessListener { snapshot ->
                // Si ya existe una solicitud pendiente, no crear otra
                if (snapshot.exists() && snapshot.getString("status") == "Pendiente") {
                    onResult(Result.success(false))
                    return@addOnSuccessListener
                }

                val payload = mapOf(
                    "userUid" to userUid,
                    "userName" to userName,
                    "userEmail" to userEmail,
                    "professionalUid" to professionalUid,
                    "professionalName" to professionalName,
                    "requestedRole" to requestedRole,
                    "status" to "Pendiente",
                    "createdAt" to FieldValue.serverTimestamp()
                )

                document.set(payload)
                    .addOnSuccessListener { onResult(Result.success(true)) }
                    .addOnFailureListener { onResult(Result.failure(it)) }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Obtiene todas las solicitudes pendientes dirigidas a un profesional especifico
    fun getPendingServiceRequestsForProfessional(
        professionalUid: String,
        onResult: (Result<List<ServiceRequest>>) -> Unit
    ) {
        serviceRequestsCollection
            .whereEqualTo("professionalUid", professionalUid)
            .whereEqualTo("status", "Pendiente")
            .get()
            .addOnSuccessListener { query ->
                val requests = query.documents.mapNotNull { it.toServiceRequest() }
                onResult(Result.success(requests))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Acepta una solicitud de servicio: crea la asignacion profesional-cliente segun el rol solicitado
// Acepta una solicitud de servicio: crea la asignacion profesional-cliente segun el rol solicitado
    fun acceptServiceRequest(
        request: ServiceRequest,
        onResult: (Result<Unit>) -> Unit
    ) {
        // Seleccionar la funcion de asignacion segun el rol solicitado
        val assignAction = when (request.requestedRole) {
            "Entrenador" -> {
                { callback: (Result<Unit>) -> Unit ->
                    assignTrainerToClient(request.professionalUid, request.userUid, callback)
                }
            }
            "Nutricionista" -> {
                { callback: (Result<Unit>) -> Unit ->
                    assignNutritionistToClient(request.professionalUid, request.userUid, callback)
                }
            }
            else -> null
        }

        if (assignAction == null) {
            onResult(Result.failure(IllegalArgumentException("Rol de solicitud no valido.")))
            return
        }

        // Crear asignacion y luego actualizar estado de la solicitud
        assignAction { assignmentResult ->
            assignmentResult
                .onSuccess {
                    updateServiceRequestStatus(request.id, "Aceptada", onResult)
                }
                .onFailure { onResult(Result.failure(it)) }
        }
    }

    // Rechaza una solicitud de servicio cambiando su estado a "Rechazada"
    fun rejectServiceRequest(
        requestId: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        updateServiceRequestStatus(requestId, "Rechazada", onResult)
    }

    // Autentica usuario con email y password, luego obtiene su perfil de Firestore
    // Autentica usuario con email y password, luego obtiene su perfil de Firestore
    fun signIn(email: String, password: String, onResult: (Result<UserProfile>) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid.isNullOrBlank()) {
                    onResult(Result.failure(IllegalStateException("No se obtuvo el UID del usuario.")))
                    return@addOnSuccessListener
                }
                getUserProfile(uid, onResult)
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Crea un nuevo usuario en Firebase Authentication (sin perfil en Firestore todavia)
    // Crea un nuevo usuario en Firebase Authentication (sin perfil en Firestore todavia)
    fun registerAuthUser(
        email: String,
        password: String,
        onResult: (Result<String>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid.isNullOrBlank()) {
                    onResult(Result.failure(IllegalStateException("No se pudo crear el usuario en Firebase Auth.")))
                } else {
                    onResult(Result.success(uid))
                }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Guarda o actualiza el perfil de usuario en Firestore (despues de registerAuthUser)
    // Guarda o actualiza el perfil de usuario en Firestore (despues de registerAuthUser)
    fun saveUserProfile(profile: UserProfile, onResult: (Result<Unit>) -> Unit) {
        val data = hashMapOf(
            "legacyLocalId" to profile.legacyLocalId,
            "name" to profile.name,
            "email" to profile.email,
            "role" to profile.role,
            "createdAt" to FieldValue.serverTimestamp()
        )

        usersCollection.document(profile.uid)
            .set(data)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Obtiene el perfil de usuario desde Firestore por su UID
    fun getUserProfile(uid: String, onResult: (Result<UserProfile>) -> Unit) {
        usersCollection.document(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val profile = snapshot.toUserProfile()
                if (profile == null) {
                    onResult(Result.failure(IllegalStateException("No existe perfil en Firestore para este usuario.")))
                } else {
                    onResult(Result.success(profile))
                }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Obtiene la lista de clientes asignados a un entrenador especifico
    fun getAssignedClientsForTrainer(
        trainerUid: String,
        onResult: (Result<List<UserProfile>>) -> Unit
    ) {
        getAssignedClients(trainerAssignments, "trainerUid", trainerUid, onResult)
    }

    // Obtiene la lista de clientes asignados a un nutricionista especifico
    fun getAssignedClientsForNutritionist(
        nutritionistUid: String,
        onResult: (Result<List<UserProfile>>) -> Unit
    ) {
        getAssignedClients(nutritionistAssignments, "nutritionistUid", nutritionistUid, onResult)
    }

    // Obtiene todos los usuarios que tienen un rol especifico (Entrenador, Nutricionista, etc)
    // Obtiene todos los usuarios que tienen un rol especifico (Entrenador, Nutricionista, etc)
    fun getUsersByRole(
        role: String,
        onResult: (Result<List<UserProfile>>) -> Unit
    ) {
        usersCollection
            .whereEqualTo("role", role)
            .get()
            .addOnSuccessListener { query ->
                val users = query.documents.mapNotNull { it.toUserProfile() }
                onResult(Result.success(users))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Asocia un entrenador a un cliente creando un documento en trainerAssignments
    // Asocia un entrenador a un cliente creando un documento en trainerAssignments
    fun assignTrainerToClient(
        trainerUid: String,
        clientUid: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        val documentId = "${trainerUid}_$clientUid"
        trainerAssignments.document(documentId)
            .set(
                mapOf(
                    "trainerUid" to trainerUid,
                    "clientUid" to clientUid,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Asocia un nutricionista a un cliente creando un documento en nutritionistAssignments
    // Asocia un nutricionista a un cliente creando un documento en nutritionistAssignments
    fun assignNutritionistToClient(
        nutritionistUid: String,
        clientUid: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        val documentId = "${nutritionistUid}_$clientUid"
        nutritionistAssignments.document(documentId)
            .set(
                mapOf(
                    "nutritionistUid" to nutritionistUid,
                    "clientUid" to clientUid,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Crea un nuevo plan nutricional asignado a un usuario por un nutricionista
    fun createPlan(
        nombre: String,
        descripcion: String,
        userUid: String,
        nutritionistUid: String,
        onResult: (Result<String>) -> Unit
    ) {
        val doc = plansCollection.document()
        val payload = mapOf(
            "nombre" to nombre,
            "descripcion" to descripcion,
            "userUid" to userUid,
            "nutritionistUid" to nutritionistUid,
            "createdAt" to FieldValue.serverTimestamp()
        )

        doc.set(payload)
            .addOnSuccessListener { onResult(Result.success(doc.id)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Crea una nueva rutina vacia para un usuario
    // trainerUid puede ser null si la crea el propio usuario
    fun createRoutine(
        nombre: String,
        userUid: String,
        trainerUid: String?,
        createdByRole: String,
        diaSemana: String = "",
        onResult: (Result<String>) -> Unit
    ) {
        val doc = routinesCollection.document()
        val payload = mapOf(
            "nombre" to nombre,
            "userUid" to userUid,
            "trainerUid" to trainerUid,
            "createdByRole" to createdByRole,
            "archived" to false,
            "diaSemana" to diaSemana,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        doc.set(payload)
            .addOnSuccessListener { onResult(Result.success(doc.id)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Obtiene todas las rutinas activas (no archivadas) de un usuario
    fun getRoutinesByUser(
        userUid: String,
        onResult: (Result<List<CloudRoutine>>) -> Unit
    ) {
        routinesCollection
            .whereEqualTo("userUid", userUid)
            .whereEqualTo("archived", false)
            .get()
            .addOnSuccessListener { query ->
                val routines = query.documents.mapNotNull { it.toCloudRoutine() }
                onResult(Result.success(routines))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Obtiene una rutina especifica por su ID
    fun getRoutineById(
        routineId: String,
        onResult: (Result<CloudRoutine>) -> Unit
    ) {
        routinesCollection.document(routineId)
            .get()
            .addOnSuccessListener { snapshot ->
                val routine = snapshot.toCloudRoutine()
                if (routine == null) {
                    onResult(Result.failure(IllegalStateException("No existe la rutina indicada.")))
                } else {
                    onResult(Result.success(routine))
                }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Actualiza solo el nombre de una rutina existente
    // Actualiza solo el nombre de una rutina existente
    fun updateRoutineName(
        routineId: String,
        nuevoNombre: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        routinesCollection.document(routineId)
            .update(
                mapOf(
                    "nombre" to nuevoNombre,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Actualiza el nombre y dia de la semana de una rutina
    fun updateRoutineNameAndDay(
        routineId: String,
        nuevoNombre: String,
        diaSemana: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        routinesCollection.document(routineId)
            .update(
                mapOf(
                    "nombre" to nuevoNombre,
                    "diaSemana" to diaSemana,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Elimina una rutina y todas sus subcolecciones (exercises y sets) en un batch
    fun deleteRoutine(
        routineId: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        val exercisesCollection = routinesCollection.document(routineId).collection("exercises")
        exercisesCollection.get()
            .addOnSuccessListener { exercisesQuery ->
                // Obtener todos los sets de cada ejercicio en paralelo
                val setFetchTasks = exercisesQuery.documents.map { exerciseDoc ->
                    exerciseDoc.reference.collection("sets").get()
                }

                Tasks.whenAllSuccess<com.google.firebase.firestore.QuerySnapshot>(setFetchTasks)
                    .addOnSuccessListener { setQueries ->
                        // Eliminar todos los sets, ejercicios y la rutina en un solo batch
                        val batch = firestore.batch()
                        exercisesQuery.documents.forEachIndexed { index, exerciseDoc ->
                            val setQuery = setQueries[index]
                            setQuery.documents.forEach { setDoc -> batch.delete(setDoc.reference) }
                            batch.delete(exerciseDoc.reference)
                        }
                        batch.delete(routinesCollection.document(routineId))
                        batch.commit()
                            .addOnSuccessListener { onResult(Result.success(Unit)) }
                            .addOnFailureListener { onResult(Result.failure(it)) }
                    }
                    .addOnFailureListener { onResult(Result.failure(it)) }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Agrega un ejercicio a una rutina, asignando automaticamente el siguiente orden
    fun addExerciseToRoutine(
        routineId: String,
        exerciseId: Int,
        exerciseName: String,
        onResult: (Result<String>) -> Unit
    ) {
        val exercisesCollection = routinesCollection.document(routineId).collection("exercises")
        exercisesCollection
            .orderBy("orden", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { query ->
                // Calcular el siguiente orden basado en el maximo actual
                val nextOrder = (query.documents.firstOrNull()?.getLong("orden")?.toInt() ?: -1) + 1
                val doc = exercisesCollection.document()
                val payload = mapOf(
                    "exerciseId" to exerciseId,
                    "exerciseName" to exerciseName,
                    "orden" to nextOrder,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                doc.set(payload)
                    .addOnSuccessListener { onResult(Result.success(doc.id)) }
                    .addOnFailureListener { onResult(Result.failure(it)) }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Obtiene todos los ejercicios de una rutina ordenados por su campo "orden"
    fun getExercisesOfRoutine(
        routineId: String,
        onResult: (Result<List<CloudRoutineExercise>>) -> Unit
    ) {
        routinesCollection.document(routineId)
            .collection("exercises")
            .orderBy("orden")
            .get()
            .addOnSuccessListener { query ->
                val exercises = query.documents.mapNotNull { it.toCloudRoutineExercise(routineId) }
                onResult(Result.success(exercises))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Reemplaza el exerciseId y nombre de un ejercicio existente en la rutina
    fun replaceExerciseInRoutine(
        routineId: String,
        routineExerciseId: String,
        exerciseId: Int,
        exerciseName: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        routinesCollection.document(routineId)
            .collection("exercises")
            .document(routineExerciseId)
            .update(
                mapOf(
                    "exerciseId" to exerciseId,
                    "exerciseName" to exerciseName
                )
            )
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Mueve un ejercicio hacia arriba o abajo intercambiando el campo "orden" con otro ejercicio
    fun reorderExerciseInRoutine(
        routineId: String,
        routineExerciseId: String,
        moverArriba: Boolean,
        onResult: (Result<Unit>) -> Unit
    ) {
        val exercisesCollection = routinesCollection.document(routineId).collection("exercises")
        exercisesCollection.document(routineExerciseId)
            .get()
            .addOnSuccessListener { currentSnapshot ->
                val ordenActual = currentSnapshot.getLong("orden")?.toInt()
                if (!currentSnapshot.exists() || ordenActual == null) {
                    onResult(Result.failure(IllegalStateException("No se encontro el ejercicio indicado.")))
                    return@addOnSuccessListener
                }

                // Calcular el orden objetivo y verificar limites
                val ordenObjetivo = if (moverArriba) ordenActual - 1 else ordenActual + 1
                if (ordenObjetivo < 0) {
                    onResult(Result.success(Unit))
                    return@addOnSuccessListener
                }

                exercisesCollection
                    .whereEqualTo("orden", ordenObjetivo)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { targetQuery ->
                        val targetDoc = targetQuery.documents.firstOrNull()
                        if (targetDoc == null) {
                            onResult(Result.success(Unit))
                            return@addOnSuccessListener
                        }

                        // Intercambiar valores de orden entre los dos ejercicios
                        val batch = firestore.batch()
                        batch.update(currentSnapshot.reference, "orden", ordenObjetivo)
                        batch.update(targetDoc.reference, "orden", ordenActual)
                        batch.commit()
                            .addOnSuccessListener { onResult(Result.success(Unit)) }
                            .addOnFailureListener { onResult(Result.failure(it)) }
                    }
                    .addOnFailureListener { onResult(Result.failure(it)) }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Elimina un ejercicio de la rutina y todas sus series
    fun deleteExerciseFromRoutine(
        routineId: String,
        routineExerciseId: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        val exerciseRef = routinesCollection.document(routineId)
            .collection("exercises")
            .document(routineExerciseId)

        exerciseRef.collection("sets").get()
            .addOnSuccessListener { query ->
                val batch = firestore.batch()
                // Eliminar todas las series primero
                query.documents.forEach { batch.delete(it.reference) }
                // Luego eliminar el ejercicio
                batch.delete(exerciseRef)
                batch.commit()
                    .addOnSuccessListener { onResult(Result.success(Unit)) }
                    .addOnFailureListener { onResult(Result.failure(it)) }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Agrega una serie a un ejercicio con peso y repeticiones, asignando automaticamente el orden
    fun addSetToExercise(
        routineId: String,
        routineExerciseId: String,
        peso: Float?,
        repeticiones: Int?,
        onResult: (Result<String>) -> Unit
    ) {
        val setsCollection = routinesCollection.document(routineId)
            .collection("exercises")
            .document(routineExerciseId)
            .collection("sets")

        setsCollection
            .orderBy("orden", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { query ->
                // Calcular el siguiente orden basado en el maximo actual
                val nextOrder = (query.documents.firstOrNull()?.getLong("orden")?.toInt() ?: -1) + 1
                val doc = setsCollection.document()
                val payload = mapOf(
                    "peso" to (peso ?: 0f),
                    "repeticiones" to (repeticiones ?: 0),
                    "orden" to nextOrder,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                doc.set(payload)
                    .addOnSuccessListener { onResult(Result.success(doc.id)) }
                    .addOnFailureListener { onResult(Result.failure(it)) }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Obtiene todas las series de un ejercicio ordenadas por su campo "orden"
    fun getSetsOfExercise(
        routineId: String,
        routineExerciseId: String,
        onResult: (Result<List<CloudRoutineSet>>) -> Unit
    ) {
        routinesCollection.document(routineId)
            .collection("exercises")
            .document(routineExerciseId)
            .collection("sets")
            .orderBy("orden")
            .get()
            .addOnSuccessListener { query ->
                val sets = query.documents.mapNotNull {
                    it.toCloudRoutineSet(routineId, routineExerciseId)
                }
                onResult(Result.success(sets))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Elimina una serie especifica de un ejercicio
    fun deleteSet(
        routineId: String,
        routineExerciseId: String,
        setId: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        routinesCollection.document(routineId)
            .collection("exercises")
            .document(routineExerciseId)
            .collection("sets")
            .document(setId)
            .delete()
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Actualiza peso y repeticiones de una serie
// Si el peso cambia, guarda el valor anterior en una subcoleccion "history" para trazabilidad
    fun updateSet(
        routineId: String,
        routineExerciseId: String,
        setId: String,
        peso: Float?,
        repeticiones: Int?,
        onResult: (Result<Unit>) -> Unit
    ) {
        val setDoc = routinesCollection.document(routineId)
            .collection("exercises")
            .document(routineExerciseId)
            .collection("sets")
            .document(setId)

        setDoc.get()
            .addOnSuccessListener { snapshot ->
                // Leer valor actual antes de sobreescribir para guardar en historial
                val pesoAnterior = snapshot.getDouble("peso")?.toFloat()
                    ?: snapshot.getLong("peso")?.toFloat()
                val repsAnteriores = snapshot.getLong("repeticiones")?.toInt() ?: 0

                // Solo guardar en historial si el peso realmente cambio
                if (pesoAnterior != null && pesoAnterior != (peso ?: 0f)) {
                    val historyPayload = mapOf(
                        "peso" to pesoAnterior,
                        "repeticiones" to repsAnteriores,
                        "fecha" to FieldValue.serverTimestamp()
                    )
                    setDoc.collection("history").add(historyPayload)
                }

                // Actualizar valor actual
                setDoc.update(
                    mapOf(
                        "peso" to (peso ?: 0f),
                        "repeticiones" to (repeticiones ?: 0)
                    )
                )
                .addOnSuccessListener { onResult(Result.success(Unit)) }
                .addOnFailureListener { onResult(Result.failure(it)) }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Crea una nueva nota para un usuario
    fun createNote(
        userUid: String,
        titulo: String,
        contenido: String,
        onResult: (Result<String>) -> Unit
    ) {
        val doc = notesCollection.document()
        val payload = mapOf(
            "userUid" to userUid,
            "titulo" to titulo,
            "contenido" to contenido,
            "fecha" to System.currentTimeMillis().toString(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        doc.set(payload)
            .addOnSuccessListener { onResult(Result.success(doc.id)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Obtiene todas las notas de un usuario ordenadas por fecha descendente
    fun getNotesByUser(
        userUid: String,
        onResult: (Result<List<CloudNote>>) -> Unit
    ) {
        notesCollection
            .whereEqualTo("userUid", userUid)
            .get()
            .addOnSuccessListener { query ->
                val notes = query.documents
                    .mapNotNull { it.toCloudNote() }
                    .sortedByDescending { it.fecha }
                onResult(Result.success(notes))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Elimina una nota por su ID
    fun deleteNote(
        noteId: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        notesCollection.document(noteId)
            .delete()
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Actualiza el titulo y contenido de una nota existente
    fun updateNote(
        noteId: String,
        titulo: String,
        contenido: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        val payload = mapOf(
            "titulo" to titulo,
            "contenido" to contenido,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        notesCollection.document(noteId)
            .update(payload)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Obtiene estadisticas de progreso de todos los ejercicios de una rutina
// Incluye evolucion historica de pesos por set para graficar tendencias
    fun getRoutineExerciseStats(
        routineId: String,
        onResult: (Result<List<ExerciseProgressStat>>) -> Unit
    ) {
        getExercisesOfRoutine(routineId) { exercisesResult ->
            exercisesResult.onSuccess { exercises ->
                if (exercises.isEmpty()) {
                    onResult(Result.success(emptyList()))
                    return@onSuccess
                }

                val stats = mutableListOf<ExerciseProgressStat>()
                var pendientes = exercises.size

                exercises.forEach { exercise ->
                    val setsCollection = routinesCollection.document(routineId)
                        .collection("exercises")
                        .document(exercise.id)
                        .collection("sets")
                        .orderBy("orden")

                    setsCollection.get().addOnSuccessListener { setsQuery ->
                        val series = setsQuery.documents.mapNotNull {
                            it.toCloudRoutineSet(routineId, exercise.id)
                        }

                        if (series.isEmpty()) {
                            stats.add(buildStatConHistorial(exercise, series, emptyList()))
                            pendientes--
                            if (pendientes == 0) onResult(Result.success(stats))
                            return@addOnSuccessListener
                        }

                        // Para cada set cargar su historial ordenado por fecha en paralelo
                        val historyTasks = setsQuery.documents.map { setDoc ->
                            setDoc.reference.collection("history")
                                .orderBy("fecha")
                                .get()
                        }

                        Tasks.whenAllSuccess<com.google.firebase.firestore.QuerySnapshot>(historyTasks)
                            .addOnSuccessListener { historyQueries ->
                                // Reconstruir arrays de pesos historicos por set
                                // El ultimo elemento de la lista de pesos es siempre el valor actual
                                val pesosHistoricosPorSet = setsQuery.documents.mapIndexed { i, setDoc ->
                                    val pesoActual = setDoc.getDouble("peso")?.toFloat()
                                        ?: setDoc.getLong("peso")?.toFloat() ?: 0f
                                    val repsActuales = setDoc.getLong("repeticiones")?.toInt() ?: 0
                                    val historialPesos = historyQueries[i].documents.mapNotNull { h ->
                                        h.getDouble("peso")?.toFloat() ?: h.getLong("peso")?.toFloat()
                                    }
                                    Pair(historialPesos + pesoActual, repsActuales)
                                }

                                val stat = buildStatConHistorial(exercise, series, pesosHistoricosPorSet)
                                stats.add(stat)
                                pendientes--
                                if (pendientes == 0) onResult(Result.success(stats))
                            }
                            .addOnFailureListener { onResult(Result.failure(it)) }
                    }.addOnFailureListener { onResult(Result.failure(it)) }
                }
            }.onFailure { onResult(Result.failure(it)) }
        }
    }

    // Obtiene un resumen consolidado de progreso de una rutina
    // Incluye totales de ejercicios, series, volumen y el ejercicio mas destacable
    fun getRoutineSummary(
        routineId: String,
        onResult: (Result<RoutineProgressSummary>) -> Unit
    ) {
        getRoutineExerciseStats(routineId) { statsResult ->
            statsResult.onSuccess { stats ->
                val summary = RoutineProgressSummary(
                    totalEjercicios = stats.size,
                    totalSeries = stats.sumOf { it.totalSeries },
                    totalVolumen = stats.sumOf { it.totalVolumen.toDouble() }.toFloat(),
                    ejercicioDestacado = stats.maxByOrNull { it.totalVolumen }?.ejercicioNombre ?: "Sin datos",
                    mejorTendencia = stats.maxByOrNull { it.tendenciaPeso }?.ejercicioNombre ?: "Sin datos"
                )
                onResult(Result.success(summary))
            }.onFailure { onResult(Result.failure(it)) }
        }
    }

    // Obtiene estadisticas de progreso para todas las rutinas de un usuario
    fun getUserRoutineStats(
        userUid: String,
        onResult: (Result<List<Pair<CloudRoutine, RoutineProgressSummary>>>) -> Unit
    ) {
        getRoutinesByUser(userUid) { routinesResult ->
            routinesResult.onSuccess { routines ->
                if (routines.isEmpty()) {
                    onResult(Result.success(emptyList()))
                    return@onSuccess
                }

                // Obtener ejercicios de todas las rutinas en paralelo
                val tasks = routines.map { routine ->
                    routinesCollection.document(routine.id)
                        .collection("exercises")
                        .get()
                }

                Tasks.whenAllSuccess<com.google.firebase.firestore.QuerySnapshot>(tasks)
                    .addOnSuccessListener {
                        val results = mutableListOf<Pair<CloudRoutine, RoutineProgressSummary>>()
                        var processed = 0

                        routines.forEach { routine ->
                            getRoutineSummary(routine.id) { summaryResult ->
                                summaryResult.onSuccess { summary ->
                                    results.add(routine to summary)
                                }
                                processed += 1
                                if (processed == routines.size) {
                                    onResult(Result.success(results.sortedBy { it.first.nombre }))
                                }
                            }
                        }
                    }
                    .addOnFailureListener { onResult(Result.failure(it)) }
            }.onFailure { onResult(Result.failure(it)) }
        }
    }

    // Obtiene todos los planes nutricionales de un usuario
    fun getPlansByUser(userUid: String, onResult: (Result<List<CloudPlanNutricional>>) -> Unit) {
        plansCollection
            .whereEqualTo("userUid", userUid)
            .get()
            .addOnSuccessListener { query ->
                val plans = query.documents.mapNotNull { it.toCloudPlan() }
                onResult(Result.success(plans))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Obtiene un plan nutricional especifico por su ID
    fun getPlanById(planId: String, onResult: (Result<CloudPlanNutricional>) -> Unit) {
        plansCollection.document(planId)
            .get()
            .addOnSuccessListener { snapshot ->
                val plan = snapshot.toCloudPlan()
                if (plan == null) {
                    onResult(Result.failure(IllegalStateException("No existe el plan indicado.")))
                } else {
                    onResult(Result.success(plan))
                }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Actualiza el nombre y descripcion de un plan nutricional
    fun updatePlan(
        planId: String,
        nombre: String,
        descripcion: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        plansCollection.document(planId)
            .update(
                mapOf(
                    "nombre" to nombre,
                    "descripcion" to descripcion
                )
            )
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Elimina un plan nutricional y todas sus comidas asociadas
    fun deletePlan(planId: String, onResult: (Result<Unit>) -> Unit) {
        val mealsCollection = plansCollection.document(planId).collection("meals")
        mealsCollection.get()
            .addOnSuccessListener { query ->
                val batch = firestore.batch()
                // Eliminar todas las comidas primero
                query.documents.forEach { batch.delete(it.reference) }
                // Luego eliminar el plan
                batch.delete(plansCollection.document(planId))
                batch.commit()
                    .addOnSuccessListener { onResult(Result.success(Unit)) }
                    .addOnFailureListener { onResult(Result.failure(it)) }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Agrega una comida a un plan nutricional con tipo, nombre, descripcion, calorias e imagen
    fun addMeal(
        planId: String,
        tipo: String,
        nombre: String,
        descripcion: String,
        calorias: Int,
        imagenId: Int,
        orden: Int,
        onResult: (Result<String>) -> Unit
    ) {
        val doc = plansCollection.document(planId).collection("meals").document()
        val payload = mapOf(
            "tipo" to tipo,
            "nombre" to nombre,
            "descripcion" to descripcion,
            "calorias" to calorias,
            "imagenId" to imagenId,
            "orden" to orden,
            "createdAt" to FieldValue.serverTimestamp()
        )
        doc.set(payload)
            .addOnSuccessListener { onResult(Result.success(doc.id)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Obtiene todas las comidas de un plan ordenadas por su campo "orden"
    fun getMealsOfPlan(planId: String, onResult: (Result<List<CloudComidaPlan>>) -> Unit) {
        plansCollection.document(planId)
            .collection("meals")
            .orderBy("orden")
            .get()
            .addOnSuccessListener { query ->
                val meals = query.documents.mapNotNull { it.toCloudMeal(planId) }
                onResult(Result.success(meals))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Verifica si un plan ya tiene al menos una comida de un tipo especifico (Desayuno, Almuerzo, etc)
    fun hasMealType(planId: String, tipo: String, onResult: (Result<Boolean>) -> Unit) {
        plansCollection.document(planId)
            .collection("meals")
            .whereEqualTo("tipo", tipo)
            .limit(1)
            .get()
            .addOnSuccessListener { onResult(Result.success(!it.isEmpty)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Actualiza los datos de una comida existente (nombre, descripcion, calorias, imagen)
    fun updateMeal(
        planId: String,
        mealId: String,
        nombre: String,
        descripcion: String,
        calorias: Int,
        imagenId: Int,
        onResult: (Result<Unit>) -> Unit
    ) {
        plansCollection.document(planId)
            .collection("meals")
            .document(mealId)
            .update(
                mapOf(
                    "nombre" to nombre,
                    "descripcion" to descripcion,
                    "calorias" to calorias,
                    "imagenId" to imagenId
                )
            )
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Elimina una comida especifica de un plan
    fun deleteMeal(planId: String, mealId: String, onResult: (Result<Unit>) -> Unit) {
        plansCollection.document(planId)
            .collection("meals")
            .document(mealId)
            .delete()
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Helper interno: actualiza el estado de una solicitud de servicio (Aceptada/Rechazada)
    private fun updateServiceRequestStatus(
        requestId: String,
        status: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        serviceRequestsCollection.document(requestId)
            .update("status", status)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Helper interno: obtiene clientes asignados a un profesional segun la coleccion de asignaciones
    private fun getAssignedClients(
        assignmentsCollection: com.google.firebase.firestore.CollectionReference,
        professionalField: String,
        professionalUid: String,
        onResult: (Result<List<UserProfile>>) -> Unit
    ) {
        assignmentsCollection
            .whereEqualTo(professionalField, professionalUid)
            .get()
            .addOnSuccessListener { query ->
                // Extraer UIDs de clientes
                val clientIds = query.documents.mapNotNull { it.getString("clientUid") }
                // Obtener perfiles de los clientes
                fetchUserProfiles(clientIds, onResult)
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Helper interno: obtiene perfiles de usuario por lista de UIDs con deduplicacion
    private fun fetchUserProfiles(
        uids: List<String>,
        onResult: (Result<List<UserProfile>>) -> Unit
    ) {
        // Normalizar: eliminar duplicados y vacios
        val normalizedIds = uids.distinct().filter { it.isNotBlank() }
        if (normalizedIds.isEmpty()) {
            onResult(Result.success(emptyList()))
            return
        }

        // Cargar todos los perfiles en paralelo
        val tasks = normalizedIds.map { usersCollection.document(it).get() }
        Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
            .addOnSuccessListener { docs ->
                val profiles = docs.mapNotNull { it.toUserProfile() }
                onResult(Result.success(profiles))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Convierte un DocumentSnapshot a UserProfile
    private fun DocumentSnapshot.toUserProfile(): UserProfile? {
        if (!exists()) return null
        return UserProfile(
            uid = id,
            legacyLocalId = getLong("legacyLocalId")?.toInt() ?: -1,
            name = getString("name").orEmpty(),
            email = getString("email").orEmpty(),
            role = getString("role") ?: "Usuario"
        )
    }

    // Convierte un DocumentSnapshot a CloudPlanNutricional
    private fun DocumentSnapshot.toCloudPlan(): CloudPlanNutricional? {
        if (!exists()) return null
        return CloudPlanNutricional(
            id = id,
            nombre = getString("nombre").orEmpty(),
            descripcion = getString("descripcion").orEmpty(),
            userUid = getString("userUid").orEmpty(),
            nutritionistUid = getString("nutritionistUid").orEmpty()
        )
    }

    // Convierte un DocumentSnapshot a CloudComidaPlan
    private fun DocumentSnapshot.toCloudMeal(planId: String): CloudComidaPlan? {
        if (!exists()) return null
        return CloudComidaPlan(
            id = id,
            planId = planId,
            tipo = getString("tipo").orEmpty(),
            nombre = getString("nombre").orEmpty(),
            descripcion = getString("descripcion").orEmpty(),
            calorias = getLong("calorias")?.toInt() ?: 0,
            imagenId = getLong("imagenId")?.toInt() ?: 0,
            orden = getLong("orden")?.toInt() ?: 0
        )
    }

    // Convierte un DocumentSnapshot a CloudRoutine
    private fun DocumentSnapshot.toCloudRoutine(): CloudRoutine? {
        if (!exists()) return null
        return CloudRoutine(
            id = id,
            nombre = getString("nombre").orEmpty(),
            userUid = getString("userUid").orEmpty(),
            trainerUid = getString("trainerUid"),
            createdByRole = getString("createdByRole") ?: "Usuario",
            archived = getBoolean("archived") ?: false,
            diaSemana = getString("diaSemana").orEmpty()
        )
    }

    // Convierte un DocumentSnapshot a CloudRoutineExercise
    private fun DocumentSnapshot.toCloudRoutineExercise(routineId: String): CloudRoutineExercise? {
        if (!exists()) return null
        return CloudRoutineExercise(
            id = id,
            routineId = routineId,
            exerciseId = getLong("exerciseId")?.toInt() ?: 0,
            exerciseName = getString("exerciseName").orEmpty(),
            orden = getLong("orden")?.toInt() ?: 0
        )
    }

    // Convierte un DocumentSnapshot a CloudRoutineSet
    private fun DocumentSnapshot.toCloudRoutineSet(
        routineId: String,
        routineExerciseId: String
    ): CloudRoutineSet? {
        if (!exists()) return null
        return CloudRoutineSet(
            id = id,
            routineId = routineId,
            routineExerciseId = routineExerciseId,
            peso = getDouble("peso")?.toFloat() ?: getLong("peso")?.toFloat() ?: 0f,
            repeticiones = getLong("repeticiones")?.toInt() ?: 0,
            orden = getLong("orden")?.toInt() ?: 0
        )
    }

    // Convierte un DocumentSnapshot a CloudNote
    private fun DocumentSnapshot.toCloudNote(): CloudNote? {
        if (!exists()) return null
        return CloudNote(
            id = id,
            userUid = getString("userUid").orEmpty(),
            titulo = getString("titulo").orEmpty(),
            contenido = getString("contenido").orEmpty(),
            fecha = getString("fecha").orEmpty()
        )
    }

// Construye estadisticas de ejercicio incluyendo evolucion historica de pesos
    // La tendencia prioriza la mejora de peso maximo; si es igual, mira el volumen
    private fun buildStatConHistorial(
        exercise: CloudRoutineExercise,
        series: List<CloudRoutineSet>,
        pesosHistoricosPorSet: List<Pair<List<Float>, Int>>
    ): ExerciseProgressStat {
        // Calcular totales basicos
        val totalSeries = series.size
        val maxPeso = series.maxOfOrNull { it.peso } ?: 0f
        val promedioPeso = if (totalSeries > 0) series.map { it.peso }.average().toFloat() else 0f
        val maxRepeticiones = series.maxOfOrNull { it.repeticiones } ?: 0
        val totalRepeticiones = series.sumOf { it.repeticiones }
        val totalVolumen = series.sumOf { (it.peso * it.repeticiones).toDouble() }.toFloat()

        // Numero de "momentos" historicos = el set con mas cambios registrados
        val numMomentos = pesosHistoricosPorSet.maxOfOrNull { it.first.size } ?: 1

        // Para cada momento calcular maxPeso y volumen total de ese instante
        // Si un set aun no tenia valor en ese momento, usar su primer valor conocido
        val evolucionMaxPeso = (0 until numMomentos).map { momento ->
            pesosHistoricosPorSet.mapNotNull { (historial, _) ->
                historial.getOrNull(momento) ?: historial.firstOrNull()
            }.maxOrNull() ?: 0f
        }

        val evolucionVolumen = (0 until numMomentos).map { momento ->
            pesosHistoricosPorSet.sumOf { (historial, reps) ->
                val peso = historial.getOrNull(momento) ?: historial.firstOrNull() ?: 0f
                (peso * reps).toDouble()
            }.toFloat()
        }

        // Calcular tendencia: diferencia entre el ultimo y primer momento
        val difMaxPeso = if (evolucionMaxPeso.size >= 2)
            evolucionMaxPeso.last() - evolucionMaxPeso.first() else 0f
        val difVolumen = if (evolucionVolumen.size >= 2)
            evolucionVolumen.last() - evolucionVolumen.first() else 0f

        // Priorizar mejora de peso maximo; si es igual, usar diferencia de volumen
        val tendenciaPeso = if (difMaxPeso != 0f) difMaxPeso else difVolumen / 100f

        return ExerciseProgressStat(
            rutinaEjercicioId = -1,
            ejercicioNombre = exercise.exerciseName,
            totalSeries = totalSeries,
            promedioPeso = promedioPeso,
            maxPeso = maxPeso,
            maxRepeticiones = maxRepeticiones,
            totalRepeticiones = totalRepeticiones,
            totalVolumen = totalVolumen,
            tendenciaPeso = tendenciaPeso,
            pesosPorSerie = evolucionMaxPeso,
            repsPorSerie = series.map { it.repeticiones },
            evolucionVolumen = evolucionVolumen
        )
    }

    // Guarda una compra realizada en la tienda, incluyendo items, total y fecha
    fun savePurchase(
        userUid: String,
        items: List<CompraItem>,
        total: Double,
        onResult: (Result<String>) -> Unit
    ) {
        // Convertir items al formato esperado por Firestore
        val itemsData = items.map { item ->
            mapOf(
                "articuloId" to item.articuloId,
                "nombre" to item.nombre,
                "cantidad" to item.cantidad,
                "precioUnitario" to item.precioUnitario,
                "subtotal" to item.subtotal
            )
        }

        val payload = hashMapOf(
            "userUid" to userUid,
            "items" to itemsData,
            "total" to total,
            "fecha" to FieldValue.serverTimestamp()
        )

        comprasCollection.add(payload)
            .addOnSuccessListener { onResult(Result.success(it.id)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Obtiene el historial de compras de un usuario ordenadas por fecha descendente
    fun getPurchaseHistory(
        userUid: String,
        onResult: (Result<List<Compra>>) -> Unit
    ) {
        comprasCollection
            .whereEqualTo("userUid", userUid)
            .get()
            .addOnSuccessListener { query ->
                val compras = query.documents
                    .mapNotNull { it.toCompra() }
                    .sortedByDescending { it.fecha }
                onResult(Result.success(compras))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // Convierte un DocumentSnapshot a Compra (compra con items y total)
    // Maneja parsing de fecha desde Timestamp o Long
    private fun DocumentSnapshot.toCompra(): Compra? {
        if (!exists()) return null
        // Parsear lista de items con conversion segura de tipos
        val itemsList = get("items") as? List<*>
        val items = itemsList?.mapNotNull { item ->
            val map = item as? Map<*, *>
            if (map != null) {
                CompraItem(
                    articuloId = (map["articuloId"] as? Number)?.toInt() ?: 0,
                    nombre = map["nombre"] as? String ?: "",
                    cantidad = (map["cantidad"] as? Number)?.toInt() ?: 0,
                    precioUnitario = (map["precioUnitario"] as? Number)?.toDouble() ?: 0.0,
                    subtotal = (map["subtotal"] as? Number)?.toDouble() ?: 0.0
                )
            } else null
        } ?: emptyList()

        // Intentar leer fecha como Timestamp, luego como Long
        val fecha: Long = try {
            val ts = getTimestamp("fecha")
            ts?.toDate()?.time ?: getLong("fecha") ?: System.currentTimeMillis()
        } catch (e: Exception) {
            getLong("fecha") ?: System.currentTimeMillis()
        }

        return Compra(
            id = id,
            userUid = getString("userUid").orEmpty(),
            items = items,
            total = getDouble("total") ?: 0.0,
            fecha = fecha
        )
    }

    // Convierte un DocumentSnapshot a ServiceRequest (solicitud de servicio)
    private fun DocumentSnapshot.toServiceRequest(): ServiceRequest? {
        if (!exists()) return null
        return ServiceRequest(
            id = id,
            userUid = getString("userUid").orEmpty(),
            userName = getString("userName").orEmpty(),
            userEmail = getString("userEmail").orEmpty(),
            professionalUid = getString("professionalUid").orEmpty(),
            professionalName = getString("professionalName").orEmpty(),
            requestedRole = getString("requestedRole").orEmpty(),
            status = getString("status") ?: "Pendiente"
        )
    }
}
