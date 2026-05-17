# Somax

Plataforma web para gestion de clases dirigidas y asesoramiento con IA.

## Estructura

- backend/ Spring Boot + Thymeleaf + JWT
- frontend/ reservado para futuras versiones standalone
- docs/ documentacion auxiliar

## Requisitos

- JDK 25 instalado
- Maven Wrapper incluido en `backend/`

## Ejecutar en local

Desde la carpeta backend:

```
./mvnw spring-boot:run
```

En Windows:

```
.\mvnw spring-boot:run
```

Abrir http://localhost:8080

## Credenciales demo

- Admin: admin@somax.com / Somax123
- Staff: staff@somax.com / Somax123
- User: user@somax.com / Somax123

## Configuracion externa

Edita `backend/src/main/resources/application.properties` o define variables de entorno equivalentes y reemplaza:

- app.jwt.secret
- app.stackai.\*
- app.sendgrid.\*
- app.cloudinary.\*
- app.frontend.base-url

## Base de datos

H2 en modo archivo persistente: `./data/somaxdb`

## Despliegue

El proyecto está preparado para desplegarse como una aplicación Java en Render usando el `render.yaml` de la raíz. Para publicarlo correctamente:

1. Sube el repositorio a GitHub.
2. Crea el servicio web en Render usando `render.yaml`.
3. Define las variables reales de entorno: `APP_JWT_SECRET`, `APP_STACKAI_API_KEY`, `APP_STACKAI_WORKFLOW_ID`, `APP_SENDGRID_API_KEY`, `APP_CLOUDINARY_*` y `APP_FRONTEND_BASE_URL`.
4. Mantén `SPRING_H2_CONSOLE_ENABLED=false` en despliegue.
5. Ten en cuenta que H2 en archivo sirve bien para demo y pruebas, pero en un PaaS como Render el almacenamiento no es una base de datos gestionada; si el contenedor se recrea, puedes perder datos.

## Observaciones

- La consola H2 solo debería usarse en local.
- La URL base del frontend debe apuntar al dominio real publicado, no a `localhost`.
- No hay tests automáticos en el repo, así que la validación principal es compilación y recorrido manual.
