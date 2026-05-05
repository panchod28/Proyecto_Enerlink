# IoT Module Analysis Report

## 1. IoTDeviceData Fields

| Field | Type | Default Value |
|-------|------|---------------|
| deviceId | String | null |
| deviceName | String | null |
| deviceType | DeviceType | null |
| currentReading | double | 0.0 |
| unit | String | null |
| location | String | null |
| status | String | null |
| timestamp | long | 0 |

No explicit default values set in the constructor. Java defaults apply (null for objects, 0.0 for double, 0 for long).

---

## 2. DeviceType Enum Values

```
SMART_METER
SOLAR_PANEL
BATTERY_STORAGE
ELECTRIC_VEHICLE_CHARGER
THERMOSTAT
SMART_SWITCH
SENSOR
```

---

## 3. Where Device Data Comes From

### SmartHomeProviderClient

Generates **hardcoded mock data** (not random):

**fetchDeviceById:**
- deviceId: passed as parameter
- deviceName: fixed `"Smart Device " + deviceId`
- deviceCategory: fixed `"smart_meter"`
- metrics.value: fixed `1234.56`
- metrics.unitOfMeasure: fixed `"kWh"`
- metrics.qualityScore: fixed `0.98`
- location.zone: fixed `"Living Room"`
- location.address: fixed `"123 Main St"`
- operationalState: fixed `"online"`
- lastUpdated: `System.currentTimeMillis()` (only dynamic value)

**fetchAllDevices** returns 3 fixed mock devices:
- DEV-001, "Smart Meter 1", category "meter", value 100.0
- DEV-002, "Solar Panel Array", category "solar", value 100.0
- DEV-003, "Battery Storage", category "battery", value 100.0

### EnergyCloudProviderClient

Generates **hardcoded mock data** (not random):

**fetchDeviceById:**
- resource.id: passed as parameter
- resource.name: fixed `"Energy Device " + deviceId`
- resource.type: fixed `"meter"`
- resource.manufacturer: fixed `"EnergyCorp"`
- resource.model: fixed `"EC-2000"`
- measurement.currentValue: fixed `2500.75`
- measurement.uom: fixed `"kWh"`
- measurement.timestamp: `System.currentTimeMillis()` (only dynamic value)
- status.code: fixed `"ACTIVE"`
- status.active: fixed `true`

**fetchAllDevices** returns 3 fixed mock resources:
- EC-001, "Primary Meter", type "energy_meter", value 500.0
- EC-002, "Solar Array PV", type "pv_array", value 500.0
- EC-003, "Storage System", type "storage", value 500.0

**Conclusion:** Data is NOT random, NOT per-user. All values are fixed/hardcoded mocks. Only timestamps are dynamic.

---

## 4. IoTDataPort Interface Methods

```java
public interface IoTDataPort {
    IoTDeviceData fetchDeviceData(String deviceId);
    List<IoTDeviceData> fetchAllDevices();
    List<IoTDeviceData> fetchDevicesByLocation(String location);
    List<IoTDeviceData> fetchDevicesByType(String deviceType);
}
```

---

## 5. CachingIoTDataProxy

Implements `IoTDataPort` interface. Methods:

| Method | Cache Key Pattern | Cache Map |
|--------|-------------------|-----------|
| fetchDeviceData(String deviceId) | `"device:" + deviceId` | `deviceCache` (Map<String, CacheEntry<IoTDeviceData>>) |
| fetchAllDevices() | `"all"` | `listCache` (Map<String, CacheEntry<List<IoTDeviceData>>>) |
| fetchDevicesByLocation(String location) | `"location:" + location` | `listCache` |
| fetchDevicesByType(String deviceType) | `"type:" + deviceType` | `listCache` |

**How cache keys work:**
- Device-level cache uses prefix `"device:"` + deviceId
- List-level cache uses prefixes `"all"`, `"location:"`, or `"type:"`
- Two separate maps: `deviceCache` for single-device entries, `listCache` for list entries
- TTL: default 5 minutes (configurable via constructor)
- Thread-safe: uses `ConcurrentHashMap` and `ReentrantLock` per key
- Cache invalidation methods: `invalidateDevice(String)`, `invalidateAll()`

---

## 6. IoTDataPortComposite

**Children:** `List<IoTDataPort>` - maintains a list of adapter implementations.

**Aggregation logic:**
- `fetchDeviceData`: Iterates through children, returns first non-null result (stops at first found)
- `fetchAllDevices`: Iterates all children, aggregates all results into a single list
- `fetchDevicesByLocation`: Iterates all children, aggregates results by location
- `fetchDevicesByType`: Iterates all children, aggregates results by type

**Configured children** (from `IoTAdapterConfig.java`):
1. `SmartHomeAdapter`
2. `EnergyCloudAdapter`

**Pattern:** Composite Pattern - treats individual adapters and the composite uniformly through the same `IoTDataPort` interface.

---

## 7. IoTDeviceController Endpoints

| HTTP Method | Path | Method Called | Returns |
|-------------|------|---------------|---------|
| GET | `/api/iot/devices/{deviceId}` | `ioTDeviceService.getDeviceData(deviceId)` or `ioTDeviceService.getDeviceData(deviceId, provider)` | `IoTDeviceData` |
| GET | `/api/iot/devices` | `ioTDeviceService.getAllDevices()` | `List<IoTDeviceData>` |
| GET | `/api/iot/devices/location/{location}` | `ioTDeviceService.getDevicesByLocation(location)` | `List<IoTDeviceData>` |
| GET | `/api/iot/devices/type?type=...` | `ioTDeviceService.getDevicesByType(type)` | `List<IoTDeviceData>` |

Note: The controller uses `IoTDeviceService`, not `IoTProcessorService`.

---

## 8. IoTDeviceService and IoTProcessorService

### IoTDeviceService Methods

| Method | What It Does |
|--------|--------------|
| `getDeviceData(String deviceId)` | Calls `ioTDataPort.fetchDeviceData(deviceId)` |
| `getDeviceData(String deviceId, String provider)` | Calls `ioTDataPort.fetchDeviceData(deviceId)` (ignores provider parameter) |
| `getAllDevices()` | Calls `ioTDataPort.fetchAllDevices()` |
| `getDevicesByLocation(String location)` | Calls `ioTDataPort.fetchDevicesByLocation(location)` |
| `getDevicesByType(String deviceType)` | Calls `ioTDataPort.fetchDevicesByType(deviceType)` |

Note: The `getDeviceData(deviceId, provider)` method accepts a provider parameter but does NOT use it.

### IoTProcessorService Methods

| Method | What It Does |
|--------|--------------|
| `getDefaultProcessor()` | Returns the default `IoTProcessor` bean |
| `getProcessorForProvider(String provider)` | Currently ALWAYS returns `defaultProcessor` (ignores provider parameter) |
| `processDeviceData(String deviceId)` | Calls `defaultProcessor.processDeviceData(deviceId)` |
| `processDeviceData(String deviceId, String provider)` | Gets processor via `getProcessorForProvider()`, then calls `processDeviceData(deviceId)` |
| `processAllDevices()` | Calls `defaultProcessor.processAllDevices()` |
| `processDevicesByLocation(String location)` | Calls `defaultProcessor.processDevicesByLocation(location)` |
| `processDevicesByType(String deviceType)` | Calls `defaultProcessor.processDevicesByType(deviceType)` |

---

## 9. User Persistence Pattern

### UserEntity → User Domain Model Mapping

**UserEntity fields:** id (Long), nombre (String), email (String), rol (String)

**User domain model fields:** id (Long), nombre (String), email (String), rol (String)

**UserRepositoryAdapter mapping:**

```java
// Entity → Domain (buscarPorId, listarTodos)
User user = new User(entity.getNombre(), entity.getEmail(), entity.getRol()) { };
user.setId(entity.getId());

// Domain → Entity (guardar)
UserEntity entity = new UserEntity(user.getNombre(), user.getEmail(), user.getRol());
if (user.getId() != null) entity.setId(user.getId());
```

### UserRepositoryPort Methods

```java
public interface UserRepositoryPort {
    User guardar(User user);
    Optional<User> buscarPorId(Long id);
    List<User> listarTodos();
    void eliminarPorId(Long id);
}
```

---

## 10. EnergyOffer Persistence Pattern

**Reference template from EnergyOfferRepositoryAdapter:**

```java
// Domain → Entity (save)
EnergyOfferEntity entity = new EnergyOfferEntity();
entity.setId(offer.getId());
entity.setProducerId(offer.getProducer().getId());
entity.setKwh(offer.getKwh());
entity.setPrice(offer.getPrice());
entity.setSaleType(offer.getSaleType());
entity.setAvailable(offer.isAvailable());
EnergyOfferEntity saved = jpaRepository.save(entity);

// Entity → Domain (findAll, findById)
User producer = userRepository.buscarPorId(entity.getProducerId())
    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
EnergyOffer offer = new EnergyOffer(
    entity.getId(), producer, entity.getKwh(), 
    entity.getPrice(), entity.getSaleType());
offer.setAvailable(entity.isAvailable());
```

**EnergyOfferRepositoryPort methods:**

```java
public interface EnergyOfferRepositoryPort {
    EnergyOffer save(EnergyOffer offer);
    List<EnergyOffer> findAll();
    Optional<EnergyOffer> findById(Long id);
    void deleteById(Long id);
}
```

**Key pattern:** The adapter converts between domain objects and JPA entities. Uses `UserRepositoryPort` to fetch the producer User domain object when reconstructing EnergyOffer.

---

## 11. V1 Migration SQL

```sql
-- Initial schema migration for Enerlink
-- Tables created in dependency order: users -> energy_offer -> transactions

-- Table: users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    rol VARCHAR(255) NOT NULL
);

-- Table: energy_offer
CREATE TABLE IF NOT EXISTS energy_offer (
    id BIGSERIAL PRIMARY KEY,
    producer_id BIGINT,
    kwh DOUBLE PRECISION,
    price DOUBLE PRECISION,
    available BOOLEAN DEFAULT TRUE,
    sale_type VARCHAR(50)
);

-- Table: transactions
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    offer_id BIGINT,
    buyer_id BIGINT,
    seller_id BIGINT,
    kwh DOUBLE PRECISION,
    price DOUBLE PRECISION,
    timestamp TIMESTAMP,
    CONSTRAINT fk_transaction_offer FOREIGN KEY (offer_id) REFERENCES energy_offer(id),
    CONSTRAINT fk_transaction_buyer FOREIGN KEY (buyer_id) REFERENCES users(id),
    CONSTRAINT fk_transaction_seller FOREIGN KEY (seller_id) REFERENCES users(id)
);
```

---

## 12. Existing GOF Patterns in IoT Module

| Pattern | Classes Implementing It | Description |
|---------|------------------------|-------------|
| **Composite** | `IoTDataPortComposite` | Aggregates multiple `IoTDataPort` implementations (SmartHomeAdapter, EnergyCloudAdapter) and treats them uniformly |
| **Proxy** | `CachingIoTDataProxy` | Wraps `IoTDataPort` to add caching behavior without changing the interface |
| **Adapter** | `SmartHomeAdapter`, `EnergyCloudAdapter` | Converts external provider DTOs (SmartHomeProviderDTO, EnergyCloudProviderDTO) to domain model (IoTDeviceData) |
| **Template Method** | `AbstractIoTProcessor` with `SimpleIoTProcessor`, `FilteringIoTProcessor`, `EnrichedIoTProcessor` | Defines skeleton algorithm in `transformDeviceList()` and `process*()` methods, subclasses override `transformDeviceData()` |
| **Strategy** | `IoTProcessor` interface with multiple implementations | Different processor strategies (Simple, Filtering, Enriched) can be used interchangeably |
| **Factory/Configuration** | `IoTAdapterConfig`, `IoTProcessorConfig` | Spring @Configuration classes that wire beans (Factory-like pattern for object creation) |

---

## 13. Risk Analysis

### Q: Would adding persistence to IoTDeviceData break CachingIoTDataProxy?

**Answer: NO**

**Why:** CachingIoTDataProxy caches `IoTDeviceData` objects and `List<IoTDeviceData>` using `CacheEntry`. Adding persistence (e.g., saving to database) would be a new operation not related to caching. The proxy would need a new method or a separate persistence adapter. The cache stores domain objects in memory - adding a `@Entity` for persistence doesn't affect the cache's ability to store domain objects. However, if you want to cache database-fetched data, the proxy would work fine as-is.

---

### Q: Would adding a userId field to IoTDeviceData break any existing pattern?

**Answer: NO** (with minor caveat)

**Why:** IoTDeviceData is a plain domain model with getters/setters. Adding a field is backward-compatible. However:
- The mock data providers (`SmartHomeProviderClient`, `EnergyCloudProviderClient`) don't provide userId - would need updates
- `IoTDataMapper` would need to handle the new field during DTO → domain mapping
- If adding `@ManyToOne` to a future `IoTDeviceEntity`, the domain model itself wouldn't change (entities are in infrastructure layer)

---

### Q: Would replacing SmartHomeProviderClient/EnergyCloudProviderClient with a DB-backed adapter break the Adapter pattern?

**Answer: NO**

**Why:** The Adapter pattern's purpose is to convert between the external data source format and the domain model. Whether the data comes from:
- Mock objects (current SmartHomeProviderClient)
- External API clients
- Database queries (new adapter)

As long as the adapter implements `IoTDataPort` and converts to `IoTDeviceData`, the pattern is preserved. A DB-backed adapter would:
1. Implement `IoTDataPort`
2. Use a JpaRepository to fetch data
3. Convert JPA entities to `IoTDeviceData` domain objects

This is exactly the same pattern used in `UserRepositoryAdapter` and `EnergyOfferRepositoryAdapter`.

---

### Q: Would adding CRUD endpoints to IoTDeviceController break the Bridge pattern?

**Answer: NO** (there is no Bridge pattern currently)

**Why:** The current IoT module does NOT implement the Bridge pattern. It uses:
- Composite pattern (IoTDataPortComposite)
- Proxy pattern (CachingIoTDataProxy)
- Adapter pattern (SmartHomeAdapter, EnergyCloudAdapter)
- Template Method (AbstractIoTProcessor)

Adding CRUD endpoints to `IoTDeviceController` would simply extend its functionality. The controller currently uses `IoTDeviceService` which uses `IoTDataPort`. Adding write operations (POST, PUT, DELETE) would require:
1. Adding methods to `IoTDataPort` interface
2. Implementing them in adapters
3. Adding service methods

This would NOT break any existing pattern - it would be an extension of the existing hexagonal architecture.

---

### Q: Would the new IoTDeviceEntity need a @ManyToOne to UserEntity?

**Answer: YES** (if devices belong to users)

**Why:** Based on the existing patterns in the codebase:
- `EnergyOfferEntity` has `producer_id` (Long) referencing `UserEntity`
- `TransactionEntity` has `buyer_id` and `seller_id` with `@ManyToOne` to `UserEntity`

If `IoTDeviceData` gains a `userId` field, then `IoTDeviceEntity` should have:
```java
@ManyToOne
@JoinColumn(name = "user_id")
private UserEntity user;
```

This follows the same pattern as `TransactionEntity` which has multiple `@ManyToOne` relationships to `UserEntity`. The domain model `IoTDeviceData` would remain pure (no JPA annotations), and the mapping would happen in the repository adapter, same as `EnergyOfferRepositoryAdapter` fetches `User` via `userRepository.buscarPorId()`.

---

*Report generated on 2026-05-05*
