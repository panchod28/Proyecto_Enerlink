# API Endpoints - Enerlink

## Base URL
```
http://localhost:8080/api
```

---

## Módulo: usuario

### Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/usuarios` | Crear usuario |
| GET | `/api/usuarios` | Listar todos los usuarios |
| GET | `/api/usuarios/{id}` | Obtener usuario por ID |
| PUT | `/api/usuarios/{id}` | Actualizar usuario |
| DELETE | `/api/usuarios/{id}` | Eliminar usuario |

---

### POST /api/usuarios

**Crear usuario**

Request Body:
```json
{
    "nombre": "Juan Pérez",
    "email": "juan@example.com",
    "rol": "PRODUCER"
}
```

Response (200 OK):
```json
{
    "id": 1,
    "nombre": "Juan Pérez",
    "email": "juan@example.com",
    "rol": "PRODUCER"
}
```

**Casos de uso**:
- `crearUsuario(nombre, email, rol)` en UserService

**Validaciones**:
- `nombre`: requerido
- `email`: requerido
- `rol`: requerido (PRODUCER, CONSUMER, o MIXED)

---

### GET /api/usuarios

**Listar todos los usuarios**

Response (200 OK):
```json
[
    {
        "id": 1,
        "nombre": "Juan Pérez",
        "email": "juan@example.com",
        "rol": "PRODUCER"
    },
    {
        "id": 2,
        "nombre": "María López",
        "email": "maria@example.com",
        "rol": "CONSUMER"
    }
]
```

**Casos de uso**:
- `listarUsuarios()` en UserService

---

### GET /api/usuarios/{id}

**Obtener usuario por ID**

Response (200 OK):
```json
{
    "id": 1,
    "nombre": "Juan Pérez",
    "email": "juan@example.com",
    "rol": "PRODUCER"
}
```

**Casos de uso**:
- `obtenerUsuarioPorId(id)` en UserService

**Error** (404 Not Found):
```json
{
    "error": "Usuario no encontrado"
}
```

---

### PUT /api/usuarios/{id}

**Actualizar usuario**

Request Body:
```json
{
    "nombre": "Juan Actualizado",
    "email": "juan.new@example.com",
    "rol": "MIXED"
}
```

Response (200 OK):
```json
{
    "id": 1,
    "nombre": "Juan Actualizado",
    "email": "juan.new@example.com",
    "rol": "MIXED"
}
```

**Casos de uso**:
- `actualizarUsuario(id, nombre, email, rol)` en UserService

---

### DELETE /api/usuarios/{id}

**Eliminar usuario**

Response: 204 No Content

**Casos de uso**:
- `eliminarUsuario(id)` en UserService

---

## Módulo: energia

### Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/offers` | Publicar nueva oferta |
| GET | `/api/offers` | Listar ofertas activas |
| GET | `/api/offers/{id}` | Obtener oferta por ID |
| PUT | `/api/offers/{id}` | Actualizar oferta |
| DELETE | `/api/offers/{id}` | Eliminar oferta |

---

### POST /api/offers

**Publicar oferta de energía**

Request Body:
```json
{
    "saleType": "DIRECT",
    "producerId": 1,
    "kwh": 100.0,
    "price": 0.12
}
```

Response (200 OK):
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

**Casos de uso**:
- `publishOffer(saleType, producerId, kwh, price)` en EnergyTradingFacade

**Validaciones**:
- `saleType`: requerido (DIRECT o AUCTION)
- `producerId`: requerido, debe existir
- `kwh`: requerido, debe ser mayor a 0
- `price`: requerido, debe ser mayor a 0

---

### GET /api/offers

**Listar ofertas activas**

Response (200 OK):
```json
[
    {
        "id": 1,
        "producer": { ... },
        "kwh": 100.0,
        "price": 0.12,
        "saleType": "DIRECT"
    }
]
```

**Casos de uso**:
- `getActiveOffers()` en EnergyTradingFacade

---

### GET /api/offers/{id}

**Obtener oferta por ID**

Response (200 OK):
```json
{
    "id": 1,
    "producer": { ... },
    "kwh": 100.0,
    "price": 0.12,
    "saleType": "DIRECT"
}
```

**Casos de uso**:
- `getOfferById(id)` en EnergyTradingFacade

**Error** (404 Not Found):
```json
{
    "error": "Oferta no encontrada con id: 1"
}
```

---

### PUT /api/offers/{id}

**Actualizar oferta (NOTA: Actualmente crea una nueva oferta)**

**ADVERTENCIA**: Este endpoint no actualiza la oferta existente. Llama a `publishOffer` que crea una nueva oferta.

Request Body:
```json
{
    "saleType": "AUCTION",
    "producerId": 1,
    "kwh": 200.0,
    "price": 0.15
}
```

Response (200 OK):
```json
{
    "id": 1,
    "producer": { ... },
    "kwh": 200.0,
    "price": 0.15,
    "saleType": "AUCTION"
}
```

**Casos de uso**:
- No recomendado para actualización (crea nuevo registro)
- Actualmente llama a `publishOffer()` que genera una nueva oferta

---

### DELETE /api/offers/{id}

**Eliminar oferta**

Response: 204 No Content

**Casos de uso**:
- `deleteOffer(id)` en EnergyTradingFacade

---

### POST /api/offers/{id}/sale

**Ejecutar venta directa**

Ejecuta una compra directa de una oferta (tipo DIRECT).

Request:
```
POST /api/offers/{id}/sale
```

Request Body:
```json
{
    "buyerId": 2
}
```

Response (200 OK):
```json
{
    "id": 1,
    "offer": {
        "id": 1,
        "producer": { ... },
        "kwh": 100.0,
        "price": 0.12,
        "saleType": "DIRECT"
    },
    "buyer": {
        "id": 2,
        "nombre": "María López",
        "email": "maria@example.com",
        "rol": "CONSUMER"
    },
    "seller": {
        "id": 1,
        "nombre": "Juan Pérez",
        "email": "juan@example.com",
        "rol": "PRODUCER"
    },
    "kwh": 100.0,
    "price": 0.12,
    "timestamp": "2026-04-21T10:30:00"
}
```

**Validaciones**:
- La oferta debe existir
- La oferta debe ser de tipo DIRECT
- El buyer debe existir

**Errores**:
- 404: "Oferta no encontrada con id: X"
- 400: "La oferta no es de tipo venta directa"

---

### POST /api/offers/{id}/auction

**Ejecutar puja en subasta**

Ejecuta una puja en una oferta de tipo Auction.

Request:
```
POST /api/offers/{id}/auction
```

Request Body:
```json
{
    "buyerId": 2,
    "bidAmount": 0.15
}
```

Response (200 OK):
```json
{
    "id": 2,
    "offer": {
        "id": 1,
        "producer": { ... },
        "kwh": 100.0,
        "price": 0.12,
        "saleType": "AUCTION"
    },
    "buyer": {
        "id": 2,
        "nombre": "María López",
        "email": "maria@example.com",
        "rol": "CONSUMER"
    },
    "seller": {
        "id": 1,
        "nombre": "Juan Pérez",
        "email": "juan@example.com",
        "rol": "PRODUCER"
    },
    "kwh": 100.0,
    "price": 0.15,
    "timestamp": "2026-04-21T10:30:00"
}
```

**Validaciones**:
- La oferta debe existir
- La oferta debe ser de tipo AUCTION
- bidAmount debe ser mayor o igual al precio base

**Errores**:
- 404: "Oferta no encontrada con id: X"
- 400: "La oferta no es de tipo subasta"
- 400: "La oferta debe ser mayor o igual al precio base: X"

---

## Módulo: iot

### Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/iot/devices/{deviceId}` | Obtener datos de dispositivo |
| GET | `/api/iot/devices` | Listar todos los dispositivos |
| GET | `/api/iot/devices/location/{location}` | Filtrar por ubicación |
| GET | `/api/iot/devices/type?type=X` | Filtrar por tipo |

---

### GET /api/iot/devices/{deviceId}

**Obtener datos de un dispositivo**

Query Parameters:
- `provider` (opcional): Nombre del proveedor

Request:
```
GET /api/iot/devices/device-001
GET /api/iot/devices/device-001?provider=smart
```

Response (200 OK):
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

**Casos de uso**:
- `getDeviceData(deviceId)` en IoTDeviceService
- `getDeviceData(deviceId, provider)` (proveedor específico)

**Nota**: Los datos son cacheados por 5 minutos

---

### GET /api/iot/devices

**Listar todos los dispositivos**

Response (200 OK):
```json
[
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
]
```

**Casos de uso**:
- `getAllDevices()` en IoTDeviceService

---

### GET /api/iot/devices/location/{location}

**Filtrar dispositivos por ubicación**

Request:
```
GET /api/iot/devices/location/home
```

Response (200 OK):
```json
[
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
]
```

**Casos de uso**:
- `getDevicesByLocation(location)` en IoTDeviceService

---

### GET /api/iot/devices/type?type=X

**Filtrar dispositivos por tipo**

Request:
```
GET /api/iot/devices/type?type=SENSOR
```

Response (200 OK):
```json
[
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
]
```

**Casos de uso**:
- `getDevicesByType(deviceType)` en IoTDeviceService

**Tipos válidos**:
- SENSOR
- ACTUATOR
- GATEWAY
- SMART_METER
- SOLAR_PANEL
- BATTERY_STORAGE
- ELECTRIC_VEHICLE_CHARGER
- THERMOSTAT
- SMART_SWITCH

---

## Códigos de Estado HTTP

| Código | Significado |
|--------|-------------|
| 200 | OK |
| 201 | Created |
| 204 | No Content |
| 400 | Bad Request |
| 404 | Not Found |
| 500 | Internal Server Error |

## Manejo de Errores

Ejemplo de respuesta de error:
```json
{
    "error": "Usuario no encontrado"
}
```

Error de validación:
```json
{
    "error": "Rol desconocido: INVALIDO"
}
```