# Modelos de Datos - Enerlink

## Entidades del Dominio vs DTOs

### Dominio (Core Business)

| Entidad | Módulo | Descripción |
|---------|--------|-------------|
| User | usuario | Usuario del sistema |
| EnergyOffer | energia | Oferta de energía |
| Transaction | energia | Transacción energética |
| Auction | energia | Subasta activa |
| IoTDeviceData | iot | Datos de dispositivo IoT |

### DTOs (Data Transfer Objects)

| DTO | Ubicación | Uso |
|-----|----------|-----|
| UserRequest | usuario/infraestructura/controlador | Request para crear/actualizar usuario |
| EnergyOfferRequest | energia/infraestructura/controlador | Request para crear oferta |
| SmartHomeProviderDTO | iot/infraestructura/dto | DTO del proveedor SmartHome |
| EnergyCloudProviderDTO | iot/infraestructura/dto | DTO del proveedor EnergyCloud |

---

## User (Dominio)

**Ubicación**: `com.enerlink.enerlink.usuario.dominio.modelo.User`

**Tipo**: Clase abstracta

```java
public abstract class User {
    protected Long id;
    protected String nombre;
    protected String email;
    protected String rol;  // "PRODUCER", "CONSUMER", "MIXED"
}
```

### Subclases

| Clase | Descripción |
|-------|------------|
| ProducerUser | Usuario productor de energía |
| ConsumerUser | Usuario consumidor de energía |
| MixedUser | Usuario con ambos roles |

### Validaciones

| Campo | Tipo | Requerido | Validación |
|-------|------|-----------|------------|
| id | Long | No | Auto-generado |
| nombre | String | Sí | No vacío |
| email | String | Sí | Formato email |
| rol | String | Sí | PRODUCER, CONSUMER, o MIXED |

---

## UserRequest (DTO)

**Ubicación**: `com.enerlink.enerlink.usuario.infraestructura.controlador.UserRequest`

```java
public class UserRequest {
    private String nombre;
    private String email;
    private String rol;
}
```

---

## EnergyOffer (Dominio)

**Ubicación**: `com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer`

**Tipo**: Clase que implementa Prototype

```java
public class EnergyOffer implements Prototype<EnergyOffer> {
    private Long id;
    private User producer;
    private double kwh;
    private double price;
    private SaleType saleType;
}
```

### Relaciones

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | Identificador único |
| producer | User | Usuario que ofrece energía |
| kwh | double | Cantidad de energía (kWh) |
| price | double | Precio por kWh |
| saleType | SaleType | Tipo de venta |

---

## Prototype (Interfaz)

**Ubicación**: `com.enerlink.enerlink.energia.dominio.modelo Prototype`

**Tipo**: Interfaz genérica

```java
public interface Prototype<T> {
    T clone();
    T shallowClone();
}
```

**Implementaciones**:
- `EnergyOffer` - implementa Prototype<EnergyOffer>

---

## Auction (Dominio)

**Ubicación**: `com.enerlink.enerlink.energia.dominio.modelo.Auction`

**Tipo**: Clase (detalles incompletos en código)

```java
public class Auction {
    private Long id;
    private EnergyOffer offer;
    private double highestBid;
    // bidders...
}
```

**Relaciones**:
- Association con EnergyOffer

---

## Transaction (Dominio)

**Ubicación**: `com.enerlink.enerlink.energia.dominio.modelo.Transaction`

**Tipo**: Clase inmutable (usa Fluent Builder)

```java
public final class Transaction {
    private final Long id;
    private final EnergyOffer offer;
    private final User buyer;
    private final User seller;
    private final double kwh;
    private final double price;
    private final LocalDateTime timestamp;
}
```

**Método calculado**:
```java
public double getTotalAmount() {
    return kwh * price;
}
```

**Relaciones**:

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | Identificador de transacción |
| offer | EnergyOffer | Oferta asociada |
| buyer | User | Usuario comprador |
| seller | User | Usuario vendedor (producer) |
| kwh | double | Cantidad de energía |
| price | double | Precio final |
| timestamp | LocalDateTime | Fecha/hora de la transacción |

**totalAmount** es calculado, no almacenado.

---

## TransactionComponent (Interfaz)

**Ubicación**: `com.enerlink.enerlink.energia.dominio.componente.TransactionComponent`

**Tipo**: Interfaz para el patrón Decorator

```java
public interface TransactionComponent {
    Long getId();
    EnergyOffer getOffer();
    User getBuyer();
    User getSeller();
    double getKwh();
    double getPrice();
    double getTotalAmount();
    LocalDateTime getTimestamp();
}
```

**Uso**: Implementada por decoradores (ValidatingTransactionDecorator, FeeTransactionDecorator, etc.)

---

## SaleType (Enum)

**Ubicación**: `com.enerlink.enerlink.energia.dominio.modelo.SaleType`

```java
public enum SaleType {
    DIRECT,    // Venta directa
    AUCTION   // Subasta
}
```

---

## SaleType (Enum)

**Ubicación**: `com.enerlink.enerlink.energia.infraestructura.controlador.EnergyOfferRequest`

```java
public class EnergyOfferRequest {
    private SaleType saleType;
    private Long producerId;
    private double kwh;
    private double price;
    private Long buyerId;  // Opcional
}
```

### Validaciones

| Campo | Tipo | Requerido | Validación |
|-------|------|-----------|------------|
| saleType | SaleType | Sí | Enum válido |
| producerId | Long | Sí | Debe existir usuario |
| kwh | double | Sí | Mayor a 0 |
| price | double | Sí | Mayor a 0 |
| buyerId | Long | No | Usuario comprador |

---

## IoTDeviceData (Dominio)

**Ubicación**: `com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData`

```java
public class IoTDeviceData {
    private String deviceId;
    private String deviceName;
    private DeviceType deviceType;
    private double currentReading;
    private String unit;
    private String location;
    private String status;
    private long timestamp;
}
```

### Validaciones

| Campo | Tipo | Descripción |
|-------|------|-------------|
| deviceId | String | Identificador del dispositivo |
| deviceName | String | Nombre descriptivo |
| deviceType | DeviceType | Tipo de dispositivo |
| currentReading | double | Lectura actual |
| unit | String | Unidad de medida |
| location | String | Ubicación |
| status | String | Estado (online/offline) |
| timestamp | long | Timestamp Unix |

---

## DeviceType (Enum)

**Ubicación**: `com.enerlink.enerlink.iot.dominio.modelo.DeviceType`

```java
public enum DeviceType {
    SMART_METER,
    SOLAR_PANEL,
    BATTERY_STORAGE,
    ELECTRIC_VEHICLE_CHARGER,
    THERMOSTAT,
    SMART_SWITCH,
    SENSOR
}
```

---

## DTOs de Proveedores IoT

### SmartHomeProviderDTO

**Ubicación**: `com.enerlink.enerlink.iot.infraestructura.dto.SmartHomeProviderDTO`

Formato propietario del proveedor SmartHome.

### EnergyCloudProviderDTO

**Ubicación**: `com.enerlink.enerlink.iot.infraestructura.dto.EnergyCloudProviderDTO`

Formato propietario del proveedor EnergyCloud.

---

## Entity vs Domain Model

### JPA Entities (Persistencia)

| Entity | Tabla | Descripción |
|--------|-------|-------------|
| UserEntity | USERS | Persistencia de usuario |
| EnergyOfferEntity | ENERGY_OFFERS | Persistencia de oferta |

### Mapeo

```
User (Dominio) ←→ UserEntity (JPA) ←→ UserRepositoryAdapter
EnergyOffer (Dominio) ←→ EnergyOfferEntity (JPA) ←→ EnergyOfferRepositoryAdapter
```

### Nota Importante

Los **domain models** son independientes de la infraestructura:
- No tienen anotaciones JPA (@Entity, @Table, etc.)
- No conocen la base de datos
- Los adaptadores de persistencia realizan la conversión

---

## Relaciones entre Entidades

```
User (1) ──────────► (N) EnergyOffer
    │
    └── producer ──► EnergyOffer.offer

User (1) ──────────► (N) Transaction (como buyer o seller)
    │
    └── buyer ──► Transaction.buyer
    └── seller ──► Transaction.seller

EnergyOffer (1) ──► (N) Transaction
    │
    └── offer ──► Transaction.offer
```

---

## Enums del Sistema

| Enum | Valores | Módulo |
|------|---------|--------|
| SaleType | DIRECT, AUCTION | energia |
| DeviceType | SENSOR, ACTUATOR, GATEWAY, SMART_METER, SOLAR_PANEL, etc. | iot |