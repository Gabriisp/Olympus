# Firebase Email Setup

La app ya no abre Gmail en el movil para las solicitudes profesionales.

Ahora hace dos cosas al enviar una solicitud:

1. Guarda la solicitud en `roleRequests`.
2. Crea un documento en la coleccion `mail`.

Ese documento se crea con este formato:

- `to`: `["tuyoloxd@gmail.com"]`
- `replyTo`: correo del solicitante
- `message.subject`: `Nuevo usuario [rol] que quiere registrarse: [correo]`
- `message.text`: `Nuevo usuario [rol] que quiere registrarse: [correo]`

## Lo que falta en Firebase

Para que llegue un correo real a `tuyoloxd@gmail.com`, necesitas instalar la extension oficial `Trigger Email` de Firebase y conectarla a un proveedor SMTP.

Pasos recomendados:

1. Cambia el proyecto de `Spark` a `Blaze`.
2. En Firebase Console entra en `Extensions`.
3. Instala `Trigger Email`.
4. Indica que la extension observe la coleccion `mail`.
5. Configura un proveedor SMTP.

## Nota sobre Gmail

Si quieres usar Gmail como proveedor SMTP, necesitaras una cuenta de Google con contrasena de aplicacion.

Cuando la extension este instalada, cada nueva solicitud profesional enviada desde la app llegara automaticamente a `tuyoloxd@gmail.com` sin abrir Gmail en Android.
