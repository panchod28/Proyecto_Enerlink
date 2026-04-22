# Design Patterns - Enerlink

## Patrones Implementados

### 1. Singleton

**Ubicación**: `com.enerlink.enerlink.configuracion.PredictionEngine`

**Tipo**: Enum (Joshua Bloch)

```java
public enum PredictionEngine {
    INSTANCE;  // Una sola instancia garantizada por JVM

    public double predict(double consumoActual) {
        double factorBase = 1.15;
        double variacion = ThreadLocalRandom.current().nextDouble(-0.05, 0.05);
        return consumoActual * (factorBase + variacion);
    }
}
```

**Por qué se usa**: Garantizar una sola instancia del motor de predicciones en toda la aplicación.

**Problema resuelto**: Evitar múltiples instancias de PredictionEngine que consumirían recursos innecesarios.

---

### 2. Factory Method

**Ubicación**: 
- `com.enerlink.enerlink.usuario.dominio.factory.UserFactory` (interfaz)
- `com.enerlink.enerlink.usuario.dominio.factory.ProducerUserFactory`
- `com.enerlink.enerlink.usuario.dominio.factory.ConsumerUserFactory`
- `com.enerlink.enerlink.usuario.dominio.factory.MixedUserFactory`

**Por qué se usa**: Crear el tipo de usuario correcto según el rol sin conocimiento de las clases concretas.

**Problema resuelto**: El código cliente (`UserService`) no necesita saber qué clase instanciar. Solo llama a `factory.crearUsuario()`.

```java
// UserService usa factory sin conocer la clase concreta
UserFactory factory = userFactories.get(rol.toUpperCase());
User user = factory.crearUsuario(nombre, email);
```

---

### 3. Abstract Factory

**Ubicación**:
- `com.enerlink.enerlink.energia.dominio.factory.EnergySaleFactory` (interfaz)
- `com.enerlink.enerlink.energia.dominio.factory.DirectSaleFactory`
- `com.enerlink.enerlink.energia.dominio.factory.AuctionSaleFactory`

**Por qué se usa**: Crear familias de objetos relacionados (EnergyOffer + SaleProcess) según el tipo de venta.

**Problema resuelto**: Crear oferta Y proceso de venta con una sola factory.

```java
EnergySaleFactory factory = factoryMap.get(saleType);  // DIRECT o AUCTION
EnergyOffer offer = factory.createEnergyOffer(null, producer, kwh, price);
SaleProcess process = factory.createSaleProcess();     // SAME factory
```

---

### 4. Fluent Builder

**Ubicación**: `com.enerlink.enerlink.energia.dominio.modelo.Transaction`

```java
Transaction tx = Transaction.builder()
    .id(1L)
    .offer(offer)
    .buyer(buyer)
    .seller(seller)
    .kwh(100.0)
    .price(0.12)
    .build();
```

**Por qué se usa**: Construcción de objetos complejos de forma legible y encadenada.

**Problema resuelto**: Evitar constructore con muchos parámetros.

---

### 5. Prototype

**Ubicación**: 
- `com.enerlink.enerlink.energia.dominio.modelo.Prototype` (interfaz)
- `com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer` (implementa Prototype)
- `com.enerlink.enerlink.energia.dominio.factory.EnergyOfferPrototypeRegistry`

**Por qué se usa**: Clonar ofertas existentes para crear nuevas basadas en templates.

**Problema resuelto**: Evitar crear ofertas desde cero cada vez.

```java
// Clonar oferta
EnergyOffer newOffer = existingOffer.clone();

// Usar registry de prototypes
EnergyOffer template = registry.create("standard-direct");
```

---

### 6. Adapter

**Ubicación**:
- `com.enerlink.enerlink.iot.infraestructura.adapter.SmartHomeAdapter`
- `com.enerlink.enerlink.iot.infraestructura.adapter.EnergyCloudAdapter`

**Por qué se usa**: Normalizar interfaces de proveedores externos diferentes a una interfaz común (`IoTDataPort`).

**Problema resuelto**: El dominio usa una interfaz unificada, los adaptadores traducen al formato del proveedor.

```java
public class SmartHomeAdapter implements IoTDataPort {
    // Convierte SmartHomeProviderDTO → IoTDeviceData
    public IoTDeviceData fetchDeviceData(String deviceId) {
        Optional<Object> rawData = providerClient.fetchDeviceById(deviceId);
        SmartHomeProviderDTO dto = convertToSmartHomeDTO(rawData.get());
        return mapper.mapFromSmartHome(dto);
    }
}
```

---

### 7. Bridge

**Ubicación**: 
- `com.enerlink.enerlink.iot.dominio.servicio.AbstractIoTProcessor`
- `com.enerlink.enerlink.iot.dominio.servicio.SimpleIoTProcessor`
- `com.enerlink.enerlink.iot.dominio.servicio.FilteringIoTProcessor`
- `com.enerlink.enerlink.iot.dominio.servicio.EnrichedIoTProcessor`

**Por qué se usa**: Separar abstracción (procesamiento) de implementación (lectura de datos).

**Problema resuelto**: Cambiar algoritmos de procesamiento sin modificar la estructura de datos.

---

### 8. Decorator (Chain)

**Ubicación**:
- `com.enerlink.enerlink.energia.dominio.decorador.TransactionDecorator`
- `com.enerlink.enerlink.energia.dominio.decorador.ValidatingTransactionDecorator`
- `com.enerlink.enerlink.energia.dominio.decorador.FeeTransactionDecorator`
- `com.enerlink.enerlink.energia.dominio.decorador.DiscountedTransactionDecorator`
- `com.enerlink.enerlink.energia.dominio.decorador.AuditingTransactionDecorator`

**Por qué se usa**: Añadir comportamientos (validación, fees, auditoría) sin modificar la clase Transaction.

**Problema resuelto**: Composición dinámica de comportamientos.

```java
TransactionComponent component = new ConcreteTransactionComponent(transaction);
component = new ValidatingTransactionDecorator(component);  // Valida
component = new FeeTransactionDecorator(component, 0.0);   // Fees
component = new AuditingTransactionDecorator(component);   // Audita
```

---

### 9. Facade

**Ubicación**: `com.enerlink.enerlink.energia.aplicacion.servicio.EnergyTradingFacade`

**Por qué se usa**: Proporcionar una interfaz unificada simple para todo el subsystem de trading de energía.

**Problema resuelto**: El cliente no necesita conocer:
- EnergyOfferService
- DirectSaleFactory / AuctionSaleFactory
- SaleProcess
- Cadena de decoradores

```java
// Cliente simple
EnergyOffer offer = facade.publishOffer(saleType, producerId, kwh, price);
Transaction tx = facade.executeDirectSale(offerId, buyer);
```

---

### 10. Composite

**Ubicación**: `com.enerlink.enerlink.iot.infraestructura.adapter.IoTDataPortComposite`

**Por qué se usa**: Tratar múltiples adaptadores como uno solo.

**Problema resuelto**: Agregar datos de SmartHome + EnergyCloud sin que el cliente lo sepa.

```java
IoTDataPortComposite composite = new IoTDataPortComposite();
composite.add(smartHomeAdapter);
composite.add(energyCloudAdapter);
// client usa solo composite.fetchDeviceData()
```

---

### 11. Proxy (Caching)

**Ubicación**: `com.enerlink.enerlink.iot.infraestructura.adapter.CachingIoTDataProxy`

**Por qué se usa**: Cachear resultados de proveedores IoT para evitar llamadas redundantes.

**Problema resuelto**: Reducir latencia y llamadas a proveedores externos.

```java
// Sin modificar el código cliente
public IoTDataPort ioTDataPort() {
    return new CachingIoTDataProxy(ioTDataPortComposite, Duration.ofMinutes(5));
}
```

---

### 12. Strategy

**Ubicación**:
- `com.enerlink.enerlink.iot.infraestructura.adapter.AdapterSelectionStrategy` (interfaz)
- `com.enerlink.enerlink.iot.infraestructura.adapter.ProviderParameterSelectionStrategy`
- `com.enerlink.enerlink.iot.infraestructura.adapter.FallbackSelectionStrategy`
- `com.enerlink.enerlink.iot.infraestructura.adapter.DeviceIdPrefixSelectionStrategy`

**Por qué se usa**: Seleccionar dinámicamente el adapter IoT apropiado según diferentes criterios (parámetro provider, prefijo de deviceId, fallback).

**Problema resuelto**: Determinar qué proveedor usar en tiempo de ejecución sin hardcodear lógica.

```java
public interface AdapterSelectionStrategy {
    IoTDataPort select(List<IoTDataPort> adapters, String deviceId);
}
```

---

## Resumen de Patrones

| # | Patrón | Módulo | Problema Resuelto |
|---|--------|--------|------------------|
| 1 | Singleton | configuracion | Una sola instancia de PredictionEngine |
| 2 | Factory Method | usuario | Crear tipo de usuario según rol |
| 3 | Abstract Factory | energia | Crear oferta + proceso según SaleType |
| 4 | Fluent Builder | energia | Construcción legible de Transaction |
| 5 | Prototype | energia | Clonar ofertas existentes |
| 6 | Adapter | iot | Normalizar proveedores externos |
| 7 | Bridge | iot | Separar procesamiento de datos |
| 8 | Decorator | energia | Componer behaviors (validación, fees, auditoría) |
| 9 | Facade | energia | Interfaz simple para subsystem |
| 10 | Composite | iot | Agregar múltiples adaptadores |
| 11 | Proxy | iot | Cachear llamadas a proveedores |
| 12 | Strategy | iot | Seleccionar adapter según criterios |