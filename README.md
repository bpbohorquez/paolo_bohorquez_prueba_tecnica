# API de Gestión de Pólizas — Prueba Técnica — Paolo Bohorquez Sanchez

Implementación del caso técnico: **API de Gestión de Pólizas** con Spring Boot, con base en arquitectura de microservicios y patrón hexagonal, separación por capas (`controller` → `service` → `repository`) e integración con el CORE transaccional legado detrás de un puerto/adapter.

## Stack técnico

- Java 17
- Spring Boot 3.2.5 (Web, Data JPA, Validation)
- H2 (base de datos de prueba en memoria, se reinicia en cada arranque)
- Lombok
- JUnit 5 + Mockito + AssertJ (pruebas unitarias)
- Maven (incluye Maven Wrapper: `mvnw` / `mvnw.cmd`, no requiere Maven instalado localmente)

## Estructura del proyecto

```
api-polizas/
├── pom.xml
├── mvnw, mvnw.cmd, .mvn/
└── src/
    ├── main/java/com/pruebatecnica/polizas/
    │   ├── PolizasApiApplication.java
    │   ├── domain/            # Entidades JPA: Poliza, Riesgo + enums (TipoPoliza, EstadoPoliza, EstadoRiesgo)
    │   ├── repository/        # PolizaRepository, RiesgoRepository (Spring Data JPA)
    │   ├── dto/                # Objetos de entrada/salida
    │   ├── service/            # PolizaService, RiesgoService (lógica del negocio)
    │   │   └── core/           # Puerto (CoreNotificationPort) + adapter mock (CoreMockAdapter)
    │   ├── controller/         # PolizaController, RiesgoController, CoreMockController
    │   ├── security/           # ApiKeyFilter (header x-api-key)
    │   ├── exception/          # Excepciones de negocio + manejador global (@RestControllerAdvice)
    │   └── config/             # DataSeeder (datos de prueba al arrancar)
    └── test/java/...           # Pruebas unitarias de servicios + test de contexto
```

## Modelo de datos

- **Póliza**: `tipo` (INDIVIDUAL/COLECTIVA), `estado` (ACTIVA/RENOVADA/CANCELADA), `tomador`, vigencia (`fechaInicioVigencia`, `fechaFinVigencia`, `mesesVigencia`), `canonMensual`, `prima`.
- **Riesgo**: Entidad hija de `Poliza` (relación `1—N`). Contiene `asegurado` (arrendatario), `beneficiario` (arrendador), `inmueble` y `estado` (ACTIVO/CANCELADO).

## Endpoints

Todos los endpoints (incluyendo el mock del CORE) exigen el header:

```
x-api-key: 123456
```

En caso de que el header sea incorrecto, la API devuelve el error `401 Unauthorized` antes de llegar al controlador.

| Método | Ruta                     | Descripción                                                                                                                 |
| ------ | ------------------------ | --------------------------------------------------------------------------------------------------------------------------- |
| GET    | `/polizas?tipo=&estado=` | Lista pólizas, con filtros opcionales por `tipo` (`INDIVIDUAL`\|`COLECTIVA`) y `estado` (`ACTIVA`\|`RENOVADA`\|`CANCELADA`) |
| GET    | `/polizas/{id}/riesgos`  | Lista los riesgos de una póliza                                                                                             |
| POST   | `/polizas/{id}/renovar`  | Renueva la póliza: incrementa canon/prima en +IPC y pasa a `RENOVADA`                                                       |
| POST   | `/polizas/{id}/cancelar` | Cancela la póliza y todos sus riesgos                                                                                       |
| POST   | `/polizas/{id}/riesgos`  | Agrega un riesgo (solo si `tipo = COLECTIVA`)                                                                               |
| POST   | `/riesgos/{id}/cancelar` | Cancela un riesgo puntual                                                                                                   |
| POST   | `/core-mock/evento`      | Mock del servicio agnóstico de edición (CORE Weblogic)                                                                      |

### Lógica del negocio aplicada

- Una póliza **individual** solo puede tener 1 riesgo (solo las pólizas **colectivas** aceptan `POST /polizas/{id}/riesgos`).
- No se puede renovar una póliza `CANCELADA`.
- Cancelar una póliza cancela **todos** sus riesgos activos.
- Agregar un riesgo valida el tipo de póliza (`COLECTIVA`) y que la póliza no esté cancelada.
- Cada acción que cambia el estado de una póliza o riesgo (renovar, cancelar, agregar riesgo, cancelar riesgo) invoca `CoreNotificationPort.notificar(...)`, cuya implementación mock (`CoreMockAdapter`) deja constancia en el log de que la operación se intentó enviar al CORE transaccional. El endpoint `/core-mock/evento` expone ese mismo componente por HTTP para poder probarlo manualmente con el payload de ejemplo suministrado.

### Ejemplos (PowerShell / `Invoke-RestMethod`)

```powershell
$h = @{ "x-api-key" = "123456" }

# Listar pólizas colectivas activas (GET /polizas)
Invoke-RestMethod -Uri "http://localhost:8080/polizas?tipo=COLECTIVA&estado=ACTIVA" -Headers $h

# Listar los riesgos de la póliza 2 (GET /polizas/{id}/riesgos)
Invoke-RestMethod -Uri "http://localhost:8080/polizas/2/riesgos" -Headers $h

# Renovar la póliza 1 (POST /polizas/{id}/renovar)
Invoke-RestMethod -Uri "http://localhost:8080/polizas/1/renovar" -Method Post -ContentType "application/json" -Headers $h

# Cancelar la póliza 2 (cancela también todos sus riesgos) (POST /polizas/{id}/cancelar)
Invoke-RestMethod -Uri "http://localhost:8080/polizas/2/cancelar" -Method Post -ContentType "application/json" -Headers $h

# Agregar un riesgo a la póliza colectiva 2 (POST /polizas/{id}/riesgos)
Invoke-RestMethod -Uri "http://localhost:8080/polizas/2/riesgos" -Method Post -ContentType "application/json" -Headers $h `
  -Body '{"asegurado":"Nuevo Arrendatario","beneficiario":"Nuevo Arrendador","inmueble":"Apto 900"}'

# Cancelar el riesgo 1 (POST /riesgos/{id}/cancelar)
Invoke-RestMethod -Uri "http://localhost:8080/riesgos/1/cancelar" -Method Post -ContentType "application/json" -Headers $h

# Mock del CORE (POST /core-mock/evento)
Invoke-RestMethod -Uri "http://localhost:8080/core-mock/evento" -Method Post -ContentType "application/json" -Headers $h `
  -Body '{"evento":"ACTUALIZACION","polizaId":555}'
```

Equivalente en `curl`:

```bash
# Listar pólizas colectivas activas (GET /polizas)
curl -H "x-api-key: 123456" "http://localhost:8080/polizas?tipo=COLECTIVA&estado=ACTIVA"

# Listar los riesgos de la póliza 2 (GET /polizas/{id}/riesgos)
curl -H "x-api-key: 123456" "http://localhost:8080/polizas/2/riesgos"

# Renovar la póliza 1 (POST /polizas/{id}/renovar)
curl -X POST -H "x-api-key: 123456" -H "Content-Type: application/json" "http://localhost:8080/polizas/1/renovar"

# Cancelar la póliza 2 (cancela también todos sus riesgos) (POST /polizas/{id}/cancelar)
curl -X POST -H "x-api-key: 123456" -H "Content-Type: application/json" "http://localhost:8080/polizas/2/cancelar"

# Agregar un riesgo a la póliza colectiva 2 (POST /polizas/{id}/riesgos)
curl -X POST -H "x-api-key: 123456" -H "Content-Type: application/json" \
  -d '{"asegurado":"Nuevo Arrendatario","beneficiario":"Nuevo Arrendador","inmueble":"Apto 900"}' \
  "http://localhost:8080/polizas/2/riesgos"

# Cancelar el riesgo 1 (POST /riesgos/{id}/cancelar)
curl -X POST -H "x-api-key: 123456" -H "Content-Type: application/json" "http://localhost:8080/riesgos/1/cancelar"

# Mock del CORE (POST /core-mock/evento)
curl -X POST -H "x-api-key: 123456" -H "Content-Type: application/json" \
  -d '{"evento":"ACTUALIZACION","polizaId":555}' \
  "http://localhost:8080/core-mock/evento"
```

## Cómo ejecutar

Requiere JDK 17 instalado y verificar las variables de entorno asociadas:

```powershell
# Verificar
$env:JAVA_HOME

# Configurar
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.x.x-hotspot"

```

Ejecutar el sistema:

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

La API se ejecuta en `http://localhost:8080`.

### Ejecutar endpoints

```bash
# PowerShell
Invoke-RestMethod -Uri "http://localhost:8080/polizas?tipo=COLECTIVA&estado=ACTIVA" -Headers $h
...

# curl
curl -H "x-api-key: 123456" "http://localhost:8080/polizas?tipo=COLECTIVA&estado=ACTIVA"
...
```

### Ejecutar las pruebas

```bash
.\mvnw.cmd test        # Windows
./mvnw test             # Linux / macOS
```

## Datos de prueba (seed)

Al arrancar la aplicación, `DataSeeder` inserta 3 pólizas para poder probar los endpoints

| id  | tipo       | estado    | riesgos       |
| --- | ---------- | --------- | ------------- |
| 1   | INDIVIDUAL | ACTIVA    | 1 (activo)    |
| 2   | COLECTIVA  | ACTIVA    | 2 (activos)   |
| 3   | INDIVIDUAL | CANCELADA | 1 (cancelado) |

## Seguridad

Se implementó un `Filter` (`ApiKeyFilter`) que valida el header `x-api-key` en cada petición, en un entorno productivo esto se reforzaría con OAuth2/JWT y control de acceso en el API Gateway.
