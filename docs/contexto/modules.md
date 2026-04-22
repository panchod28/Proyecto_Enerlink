# Módulos del Sistema - Enerlink

## Módulo: usuario

### Responsabilidad

Gestión del ciclo de vida de usuarios del sistema. Maneja tres tipos de roles y usa el patrón **Factory Method** para crear el tipo de usuario correcto según el rol.

### Entidades del Dominio

| Entidad | Tipo | Descripción |
|---------|------|-------------|
| `User` | Abstract | Clase base abstracta |
| `ProducerUser` | Concrete | Usuario que produce energía |
| `ConsumerUser` | Concrete | Usuario que consume energía |
| `MixedUser` | Concrete | Usuario con ambos roles |

**Campos de User:**
- `id`: Long (identificador)
- `nombre`: String
- `email`: String
- `rol`: String ("PRODUCER", "CONSUMER", "MIXED")

### Casos de Uso

| Caso de Uso | Método | Descripción |
|-------------|--------|-------------|
| Crear usuario | `crearUsuario(nombre, email, rol)` | Crea nuevo usuario |
| Listar usuarios | `listarUsuarios()` | Retorna todos |
| Obtener por ID | `obtenerUsuarioPorId(id)` | Busca por identificador |
| Actualizar usuario | `actualizarUsuario(id, nombre, email, rol)` | Actualiza datos |
| Eliminar usuario | `eliminarUsuario(id)` | Elimina por ID |

### Relaciones con Otros Módulos

- **energia**: `EnergyOffer` tiene referencia a `User` (producer)
- El módulo usuario es **dependido** por energia

---

## Módulo: energia

### Responsabilidad

Gestión del marketplace de energía. Maneja la creación de ofertas, procesos de venta (directa y subasta), y transacciones. Implementa múltiples patrones: **Prototype**, **Decorator**, **Abstract Factory**, **Facade**.

### Entidades del Dominio

| Entidad | Tipo | Descripción |
|---------|------|-------------|
| `EnergyOffer` | Domain Model | Oferta de energía (implementa Prototype) |
| `Transaction` | Domain Model | Transacción de energía (inmutable, Builder) |
| `Auction` | Domain Model | Subasta activa |
| `SaleType` | Enum | Tipo de venta: DIRECT, AUCTION |

**Campos de EnergyOffer:**
- `id`: Long
- `producer`: User
- `kwh`: double (cantidad de energía)
- `price`: double (precio por kWh)
- `saleType`: SaleType

**Campos de Transaction:**
- `id`: Long
- `offer`: EnergyOffer
- `buyer`: User
- `seller`: User
- `kwh`: double
- `price`: double
- `timestamp`: LocalDateTime

### Procesos de Venta

| Proceso | Clase | Descripción |
|---------|-------|-------------|
| Venta Directa | `DirectSaleProcess` | Precio fijo, transacción inmediata |
| Subasta | `AuctionSaleProcess` | Precio variable, pujas múltiples |

### Decoradores de Transacción

El módulo implementa una **cadena de decoradores**:

| Decorador | Función |
|-----------|---------|
| `ValidatingTransactionDecorator` | Valida la transacción |
| `FeeTransactionDecorator` | Aplica comisiones |
| `DiscountedTransactionDecorator` | Aplica descuentos |
| `AuditingTransactionDecorator` | Registra auditoría |

### Casos de Uso

| Caso de Uso | Método | Descripción |
|-------------|--------|-------------|
| Publicar oferta | `publishOffer(saleType, producerId, kwh, price)` | Crea nueva oferta |
| Venta directa | `executeDirectSale(offerId, buyer)` | Ejecuta venta directa |
| Subasta | `executeAuction(offerId, buyer, bidAmount)` | Ejecuta puja |
| Listar ofertas | `getActiveOffers()` | Retorna todas las ofertas |
| Obtener oferta | `getOfferById(id)` | Busca oferta por ID |
| Eliminar oferta | `deleteOffer(id)` | Elimina oferta |

### Relaciones con Otros Módulos

- **usuario**: Usa User como producer y buyer
- **iot**: No tiene relación directa

---

## Módulo: iot

### Responsabilidad

Integración con dispositivos IoT de múltiples proveedores. Usa patrones **Adapter**, **Composite**, **Bridge**, y **Proxy** (caching).

### Entidades del Dominio

| Entidad | Tipo | Descripción |
|---------|------|-------------|
| `IoTDeviceData` | Domain Model | Datos de un dispositivo |
| `DeviceType` | Enum | Tipo: SENSOR, ACTUATOR, GATEWAY |

**Campos de IoTDeviceData:**
- `deviceId`: String
- `deviceName`: String
- `deviceType`: DeviceType
- `currentReading`: double
- `unit`: String
- `location`: String
- `status`: String
- `timestamp`: long

### Adaptadores (Proveedores)

| Adaptador | Proveedor | Descripción |
|-----------|----------|-------------|
| `SmartHomeAdapter` | SmartHome | Adaptador para proveedor SmartHome |
| `EnergyCloudAdapter` | EnergyCloud | Adaptador para proveedor EnergyCloud |
| `IoTDataPortComposite` | Composite | Agrega múltiples adaptadores |

### Proxy (Caching)

`CachingIoTDataProxy` implementa cache en memoria:
- **TTL**: 5 minutos
- **Keys**: device:{id}, location:{loc}, type:{type}, all
- **Thread-safe**: ConcurrentHashMap + ReentrantLock

### Casos de Uso

| Caso de Uso | Método | Descripción |
|-------------|--------|-------------|
| Obtener dispositivo | `getDeviceData(deviceId)` | Fetch datos de dispositivo |
| Obtener todos | `getAllDevices()` | Lista todos los dispositivos |
| Por ubicación | `getDevicesByLocation(location)` | Filtra por ubicación |
| Por tipo | `getDevicesByType(deviceType)` | Filtra por tipo |

### Relaciones con Otros Módulos

- **usuario**: No tiene relación
- **energia**: No tiene relación

---

## Módulo: configuracion

### Responsabilidad

Servicios transversales que no pertenecen a un módulo específico.

### Servicios

| Servicio | Patrón | Descripción |
|----------|--------|-------------|
| `PredictionEngine` | Singleton (Enum) | Predicción de consumo energético |

### PredictionEngine

```java
public double predict(double consumoActual) {
    // Factor base: 1.15
    // Variación: -5% a +5%
    return consumoActual * (1.15 + variacionAleatoria);
}
```

**Uso actual**: Solo en `UserService` al crear usuario.

---

## Resumen de Dependencias

```
                    usuario
                        │
                        ▼
                    energia ──────► (depende de usuario)
                        │
                    (no depende de iot)
                        │
                    iot
                        │
                        ▼
                 configuracion
                 (independiente)
```

**Regla**: energia depende de usuario. Ningún otro módulo depende de otro.