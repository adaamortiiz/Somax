# Somax

Plataforma web para gestión de clases dirigidas (USER), control de asistencia (STAFF) y administración (ADMIN).

## Estructura

- `backend/` Spring Boot + Thymeleaf + JWT + H2
- `frontend/` reservado para futuras versiones standalone

## Requisitos

- JDK 21
- Maven Wrapper en `backend/`

## Ejecutar en local

Desde `backend/`:

```
./mvnw spring-boot:run
```

En Windows:

```
.\mvnw spring-boot:run
```

Abrir `http://localhost:8080`.

## Credenciales demo

- Admin: `admin@somax.com` / `Somax123`
- Staff: `staff@somax.com` / `Somax123`
- User: `user@somax.com` / `Somax123`

## Variables de entorno

Define estas variables (o ajusta `backend/src/main/resources/application.properties`):

- `APP_JWT_SECRET`
- `APP_STACKAI_API_KEY`, `APP_STACKAI_WORKFLOW_ID`
- `APP_CLOUDINARY_CLOUD_NAME`, `APP_CLOUDINARY_API_KEY`, `APP_CLOUDINARY_API_SECRET`
- `APP_FRONTEND_BASE_URL`
- SMTP (Gmail): `SMTP_HOST` (smtp.gmail.com), `SMTP_PORT` (587), `SMTP_USER`, `SMTP_PASS` (contraseña de aplicación), `MAIL_FROM_EMAIL`, `MAIL_FROM_NAME`

## Base de datos

H2 en modo archivo: `backend/data/somaxdb`.

## Despliegue en Render

Usa el `render.yaml` de la raíz. En Render:

1. Crea un servicio web desde el repo (Blueprint).
2. Rellena los `envVars` con valores reales (especialmente `APP_JWT_SECRET` y `SMTP_PASS`).
3. Mantén `SPRING_H2_CONSOLE_ENABLED=false`.

Nota: H2 en archivo es válido para demo, pero en Render el almacenamiento puede no ser persistente en reinicios/redeploys.
