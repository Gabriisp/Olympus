# Olympus

Guía de organización del proyecto

# 1. Qué es Olympus

Olympus es una app Android desarrollada en Kotlin dentro de Android Studio con:

- `Firebase Authentication` para registro, login, cambio y recuperación de contraseña.
- `Cloud Firestore` para perfiles, asignaciones, solicitudes, nutrición, rutinas, ejercicios, series y notas.
- Interfaz basada en Activities, Fragments, Adapters y layouts XML.

Actualmente la lógica principal de la app ya está orientada a Firebase.


# 2. Organización lógica del código

# Código Kotlin

- `app/src/main/java/com/example/olympus/activities`
  - Pantallas principales de la app
- `app/src/main/java/com/example/olympus/fragments`
  - Pestañas o secciones dentro de pantallas
- `app/src/main/java/com/example/olympus/adapters`
  - Adaptadores de RecyclerView
- `app/src/main/java/com/example/olympus/firebase`
  - Acceso a Firebase y Firestore
- `app/src/main/java/com/example/olympus/database`
  - Código SQLite heredado o de respaldo
- `app/src/main/java/com/example/olympus/models`
  - Modelos de datos cloud
- `app/src/main/java/com/example/olympus/utils`
  - Utilidades como `SessionManager`
- `app/src/main/java/com/example/olympus`
  - Catálogos estáticos como `EjerciciosData.kt` y `ComidaData.kt`

# Recursos XML

- `app/src/main/res/layout`
  - Vistas XML de activities, fragments, dialogs e items
- `app/src/main/res/drawable`
  - Imágenes y recursos visuales
- `app/src/main/res/menu`
  - Menús contextuales
- `app/src/main/res/values`
  - Colores, strings, temas
- `app/src/main/res/xml`
  - Configuraciones XML

# 3. Qué usa la app en Firebase

La lógica principal ya trabaja o debe trabajar sobre estas colecciones y servicios:

- `Authentication`
  - Registro
  - Login
  - Recuperación de contraseña
  - Cambio de contraseña

- `Firestore`
  - `users`
  - `roleRequests`
  - `serviceRequests`
  - `trainerAssignments`
  - `nutritionistAssignments`
  - `mail`
  - `nutritionPlans`
  - `nutritionPlans/{planId}/meals`
  - `routines`
  - `routines/{routineId}/exercises`
  - `routines/{routineId}/exercises/{exerciseId}/sets`
  - `notes`


# 4. Qué archivos deberían ir en cada rama

### Rama `login`

Se usa para:

- autenticación
- recuperación de contraseña
- cambio de contraseña
- sesión
- registro

Archivos típicos:

- `activities/LoginActivity.kt`
- `activities/RegisterActivity.kt`
- `activities/ChangePasswordActivity.kt`
- `utils/SessionManager.kt`
- `firebase/FirebaseRepository.kt`
- layouts relacionados:
  - `activity_login.xml`
  - `activity_register.xml`
  - `activity_change_password.xml`

# Rama `fragments`

Se usa para:

- `RutinasFragment`
- `NotasFragment`
- `NutricionFragment`
- `StatsFragment`
- navegación interna del usuario

Archivos típicos:

- `fragments/RutinasFragment.kt`
- `fragments/NotasFragment.kt`
- `fragments/NutricionFragment.kt`
- `fragments/StatsFragment.kt`
- layouts:
  - `fragment_rutinas.xml`
  - `fragment_notas.xml`
  - `fragment_nutricion.xml`
  - `fragment_stats.xml`

# Rama `adapters`

Se usa para:

- RecyclerViews
- items de listas
- cambios en la forma de pintar datos

Archivos típicos:

- `adapters/*.kt`
- items XML asociados:
  - `item_*.xml`

Ejemplos:

- `RutinasAdapter.kt`
- `RutinasEntrenadorAdapter.kt`
- `UsuariosAdapter.kt`
- `CloudNotasAdapter.kt`
- `CloudRutinaEjerciciosAdapter.kt`
- `CloudSeriesAdapter.kt`

# Rama `database`

Se usa para:

- cambios en SQLite heredado
- migraciones
- limpieza o retirada progresiva de helpers locales
- cambios de modelos de datos de persistencia

Archivos típicos:

- `database/DatabaseHelper.kt`
- `database/RutinasDatabaseHelper.kt`
- `database/NotasDatabaseHelper.kt`
- `database/NutricionistaDatabaseHelper.kt`
- `models/UserProfile.kt`
- `firebase/FirebaseRepository.kt`

# Rama `interface`

Se usa para:

- UI visual
- layouts
- drawables
- colores
- menús
- textos

Archivos típicos:

- `res/layout/*`
- `res/drawable/*`
- `res/menu/*`
- `res/values/colors.xml`
- `res/values/strings.xml`
- `res/values/themes.xml`
- pantallas Activity/Fragment si el cambio es puramente visual