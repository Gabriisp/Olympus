# Olympus

Guia del proyecto y de la organizacion de ramas para que el repositorio quede limpio, entendible y facil de subir a GitHub sin mezclar archivos innecesarios.

## Estado actual

Proyecto Android nativo en Kotlin con XML, Gradle Kotlin DSL y Firebase.

Tecnologias principales:

- Kotlin
- Android SDK 36
- Firebase Authentication
- Cloud Firestore
- Material Components
- MPAndroidChart

Segun tu captura del repositorio en GitHub, ahora mismo existen estas ramas:

- `main`
- `develop`
- `adapters`
- `database`
- `fragments`
- `interface`
- `login`

## Que es Olympus

Olympus es una app Android orientada a gestion de usuarios, rutinas, nutricion y seguimiento. La aplicacion distingue varios roles:

- `Usuario`
- `Entrenador`
- `Nutricionista`

Flujos que ya se ven en el codigo:

- registro e inicio de sesion
- cambio y recuperacion de contrasena
- solicitud de rol profesional
- busqueda y solicitud de profesionales
- asignacion de entrenador o nutricionista
- gestion de rutinas
- gestion de ejercicios y series
- gestion de planes nutricionales y comidas
- notas personales
- estadisticas de progreso

## Estructura real del proyecto

La raiz util del proyecto es esta carpeta:

- `F:\Olympus\Olympus`

La aplicacion esta organizada asi:

### Codigo Kotlin

- `app/src/main/java/com/example/olympus/activities`
  - pantallas principales de la app
  - ejemplos: `LoginActivity`, `HomeActivity`, `EntrenadorActivity`, `NutricionistaActivity`

- `app/src/main/java/com/example/olympus/fragments`
  - secciones de la pantalla principal del usuario
  - ejemplos: `RutinasFragment`, `NutricionFragment`, `NotasFragment`, `StatsFragment`

- `app/src/main/java/com/example/olympus/adapters`
  - adaptadores para RecyclerView y listas
  - incluye adaptadores cloud y locales

- `app/src/main/java/com/example/olympus/firebase`
  - acceso central a Firebase
  - contiene `FirebaseRepository.kt`

- `app/src/main/java/com/example/olympus/database`
  - capa SQLite heredada o de apoyo
  - helpers locales de rutinas, notas y nutricion

- `app/src/main/java/com/example/olympus/models`
  - modelos cloud y de dominio

- `app/src/main/java/com/example/olympus/utils`
  - utilidades como `SessionManager`

- `app/src/main/java/com/example/olympus`
  - catalogos estaticos como `EjerciciosData.kt` y `ComidaData.kt`

### Recursos XML

- `app/src/main/res/layout`
  - layouts de activities, fragments, dialogs e items

- `app/src/main/res/drawable`
  - fondos, iconos y recursos visuales

- `app/src/main/res/menu`
  - menus de acciones

- `app/src/main/res/values`
  - colores, textos y temas

- `app/src/main/res/xml`
  - reglas de backup y data extraction

### Configuracion del proyecto

- `app/build.gradle.kts`
- `build.gradle.kts`
- `settings.gradle.kts`
- `gradle/libs.versions.toml`
- `gradlew`
- `gradlew.bat`

## Como funciona la app a nivel tecnico

### 1. Autenticacion

Firebase Authentication gestiona:

- login
- registro
- restablecimiento de contrasena
- cambio de contrasena

### 2. Datos cloud

`FirebaseRepository.kt` concentra la logica principal de Firestore. En el codigo aparecen estas colecciones:

- `users`
- `roleRequests`
- `serviceRequests`
- `mail`
- `trainerAssignments`
- `nutritionistAssignments`
- `nutritionPlans`
- `nutritionPlans/{planId}/meals`
- `routines`
- `routines/{routineId}/exercises`
- `routines/{routineId}/exercises/{exerciseId}/sets`
- `notes`

### 3. Datos locales heredados

Todavia existe una capa `database/` con SQLite:

- `DatabaseHelper.kt`
- `RutinasDatabaseHelper.kt`
- `NotasDatabaseHelper.kt`
- `NutricionistaDatabaseHelper.kt`

Eso significa que el proyecto esta en una etapa mixta:

- parte moderna en Firebase
- parte heredada o complementaria en SQLite

## Lectura de las ramas actuales

Importante: una rama no debe contener solo una carpeta. Cada rama debe contener el proyecto completo, pero con cambios de una responsabilidad concreta.

La manera correcta de entender tus ramas actuales es esta:

### `main`

Rama estable y publicable.

Solo debe contener:

- lo que compila
- lo que ya esta probado
- lo que consideras version estable

No se trabaja directamente aqui salvo correcciones muy puntuales.

### `develop`

Rama de integracion.

Aqui se mezclan primero los cambios que vienen de ramas funcionales antes de pasar a `main`.

Esta rama debe servir para:

- juntar trabajo de varias ramas
- probar flujos completos
- detectar conflictos entre login, fragments, adapters, UI y Firebase

### `login`

Debe usarse para todo lo relacionado con acceso y sesion:

- `LoginActivity.kt`
- `RegisterActivity.kt`
- `ChangePasswordActivity.kt`
- `SessionManager.kt`
- layouts de login y registro

Tambien puede tocar `FirebaseRepository.kt` si el cambio es puramente de autenticacion.

### `fragments`

Debe usarse para cambios en las secciones del usuario:

- `RutinasFragment.kt`
- `NutricionFragment.kt`
- `NotasFragment.kt`
- `StatsFragment.kt`
- `GymsFragment.kt`

Tambien puede tocar adapters o layouts si el cambio es necesario para que el fragmento funcione.

### `adapters`

Debe agrupar cambios de listas y renderizado:

- adaptadores de rutinas
- adaptadores de series
- adaptadores de notas
- adaptadores de planes nutricionales
- adaptadores cloud
- `item_*.xml`

Buena rama para cambios visuales o de comportamiento de RecyclerView.

### `database`

Debe reservarse para la capa local heredada:

- helpers SQLite
- migraciones
- limpieza de codigo legacy
- reemplazo progresivo de logica local por logica cloud

Si el cambio es puramente Firestore, esta rama ya no es la mejor opcion.

### `interface`

Debe usarse para cambios puramente visuales:

- `layout`
- `drawable`
- `menu`
- `values`
- temas, colores, textos y apariencia

Si el cambio es solo estetico, esta es la rama correcta.

## Que rama falta crear

### Recomendacion principal

Si tu proyecto ya esta bastante movido a Firebase, falta una rama que represente esa realidad.

La mejor opcion es crear:

- `firebase`

o, si quieres un nombre mas claro:

- `feature/firebase-core`

### Por que hace falta

Porque ahora mismo la logica cloud toca muchas capas a la vez:

- autenticacion
- sesiones
- perfiles
- solicitudes
- rutinas
- planes nutricionales
- notas
- estadisticas

Eso no encaja bien solo en:

- `database`
- `fragments`
- `login`
- `adapters`
- `interface`

La rama `firebase` te serviria para cambios transversales de backend y flujo funcional.

## Que ramas NO hace falta crear ahora mismo

No hace falta crear mas ramas genericas si no hay una necesidad real.

Por ejemplo, de momento no es necesario abrir ramas tipo:

- `activities`
- `models`
- `utils`

porque eso fragmenta demasiado el proyecto.

Mejor mantener pocas ramas con sentido funcional.

## Organizacion recomendada desde ahora

Usa esta regla:

- `main` = estable
- `develop` = integracion
- ramas tematicas = trabajo real

### Propuesta clara

- `login` para auth y sesion
- `fragments` para secciones del usuario
- `adapters` para listas e items
- `interface` para UI visual
- `database` para legado SQLite
- `firebase` para cambios cloud grandes o transversales

## Cuando usar cada rama

Usa `login` si cambias:

- login
- registro
- cambio de contrasena
- reset password
- guardado de sesion

Usa `fragments` si cambias:

- tabs del home
- carga de datos en los fragmentos
- navegacion interna del usuario

Usa `adapters` si cambias:

- RecyclerView
- ViewHolder
- item XML
- orden o presentacion de listas

Usa `interface` si cambias:

- diseno
- colores
- drawables
- layouts
- menus
- strings

Usa `database` si cambias:

- SQLite
- helpers locales
- logica legacy

Usa `firebase` si cambias:

- `FirebaseRepository.kt`
- flujo de Firestore
- colecciones cloud
- asignaciones entre usuario y profesional
- rutinas cloud
- notas cloud
- nutricion cloud
- stats calculadas desde Firestore

## Flujo sano de trabajo en GitHub

### Flujo recomendado

1. Crear o reutilizar una rama funcional.
2. Hacer cambios solo relacionados con ese bloque.
3. Probar localmente.
4. Subir esa rama a GitHub.
5. Abrir Pull Request a `develop`.
6. Validar en `develop`.
7. Pasar a `main` solo cuando este estable.

### Regla practica

Nunca mezclar en la misma rama:

- cambios de Firebase grandes
- rediseno visual grande
- limpieza de SQLite

si no forman parte del mismo flujo funcional.

## Que subir manualmente a GitHub

Si vas a subir archivos manualmente, sube solo codigo fuente y configuracion necesaria del proyecto:

- `app/src/`
- `gradle/`
- `build.gradle.kts`
- `settings.gradle.kts`
- `gradle.properties`
- `gradlew`
- `gradlew.bat`
- `.gitignore`
- `README.md`
- `FIREBASE_SETUP.md`
- `FIREBASE_EMAIL_SETUP.md`

## Que NO subir manualmente

No subas:

- `.idea/`
- `.gradle/`
- `.kotlin/`
- `build/`
- `app/build/`
- `local.properties`
- APKs como `Olympus.apk`
- imagenes temporales o carpetas auxiliares como `imagenes_descargadas/`
- archivos generados por Android Studio

### Importante con Firebase

Como el repositorio es publico, lo mas prudente es NO subir:

- `app/google-services.json`

Aunque tecnicamente a veces se comparte en proyectos Android, en un repo publico es mejor evitarlo y documentar su configuracion en `FIREBASE_SETUP.md`.

## Resumen de subida manual

Si subes a mano desde Windows, piensa asi:

- sube el proyecto desde `F:\Olympus\Olympus`
- ignora todo lo compilado o local
- no subas secretos
- no subas APKs
- no subas carpetas de cache

## Recomendacion final para ordenar este repo

La organizacion mas limpia para tu caso es:

1. Mantener `main` como rama final estable.
2. Mantener `develop` como rama de mezcla.
3. Seguir usando `login`, `fragments`, `adapters`, `interface` y `database` para cambios concretos.
4. Crear `firebase` para toda la parte cloud transversal.
5. Subir siempre cambios funcionales por ramas, no carpetas sueltas.

## Resumen corto

La lectura mas realista del proyecto hoy es esta:

- la app ya no es solo local
- Firebase es parte central de la arquitectura
- `database` parece una capa heredada
- `firebase` deberia existir como rama propia
- `develop` debe ser la rama donde se integra todo antes de `main`

Si sigues esta estructura, sabras mejor:

- que trabajo va en cada rama
- que hace falta crear
- que no conviene crear
- y que archivos debes subir manualmente sin ensuciar el repo
