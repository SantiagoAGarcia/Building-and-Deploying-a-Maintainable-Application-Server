# Java Web Framework — Maintainable Application Server

## Descripción del proyecto

Framework HTTP mínimo y secuencial en Java que permite:

- Servir archivos estáticos (HTML, CSS, JS, imágenes) desde `src/main/resources/webroot`.
- Registrar rutas GET dinámicas mediante lambdas (`get("/ruta", (req, resp) -> ...)`).
- Leer parámetros de query string desde `req.getValue("param")`.
- Configurar el puerto y otras variables por entorno (`PORT`, `GREETING_PREFIX`, `APP_ENV`).
- Apagar el servidor de forma controlada (`/shutdown`, solo en desarrollo).

## Arquitectura

```
Application
    Registra rutas y configuración
        ↓
WebFramework (API pública: get, staticfiles, start, stop)
        ↓
Router
    Asocia método+ruta con un lambda handler
        ↓
HttpServer
    Acepta conexiones, parsea el request, arma la respuesta
        ↓
Request / Response
    Abstracciones del protocolo HTTP
        ↓
StaticFileService
    Sirve recursos cuando ninguna ruta dinámica coincide
```

### Responsabilidades de los componentes principales

| Componente | Responsabilidad |
|---|---|
| `WebFramework` | API pública usada por el desarrollador de la aplicación (`get`, `staticfiles`, `start`, `stop`). |
| `Router` | Guarda el mapa ruta → lambda y resuelve si una ruta dinámica existe. |
| `HttpServer` | Bucle secuencial de conexión: acepta un socket, parsea el request, decide entre ruta dinámica o archivo estático, escribe la respuesta. |
| `Request` | Expone método, path y parámetros de query string. |
| `Response` | Permite a los handlers configurar código de estado y content-type. |
| `StaticFileService` | Lee recursos (texto y binarios) desde el classpath y resuelve su content-type. |
| `Application` | Aplicación de ejemplo que registra rutas y arranca el servidor. |

### Metáfora: el servidor como un edificio de oficinas

| Elemento del edificio | Componente del framework |
|---|---|
| Entrada y recepcionista | `HttpServer`: recibe visitantes (conexiones) y sus solicitudes. |
| Directorio del lobby | `Router`: indica a qué oficina debe dirigirse cada solicitud. |
| Oficinas individuales | Lambdas registradas con `get()`: atienden un servicio específico. |
| Archivo/bodega de documentos | `StaticFileService`: entrega HTML, CSS, JS e imágenes ya existentes. |
| Reglamento del edificio | Variables de entorno: puerto, ambiente, rutas configurables. |
| Procedimiento de cierre | Apagado controlado: se atiende al visitante actual y luego se cierra la puerta. |

## Cómo ejecutar localmente

Requisitos: JDK 17+, Maven 3.8+.

```bash
mvn clean package
java -jar target/webframework.jar
```

Por defecto el servidor escucha en `http://localhost:8080`.

Rutas de prueba:

- `http://localhost:8080/index.html`
- `http://localhost:8080/hello?name=Pedro`
- `http://localhost:8080/pi`
- `http://localhost:8080/images/logo.png`
- `http://localhost:8080/shutdown` (solo si `APP_ENV` no está en `production`)

## Variables de entorno

| Variable | Propósito | Valor local por defecto |
|---|---|---|
| `PORT` | Puerto HTTP del servidor | `8080` |
| `GREETING_PREFIX` | Prefijo usado por la ruta `/hello` | `Hello` |
| `APP_ENV` | Ambiente de ejecución (`development` habilita `/shutdown`) | `development` |

## Despliegue en la nube

> Pendiente de completar tras el despliegue: plataforma usada, URL pública,
> variables de entorno configuradas y evidencia (capturas) de:
> - la página estática funcionando,
> - al menos dos endpoints REST respondiendo,
> - variables de entorno configuradas (sin exponer secretos),
> - `/shutdown` funcionando en local,
> - `/shutdown` NO disponible en producción.

## Por qué esta arquitectura es mantenible

La infraestructura HTTP (`HttpServer`) está separada del comportamiento de la
aplicación (lambdas registradas en `Router`). Agregar una ruta nueva no
requiere modificar el bucle de conexión ni ningún otro componente existente:
solo se llama a `get(...)` desde `Application`. Cada clase tiene una única
responsabilidad (parseo de request, resolución de rutas, archivos estáticos),
lo que facilita probar cada parte por separado y reutilizar el mismo artefacto
tanto en local como en la nube, cambiando solo variables de entorno.

## Pruebas realizadas

- `mvn test` ejecuta pruebas unitarias sobre el parseo de query strings (`RequestTest`).
- Pruebas manuales pendientes de documentar con capturas: peticiones exitosas
  a `/hello`, `/pi`, archivos estáticos, y una petición a una ruta inexistente
  verificando la respuesta `404 Not Found`.
