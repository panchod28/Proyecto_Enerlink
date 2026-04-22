# Execution Flows - Enerlink

## 1. Crear Usuario

### Flow: Crear nuevo usuario con Factory Method

```
HTTP POST /api/usuarios
    │
    ▼ UserController.crear(@RequestBody UserRequest)
    │   @PostMapping → userService.crearUsuario(nombre, email, rol)
    ▼ UserService.crearUsuario()
    │   1. obtenerFactory(rol) → busca en Map<String, UserFactory>
    │   2. factory.crearUsuario(nombre, email) → Factory Method
    │   3. PredictionEngine.INSTANCE.predict(100.0) → Singleton
    │   4. userRepositoryPort.guardar(user) → Persistencia
    ▼ UserRepositoryAdapter.guardar(User)
    │   - Conversión User → UserEntity
    │   - jpaRepository.save(entity)
    ▼ H2 Database (USER table)
```

### Código Involucrado

```java
// Controller
@PostMapping
public User crear(@RequestBody UserRequest request) {
    return userService.crearUsuario(
        request.getNombre(),
        request.getEmail(),
        request.getRol()
    );
}

// Service
public User crearUsuario(String nombre, String email, String rol) {
    UserFactory factory = obtenerFactory(rol);  // Por rol: PRODUCER, CONSUMER, MIXED
    User user = factory.crearUsuario(nombre, email);
    double prediccion = PredictionEngine.INSTANCE.predict(100.0);  // Singleton
    return userRepositoryPort.guardar(user);
}
```

### Response

```json
{
    "id": 1,
    "nombre": "Juan Pérez",
    "email": "juan@example.com",
    "rol": "PRODUCER"
}
```

---

## 2. Publicar Oferta de Energía

### Flow: Crear oferta con Abstract Factory

```
HTTP POST /api/offers
    │
    ▼ EnergyOfferController.create(EnergyOfferRequest)
    │   @PostMapping → facade.publishOffer(saleType, producerId, kwh, price)
    ▼ EnergyTradingFacade.publishOffer()
    │   1. repositoryPort.findById(producerId) → Busca producer
    │   2. Create EnergyOffer via EnergySaleFactory
    │   3. Execute SaleProcess (Direct o Auction)
    │   4. Apply Decorator Chain
    │   5. repositoryPort.save(offer)
    ▼ EnergyOfferRepositoryAdapter.save(EnergyOffer)
    │   - Conversión EnergyOffer → EnergyOfferEntity
    │   - jpaRepository.save(entity)
    ▼ H2 Database (ENERGY_OFFERS table)
```

### Decorator Chain

El `EnergyTradingFacade` aplica una cadena de decoradores:

```java
private Transaction applyDecoratorChain(Transaction transaction) {
    TransactionComponent component = new ConcreteTransactionComponent(transaction);
    component = new ValidatingTransactionDecorator(component);  // Valida
    component = new FeeTransactionDecorator(component, 0.0);     // Aplica fees
    component = new AuditingTransactionDecorator(component);      // Registra
    
    return Transaction.builder()
        .id(component.getId())
        .offer(component.getOffer())
        .buyer(component.getBuyer())
        .seller(component.getSeller())
        .kwh(component.getKwh())
        .price(component.getPrice())
        .timestamp(component.getTimestamp())
        .build();
}
```

### Código Involucrado

```java
// Controller
@PostMapping
public ResponseEntity<EnergyOffer> create(@RequestBody EnergyOfferRequest request) {
    EnergyOffer offer = facade.publishOffer(
        request.getSaleType(),
        request.getProducerId(),
        request.getKwh(),
        request.getPrice()
    );
    return ResponseEntity.ok(offer);
}

// Facade
public EnergyOffer publishOffer(SaleType saleType, Long producerId, double kwh, double price) {
    return energyOfferService.createOffer(saleType, producerId, kwh, price);
}

// Service
public EnergyOffer createOffer(SaleType saleType, Long producerId, double kwh, double price) {
    User producer = userRepository.buscarPorId(producerId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    
    EnergySaleFactory factory = factoryMap.get(saleType);
    EnergyOffer offer = factory.createEnergyOffer(null, producer, kwh, price);
    
    SaleProcess process = factory.createSaleProcess();
    process.execute(offer, producer, kwh);
    
    return repository.save(offer);
}
```

### Response

```json
{
    "id": 1,
    "producer": {
        "id": 1,
        "nombre": "Juan Pérez",
        "email": "juan@example.com",
        "rol": "PRODUCER"
    },
    "kwh": 100.0,
    "price": 0.12,
    "saleType": "DIRECT"
}
```

---

## 3. Fetch Datos de Dispositivo IoT

### Flow: Fetch con Proxy (Caching)

```
HTTP GET /api/iot/devices/{deviceId}
    │
    ▼ IoTDeviceController.getDevice(deviceId, provider?)
    │   @GetMapping("/{deviceId}")
    ▼ IoTDeviceService.getDeviceData(deviceId)
    │   ioTDataPort.fetchDeviceData(deviceId)
    ▼ CachingIoTDataProxy.fetchDeviceData()  ← PROXY (Caching)
    │   1. Check cache (device:{id})
    │   2. If cache hit → return cached
    │   3. If cache miss → delegate.fetchDeviceData()
    │      - IoTDataPortComposite.fetchDeviceData()
    │      - SmartHomeAdapter / EnergyCloudAdapter
    │   4. Store in cache (TTL 5 min)
    ▼ Proveedor Externo (SmartHome / EnergyCloud)
```

### Cache Keys

| Método | Clave de Cache |
|--------|---------------|
| fetchDeviceData(id) | `device:{id}` |
| fetchAllDevices() | `all` |
| fetchDevicesByLocation(loc) | `location:{loc}` |
| fetchDevicesByType(type) | `type:{type}` |

### Código Involucrado

```java
// Controller
@GetMapping("/{deviceId}")
public IoTDeviceData getDevice(@PathVariable String deviceId,
                          @RequestParam(required = false) String provider) {
    if (provider != null && !provider.isEmpty()) {
        return ioTDeviceService.getDeviceData(deviceId, provider);
    }
    return ioTService.getDeviceData(deviceId);
}

// Service
public IoTDeviceData getDeviceData(String deviceId) {
    return ioTDataPort.fetchDeviceData(deviceId);
}

// Proxy (CachingIoTDataProxy)
@Override
public IoTDeviceData fetchDeviceData(String deviceId) {
    String cacheKey = "device:" + deviceId;
    
    CacheEntry<IoTDeviceData> entry = deviceCache.get(cacheKey);
    if (entry != null) {
        IoTDeviceData cached = entry.getValueIfValid();
        if (cached != null) {
            logger.debug("Cache HIT for device: {}", deviceId);
            return cached;
        }
    }
    
    logger.info("Cache MISS for device: {}, fetching from provider", deviceId);
    IoTDeviceData result = delegate.fetchDeviceData(deviceId);
    
    if (result != null) {
        deviceCache.put(cacheKey, new CacheEntry<>(result, ttl));
    }
    
    return result;
}
```

### Response

```json
{
    "deviceId": "device-001",
    "deviceName": "Sensor Temperatura",
    "deviceType": "SENSOR",
    "currentReading": 22.5,
    "unit": "celsius",
    "location": "home",
    "status": "online",
    "timestamp": 1713700000000
}
```

---

## 4. Listar Ofertas

### Flow: Simple fetch

```
HTTP GET /api/offers
    │
    ▼ EnergyOfferController.getAll()
    │   @GetMapping → facade.getActiveOffers()
    ▼ EnergyTradingFacade.getActiveOffers()
    │   energyOfferService.getAll()
    ▼ EnergyOfferRepositoryAdapter.findAll()
    │   jpaRepository.findAll()
    ▼ EnergyOfferEntity → EnergyOffer (mapping)
```

---

## 5. Eliminar Usuario

### Flow: Delete

```
HTTP DELETE /api/usuarios/{id}
    │
    ▼ UserController.eliminar(id)
    │   @DeleteMapping("/{id}")
    ▼ UserService.eliminarUsuario(id)
    │   userRepositoryPort.eliminarPorId(id)
    ▼ UserRepositoryAdapter.eliminarPorId(id)
    │   jpaRepository.deleteById(id)
```

---

## 6. Ejecutar Venta Directa

### Flow: Execute Direct Sale

```
HTTP POST /api/offers/{id}/sale
    │
    │ Request: { "buyerId": 2 }
    ▼ EnergyTradingFacade.executeDirectSale(offerId, buyer)
    │   1. Find offer by ID
    │   2. Validate saleType == DIRECT
    │   3. Create SaleProcess via DirectSaleFactory
    │   4. Execute process → Transaction
    │   5. Apply Decorator Chain
    ▼ Transaction returned
```

### Código

```java
public Transaction executeDirectSale(Long offerId, User buyer) {
    EnergyOffer offer = repositoryPort.findById(offerId)
        .orElseThrow(() -> new RuntimeException("Oferta no encontrada"));
    
    if (offer.getSaleType() != SaleType.DIRECT) {
        throw new IllegalStateException("La oferta no es de tipo venta directa");
    }
    
    SaleProcess saleProcess = directSaleFactory.createSaleProcess();
    Transaction transaction = saleProcess.execute(offer, buyer, offer.getKwh());
    
    return applyDecoratorChain(transaction);
}
```

### Response

```json
{
    "id": 1,
    "offer": { ... },
    "buyer": { ... },
    "seller": { ... },
    "kwh": 100.0,
    "price": 0.12,
    "timestamp": "2026-04-21T10:30:00"
}
```

---

## 7. Ejecutar Puja en Subasta

### Flow: Execute Auction Bid

```
HTTP POST /api/offers/{id}/auction
    │
    │ Request: { "buyerId": 2, "bidAmount": 0.15 }
    ▼ EnergyTradingFacade.executeAuction(offerId, buyer, bidAmount)
    │   1. Find offer by ID
    │   2. Validate saleType == AUCTION
    │   3. Validate bidAmount >= offer.getPrice()
    │   4. Create SaleProcess via AuctionSaleFactory
    │   5. Execute process → Transaction
    │   6. Apply Decorator Chain
    ▼ Transaction returned
```

### Código

```java
public Transaction executeAuction(Long offerId, User buyer, double bidAmount) {
    EnergyOffer offer = repositoryPort.findById(offerId)
        .orElseThrow(() -> new RuntimeException("Oferta no encontrada"));
    
    if (offer.getSaleType() != SaleType.AUCTION) {
        throw new IllegalStateException("La oferta no es de tipo subasta");
    }
    
    if (offer.getPrice() > 0 && bidAmount < offer.getPrice()) {
        throw new IllegalArgumentException("Bid must be >= base price");
    }
    
    SaleProcess auctionProcess = auctionSaleFactory.createSaleProcess();
    Transaction transaction = auctionProcess.execute(offer, buyer, offer.getKwh(), bidAmount);
    
    return applyDecoratorChain(transaction);
}
```

---

## Resumen de Flujos

| Flujo | Patrones Involucrados | Capa de Persistencia |
|-------|-------------------|------------------|
| Crear usuario | Factory Method, Singleton | JPA |
| Publicar oferta | Abstract Factory, Decorator | JPA |
| Fetch IoT | Adapter, Composite, Proxy (Caching) | Proveedor externo |
| Listar ofertas | Facade | JPA |
| Eliminar usuario | - | JPA |
| Venta directa | Facade, Decorator Chain | JPA |
| Puja en subasta | Facade, Decorator Chain | JPA |