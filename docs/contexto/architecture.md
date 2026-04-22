# Arquitectura Hexagonal - Enerlink

## Principios Fundamentales

1. **El dominio NO conoce la infraestructura**: El dominio define puertos (interfaces), la infraestructura implementa adaptadores.
2. **Dependencias hacia el núcleo**: Las capas externas dependen del dominio, nunca al revés.
3. **Puerto = Interfaz del dominio**: Define operaciones sin implementación.

## Capas del Sistema

### Capa 1: Dominio (Núcleo)

```
src/main/java/com/enerlink/enerlink/{modulo}/dominio/
```

**Responsabilidad**: Lógica de negocio pura. Sin dependencias Spring.

| Paquete | Contenido |
|---------|----------|
| `dominio.modelo` | Entidades del negocio |
| `dominio.puerto` | Interfaces de repositorio (Output Ports) |
| `dominio.servicio` | Servicios de dominio |
| `dominio.factory` | Fábricas de creación |
| `dominio.decorador` | Comportamientos extensibles |
| `dominio.proceso` | Procesos de negocio |

### Capa 2: Aplicación (Use Cases)

```
src/main/java/com/enerlink/enerlink/{modulo}/aplicacion/servicio/
```

**Responsabilidad**:Orquestar casos de uso. Depende del dominio.

- `UserService`
- `EnergyOfferService`
- `EnergyTradingFacade`
- `IoTDeviceService`

### Capa 3: Infraestructura (Adapters)

```
src/main/java/com/enerlink/enerlink/{modulo}/infraestructura/
```

**Responsabilidad**:Adaptadores externos

| Subpaquete | Tipo de Adapter |
|-----------|----------------|
| `controlador` | **Driving Adapter** (REST) |
| `persistencia` | **Driven Adapter** (JPA) |
| `adapter` | **Driven Adapter** (IoT Providers) |
| `configuracion` | **Configuration** (Beans Spring) |
| `dto` | DTOs para transferencia |
| `mapper` | Mapeadores DTO ↔ Entity |

## Puertos (Interfaces)

### Inbound Ports (Entradas)

| Puerto | Implementado Por | Ubicación |
|--------|---------------|----------|
| User Management | `UserController` | `usuario/infraestructura/controlador` |
| Energy Offers | `EnergyOfferController` | `energia/infraestructura/controlador` |
| IoT Devices | `IoTDeviceController` | `iot/infraestructura/controlador` |

### Outbound Ports (Salidas)

| Puerto | Definido En | Implementado Por |
|--------|------------|---------------|
| `UserRepositoryPort` | `usuario.dominio.puerto` | `UserRepositoryAdapter` |
| `EnergyOfferRepositoryPort` | `energia.dominio.puerto` | `EnergyOfferRepositoryAdapter` |
| `IoTDataPort` | `iot.dominio.puerto` | `SmartHomeAdapter`, `EnergyCloudAdapter`, `IoTDataPortComposite` |

## Dirección de Dependencias

```
┌────────────────────────────────────────────────────┐
│  CONTROLADORES (Infraestructura)        │
│  @RestController                    │
└────────────────┬───────────────────┘
                 │ depends on
                 ▼
┌────────────────────────────────────────────────────┐
│  SERVICIOS (Aplicación)                   │
│  @Service                          │
└────────────────┬───────────────────┘
                 │ depends on
                 ▼
┌────────────────────────────────────────────────────┐
│  DOMINIO (Núcleo)                     │
│  - Modelos (Entidades)                │
│  - Puertos (Interfaces)             │
│  - Servicios de dominio            │
└────────────────┬──���────────────────┘
                 │ implemented by
                 ▼
┌────────────────────────────────────────────────────┐
│  ADAPTADORES (Infraestructura)          │
│  - JPA Repositories               │
│  - IoT Clients                  │
└────────────────┬───────────────────┘
```

## Comunicación entre Capas

### Flujo Típico (CREATE usuario)

```
HTTP POST /api/usuarios
        │
        ▼ UserController (RestController)
        │ @PostMapping → userService.crearUsuario()
        ▼ UserService (Application)
        │ - Busca Factory por rol
        │ - Factory Method → crear usuario
        │ - UserRepositoryPort.guardar()
        ▼ User (Domain)
        │ - Entidad User
        ▼ UserRepositoryAdapter (Driven Adapter)
        │ - JpaRepository.save()
        ▼ BDD H2
```

### Módulo: usuario

```
Dominio:
  ├── modelo/User (abstracto)
  │     ├── ProducerUser
  │     ├── ConsumerUser
  │     └── MixedUser
  ├── puerto/UserRepositoryPort (INTERFAZ)
  └── factory/UserFactory (INTERFAZ)

Aplicación:
  └── UserService

Infraestructura:
  ├── controlador/UserController
  ├── persistencia/UserRepositoryAdapter (IMPLEMENTS UserRepositoryPort)
  └── factory/{Producer,Consumer,Mixed}UserFactory
```

### Módulo: energia

```
Dominio:
  ├── modelo/
  │     ├── EnergyOffer (Prototype)
  │     ├── Transaction (Fluent Builder)
  │     ├── Auction
  │     └── SaleType (enum)
  ├── puerto/EnergyOfferRepositoryPort
  ├── factory/EnergySaleFactory (Abstract Factory)
  │     ├── DirectSaleFactory
  │     └── AuctionSaleFactory
  ├── decorador/TransactionDecorator (Chain)
  │     ├── ValidatingTransactionDecorator
  │     ├── FeeTransactionDecorator
  │     └── AuditingTransactionDecorator
  └── proceso/SaleProcess
       ├── DirectSaleProcess
       └── AuctionSaleProcess

Aplicación:
  ├── EnergyOfferService
  └── EnergyTradingFacade (Facade)

Infraestructura:
  ├── controlador/EnergyOfferController
  └── persistencia/EnergyOfferRepositoryAdapter
```

### Módulo: iot

```
Dominio:
  ├── modelo/IoTDeviceData
  ├── modelo/DeviceType (enum)
  └── puerto/IoTDataPort (INTERFAZ)

Aplicación:
  └── IoTDeviceService

Infraestructura:
  ├── controlador/IoTDeviceController
  ├── adapter/
  │     ├── SmartHomeAdapter (implements IoTDataPort)
  │     ├── EnergyCloudAdapter (implements IoTDataPort)
  │     ├── IoTDataPortComposite (Composite)
  │     └── CachingIoTDataProxy (Proxy - caching)
  ├── configuracion/IoTAdapterConfig (@Configuration)
  └── adapter/{SelectionStrategy} (Strategy)
```

## Inyección de Dependencias

### Constructor Injection (Recomendado)

```java
public class UserService {
    private final UserRepositoryPort userRepositoryPort;
    private final Map<String, UserFactory> userFactories;

    public UserService(
            UserRepositoryPort userRepositoryPort,
            Map<String, UserFactory> userFactories) {
        this.userRepositoryPort = userRepositoryPort;
        this.userFactories = userFactories;
    }
}
```

### Spring Boot Scan

```properties
# application.properties
spring.jpa.hibernate.ddl-auto=update
```

Paquetes escaneados:
- `com.enerlink.enerlink.usuario`
- `com.enerlink.enerlink.energia`
- `com.enerlink.enerlink.iot`
- `com.enerlink.enerlink.configuracion`

## Configuración de Beans

### application.properties

```properties
spring.application.name=enerlink
spring.datasource.url=jdbc:h2:file:./data/enerlinkdb
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
```