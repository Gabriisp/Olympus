# Firebase Setup

Este proyecto ya tiene integrada la capa de Firebase Auth + Firestore para:

- login y registro
- perfiles con rol
- asignaciones entrenador-cliente
- asignaciones nutricionista-cliente
- planes nutricionales y comidas

## 1. Conectar la app Android

1. Crea un proyecto en Firebase Console.
2. Añade una app Android con `applicationId` `com.example.olympus`.
3. Descarga `google-services.json`.
4. Copia el archivo en `app/google-services.json`.

El `build.gradle.kts` de `app` aplica automáticamente el plugin `google-services` si ese archivo existe.

## 2. Habilitar Authentication

En Firebase Console:

- Authentication
- Sign-in method
- habilitar `Email/Password`

## 3. Crear colecciones esperadas

### users

Documento por UID del usuario autenticado.

Campos:

- `legacyLocalId` number
- `name` string
- `email` string
- `role` string
- `createdAt` timestamp

Roles válidos esperados por la app:

- `Usuario`
- `Entrenador`
- `Nutricionista`

Nota: el registro público solo crea `Usuario`. Los perfiles profesionales deben crearse manualmente o desde un futuro panel admin.

### trainerAssignments

Documento sugerido: `{trainerUid}_{clientUid}`

Campos:

- `trainerUid` string
- `clientUid` string
- `createdAt` timestamp

### nutritionistAssignments

Documento sugerido: `{nutritionistUid}_{clientUid}`

Campos:

- `nutritionistUid` string
- `clientUid` string
- `createdAt` timestamp

### nutritionPlans

Documento automático por plan.

Campos:

- `nombre` string
- `descripcion` string
- `userUid` string
- `nutritionistUid` string
- `createdAt` timestamp

Subcolección `meals` dentro de cada plan:

- `tipo` string
- `nombre` string
- `descripcion` string
- `calorias` number
- `imagenId` number
- `orden` number
- `createdAt` timestamp

## 4. Reglas iniciales de Firestore

Reglas mínimas de ejemplo para desarrollo:

```text
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null;
    }

    match /trainerAssignments/{assignmentId} {
      allow read, write: if request.auth != null;
    }

    match /nutritionistAssignments/{assignmentId} {
      allow read, write: if request.auth != null;
    }

    match /nutritionPlans/{planId} {
      allow read, write: if request.auth != null;

      match /meals/{mealId} {
        allow read, write: if request.auth != null;
      }
    }
  }
}
```

Estas reglas son solo para arrancar. Después conviene endurecerlas por rol y por relación profesional-cliente.

## 5. Prueba mínima

1. Registra un usuario básico desde la app.
2. Crea manualmente en Firebase Auth un entrenador o nutricionista.
3. Crea su documento en `users` con el rol correcto.
4. Añade asignaciones en `trainerAssignments` o `nutritionistAssignments`.
5. Inicia sesión con ese profesional y comprueba que ve solo sus clientes asignados.
