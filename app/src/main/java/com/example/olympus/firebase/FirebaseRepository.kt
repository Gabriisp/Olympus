package com.example.olympus

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

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

    companion object {
        val instance: FirebaseRepository by lazy { FirebaseRepository() }
    }

    fun getCurrentUserUid(): String? = auth.currentUser?.uid

    fun signOut() {
        auth.signOut()
    }

    fun sendPasswordResetEmail(
        email: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        auth.setLanguageCode("es")
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

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

        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener { onResult(Result.success(Unit)) }
                    .addOnFailureListener { onResult(Result.failure(it)) }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun submitRoleRequest(
        userUid: String?,
        fullName: String,
        email: String,
        phone: String,
        requestedRoles: List<String>,
        details: String,
        onResult: (Result<Unit>) -> Unit
    ) {
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

    private fun enqueueRoleRequestEmail(
        subject: String,
        body: String,
        replyTo: String,
        onResult: (Result<Unit>) -> Unit
    ) {
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

    fun submitServiceRequestToProfessional(
        userUid: String,
        userName: String,
        userEmail: String,
        professionalUid: String,
        professionalName: String,
        requestedRole: String,
        onResult: (Result<Boolean>) -> Unit
    ) {
        val documentId = "${professionalUid}_$userUid"
        val document = serviceRequestsCollection.document(documentId)

        document.get()
            .addOnSuccessListener { snapshot ->
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

    fun acceptServiceRequest(
        request: ServiceRequest,
        onResult: (Result<Unit>) -> Unit
    ) {
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

        assignAction { assignmentResult ->
            assignmentResult
                .onSuccess {
                    updateServiceRequestStatus(request.id, "Aceptada", onResult)
                }
                .onFailure { onResult(Result.failure(it)) }
        }
    }

    fun rejectServiceRequest(
        requestId: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        updateServiceRequestStatus(requestId, "Rechazada", onResult)
    }

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

    fun getAssignedClientsForTrainer(
        trainerUid: String,
        onResult: (Result<List<UserProfile>>) -> Unit
    ) {
        getAssignedClients(trainerAssignments, "trainerUid", trainerUid, onResult)
    }

    fun getAssignedClientsForNutritionist(
        nutritionistUid: String,
        onResult: (Result<List<UserProfile>>) -> Unit
    ) {
        getAssignedClients(nutritionistAssignments, "nutritionistUid", nutritionistUid, onResult)
    }

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

    fun createRoutine(
        nombre: String,
        userUid: String,
        trainerUid: String?,
        createdByRole: String,
        onResult: (Result<String>) -> Unit
    ) {
        val doc = routinesCollection.document()
        val payload = mapOf(
            "nombre" to nombre,
            "userUid" to userUid,
            "trainerUid" to trainerUid,
            "createdByRole" to createdByRole,
            "archived" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        doc.set(payload)
            .addOnSuccessListener { onResult(Result.success(doc.id)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

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

    fun deleteRoutine(
        routineId: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        val exercisesCollection = routinesCollection.document(routineId).collection("exercises")
        exercisesCollection.get()
            .addOnSuccessListener { exercisesQuery ->
                val setFetchTasks = exercisesQuery.documents.map { exerciseDoc ->
                    exerciseDoc.reference.collection("sets").get()
                }

                Tasks.whenAllSuccess<com.google.firebase.firestore.QuerySnapshot>(setFetchTasks)
                    .addOnSuccessListener { setQueries ->
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
                query.documents.forEach { batch.delete(it.reference) }
                batch.delete(exerciseRef)
                batch.commit()
                    .addOnSuccessListener { onResult(Result.success(Unit)) }
                    .addOnFailureListener { onResult(Result.failure(it)) }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

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

    fun deleteNote(
        noteId: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        notesCollection.document(noteId)
            .delete()
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

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

                val tasks = exercises.map { exercise ->
                    routinesCollection.document(routineId)
                        .collection("exercises")
                        .document(exercise.id)
                        .collection("sets")
                        .orderBy("orden")
                        .get()
                }

                Tasks.whenAllSuccess<com.google.firebase.firestore.QuerySnapshot>(tasks)
                    .addOnSuccessListener { setQueries ->
                        val stats = exercises.mapIndexed { index, exercise ->
                            val series = setQueries[index].documents.mapNotNull {
                                it.toCloudRoutineSet(routineId, exercise.id)
                            }
                            buildExerciseProgressStatFromCloud(exercise, series)
                        }
                        onResult(Result.success(stats))
                    }
                    .addOnFailureListener { onResult(Result.failure(it)) }
            }.onFailure { onResult(Result.failure(it)) }
        }
    }

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

    fun deletePlan(planId: String, onResult: (Result<Unit>) -> Unit) {
        val mealsCollection = plansCollection.document(planId).collection("meals")
        mealsCollection.get()
            .addOnSuccessListener { query ->
                val batch = firestore.batch()
                query.documents.forEach { batch.delete(it.reference) }
                batch.delete(plansCollection.document(planId))
                batch.commit()
                    .addOnSuccessListener { onResult(Result.success(Unit)) }
                    .addOnFailureListener { onResult(Result.failure(it)) }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

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

    fun hasMealType(planId: String, tipo: String, onResult: (Result<Boolean>) -> Unit) {
        plansCollection.document(planId)
            .collection("meals")
            .whereEqualTo("tipo", tipo)
            .limit(1)
            .get()
            .addOnSuccessListener { onResult(Result.success(!it.isEmpty)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

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

    fun deleteMeal(planId: String, mealId: String, onResult: (Result<Unit>) -> Unit) {
        plansCollection.document(planId)
            .collection("meals")
            .document(mealId)
            .delete()
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

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
                val clientIds = query.documents.mapNotNull { it.getString("clientUid") }
                fetchUserProfiles(clientIds, onResult)
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    private fun fetchUserProfiles(
        uids: List<String>,
        onResult: (Result<List<UserProfile>>) -> Unit
    ) {
        val normalizedIds = uids.distinct().filter { it.isNotBlank() }
        if (normalizedIds.isEmpty()) {
            onResult(Result.success(emptyList()))
            return
        }

        val tasks = normalizedIds.map { usersCollection.document(it).get() }
        Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
            .addOnSuccessListener { docs ->
                val profiles = docs.mapNotNull { it.toUserProfile() }
                onResult(Result.success(profiles))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

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

    private fun DocumentSnapshot.toCloudRoutine(): CloudRoutine? {
        if (!exists()) return null
        return CloudRoutine(
            id = id,
            nombre = getString("nombre").orEmpty(),
            userUid = getString("userUid").orEmpty(),
            trainerUid = getString("trainerUid"),
            createdByRole = getString("createdByRole") ?: "Usuario",
            archived = getBoolean("archived") ?: false
        )
    }

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

    private fun buildExerciseProgressStatFromCloud(
        exercise: CloudRoutineExercise,
        series: List<CloudRoutineSet>
    ): ExerciseProgressStat {
        val totalSeries = series.size
        val maxPeso = series.maxOfOrNull { it.peso } ?: 0f
        val promedioPeso = if (totalSeries > 0) series.map { it.peso }.average().toFloat() else 0f
        val maxRepeticiones = series.maxOfOrNull { it.repeticiones } ?: 0
        val totalRepeticiones = series.sumOf { it.repeticiones }
        val totalVolumen = series.sumOf { (it.peso * it.repeticiones).toDouble() }.toFloat()
        val tendenciaPeso = if (totalSeries >= 2) {
            series.last().peso - series.first().peso
        } else {
            0f
        }

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
            pesosPorSerie = series.map { it.peso },
            repsPorSerie = series.map { it.repeticiones }
        )
    }

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
