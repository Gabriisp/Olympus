package com.example.olympus

data class UserProfile(
    val uid: String = "",
    val legacyLocalId: Int = -1,
    val name: String = "",
    val email: String = "",
    val role: String = "Usuario"
)

data class CloudPlanNutricional(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val userUid: String = "",
    val nutritionistUid: String = ""
)

data class CloudComidaPlan(
    val id: String = "",
    val planId: String = "",
    val tipo: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val calorias: Int = 0,
    val imagenId: Int = 0,
    val orden: Int = 0
)

data class ServiceRequest(
    val id: String = "",
    val userUid: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val professionalUid: String = "",
    val professionalName: String = "",
    val requestedRole: String = "",
    val status: String = "Pendiente"
)

data class CloudRoutine(
    val id: String = "",
    val nombre: String = "",
    val userUid: String = "",
    val trainerUid: String? = null,
    val createdByRole: String = "Usuario",
    val archived: Boolean = false
)

data class CloudRoutineExercise(
    val id: String = "",
    val routineId: String = "",
    val exerciseId: Int = 0,
    val exerciseName: String = "",
    val orden: Int = 0
)

data class CloudRoutineSet(
    val id: String = "",
    val routineId: String = "",
    val routineExerciseId: String = "",
    val peso: Float = 0f,
    val repeticiones: Int = 0,
    val orden: Int = 0
)

data class CloudNote(
    val id: String = "",
    val userUid: String = "",
    val titulo: String = "",
    val contenido: String = "",
    val fecha: String = ""
)
