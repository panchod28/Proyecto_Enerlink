# Integration Guide - Enerlink Backend

## Para Desarrolladores Frontend

Esta guía te permite integrar tu aplicación React (o cualquier frontend) con el backend de Enerlink sin necesidad de leer el código Java.

---

## 1. Configuración Base

### Base URL
```
http://localhost:8080
```

### Headers Requeridos
```javascript
// Content-Type para requests con body
Content-Type: application/json
```

### Dependencia (React + fetch)

No necesitas librerías adicionales. Usa fetch nativo o axios.

---

## 2. Autenticación

**Estado actual**: NO implementada.

El backend NO tiene sistema de autenticación. Cualquier request funciona sin tokens.

> **Nota**: Si agregas autenticación (JWT, OAuth), esta guía deberá actualizarse.

---

## 3. Endpoints de Usuario

### Crear Usuario

```javascript
// POST /api/usuarios
async function crearUsuario(nombre, email, rol) {
  const response = await fetch('http://localhost:8080/api/usuarios', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ nombre, email, rol })
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error);
  }
  
  return response.json();
}

// Uso
crearUsuario('Juan Pérez', 'juan@example.com', 'PRODUCER')
  .then(user => console.log(user));
```

### Roles válidos
- `PRODUCER` - Produce energía
- `CONSUMER` - Consume energía
- `MIXED` - Ambos roles

### Listar Usuarios

```javascript
// GET /api/usuarios
async function listarUsuarios() {
  const response = await fetch('http://localhost:8080/api/usuarios');
  return response.json();
}
```

### Obtener Usuario por ID

```javascript
// GET /api/usuarios/:id
async function obtenerUsuario(id) {
  const response = await fetch(`http://localhost:8080/api/usuarios/${id}`);
  
  if (response.status === 404) {
    throw new Error('Usuario no encontrado');
  }
  
  return response.json();
}
```

### Actualizar Usuario

```javascript
// PUT /api/usuarios/:id
async function actualizarUsuario(id, nombre, email, rol) {
  const response = await fetch(`http://localhost:8080/api/usuarios/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ nombre, email, rol })
  });
  
  return response.json();
}
```

### Eliminar Usuario

```javascript
// DELETE /api/usuarios/:id
async function eliminarUsuario(id) {
  await fetch(`http://localhost:8080/api/usuarios/${id}`, {
    method: 'DELETE'
  });
}
```

---

## 4. Endpoints de Ofertas de Energía

### Crear Oferta

```javascript
// POST /api/offers
async function crearOferta(saleType, producerId, kwh, price) {
  const response = await fetch('http://localhost:8080/api/offers', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      saleType,    // "DIRECT" o "AUCTION"
      producerId,  // ID del usuario producer
      kwh,         // Cantidad de energía
      price        // Precio por kWh
    })
  });
  
  return response.json();
}

// Uso
crearOferta('DIRECT', 1, 100.0, 0.12)
  .then(offer => console.log(offer));
```

### Listar Ofertas

```javascript
// GET /api/offers
async function listarOfertas() {
  const response = await fetch('http://localhost:8080/api/offers');
  return response.json();
}
```

### Obtener Oferta por ID

```javascript
// GET /api/offers/:id
async function obtenerOferta(id) {
  const response = await fetch(`http://localhost:8080/api/offers/${id}`);
  
  if (response.status === 404) {
    throw new Error('Oferta no encontrada');
  }
  
  return response.json();
}
```

### Eliminar Oferta

```javascript
// DELETE /api/offers/:id
async function eliminarOferta(id) {
  await fetch(`http://localhost:8080/api/offers/${id}`, {
    method: 'DELETE'
  });
}
```

### Ejecutar Venta Directa

```javascript
// POST /api/offers/:id/sale
async function ejecutarVentaDirecta(offerId, buyerId) {
  const response = await fetch(`http://localhost:8080/api/offers/${offerId}/sale`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ buyerId })
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || 'Error ejecutando venta');
  }
  
  return response.json();  // Transaction
}

// Uso
const transaction = await ejecutarVentaDirecta(1, 2);
```

### Ejecutar Puja en Subasta

```javascript
// POST /api/offers/:id/auction
async function ejecutarPuja(offerId, buyerId, bidAmount) {
  const response = await fetch(`http://localhost:8080/api/offers/${offerId}/auction`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ buyerId, bidAmount })
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || 'Error ejecutando puja');
  }
  
  return response.json();  // Transaction
}

// Uso
const transaction = await ejecutarPuja(1, 2, 0.15);
```

---

## 5. Endpoints de Dispositivos IoT

### Obtener Dispositivo

```javascript
// GET /api/iot/devices/:deviceId
async function obtenerDispositivo(deviceId, provider = null) {
  let url = `http://localhost:8080/api/iot/devices/${deviceId}`;
  
  if (provider) {
    url += `?provider=${provider}`;
  }
  
  const response = await fetch(url);
  return response.json();
}
```

### Listar Todos los Dispositivos

```javascript
// GET /api/iot/devices
async function listarDispositivos() {
  const response = await fetch('http://localhost:8080/api/iot/devices');
  return response.json();
}
```

### Filtrar por Ubicación

```javascript
// GET /api/iot/devices/location/:location
async function obtenerPorUbicacion(location) {
  const response = await fetch(
    `http://localhost:8080/api/iot/devices/location/${location}`
  );
  return response.json();
}

// Uso
obtenerPorUbicacion('home');
```

### Filtrar por Tipo

```javascript
// GET /api/iot/devices/type?type=TIPO
async function obtenerPorTipo(tipo) {
  const response = await fetch(
    `http://localhost:8080/api/iot/devices/type?type=${tipo}`
  );
  return response.json();
}

// Tipos válidos: SENSOR, ACTUATOR, GATEWAY, SMART_METER, 
//                SOLAR_PANEL, BATTERY_STORAGE, ELECTRIC_VEHICLE_CHARGER,
//                THERMOSTAT, SMART_SWITCH
```

---

## 6. Manejo de Errores

### Estructura de Error

```javascript
try {
  const user = await obtenerUsuario(999);
} catch (error) {
  // Error del backend
  // response.status === 404
  console.log(error.message); // "Usuario no encontrado"
}
```

### Códigos de Estado

| Código | Significado | Cómo manejarlo |
|--------|------------|----------------|
| 200 | OK | Procesar respuesta |
| 201 | Created | Recurso creado |
| 204 | No Content | Sin respuesta (DELETE exitoso) |
| 400 | Bad Request | Datos inválidos |
| 404 | Not Found | Recurso no existe |
| 500 | Error Server | Error interno |

### Ejemplo de Manejo

```javascript
async function fetchConManejo(url, options = {}) {
  try {
    const response = await fetch(url, options);
    
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.error || `HTTP ${response.status}`);
    }
    
    // 204 No Content
    if (response.status === 204) {
      return null;
    }
    
    return response.json();
    
  } catch (error) {
    console.error('Error:', error.message);
    throw error;
  }
}
```

---

## 7. Flujos Comunes

### Flujo 1: Registro de Productor + Publicar Oferta

```javascript
// Paso 1: Crear usuario producer
const producer = await crearUsuario('Energía Solar SA', 'contacto@energia.com', 'PRODUCER');
const producerId = producer.id;

// Paso 2: Crear oferta de energía
const oferta = await crearOferta('DIRECT', producerId, 500, 0.10);

console.log('Oferta creada:', oferta.id);
```

### Flujo 2: Dashboard de Dispositivos IoT

```javascript
// Cargar todos los dispositivos
const dispositivos = await listarDispositivos();

// Filtrar por tipo para dashboard
const sensores = await obtenerPorTipo('SENSOR');
const paneles = await obtenerPorTipo('SOLAR_PANEL');

// Obtener dato de dispositivo específico
const dato = await obtenerDispositivo('device-001');
```

### Flujo 3: Listar y Seleccionar Oferta

```javascript
// Listar ofertas disponibles
const ofertas = await listarOfertas();

// Filtrar por tipo (opcional)
const ofertasDirectas = ofertas.filter(o => o.saleType === 'DIRECT');

// Seleccionar una
const ofertaElegida = await obtenerOferta(ofertasDirectas[0].id);
```

---

## 8. Consideraciones de Rendimiento

### Cache de IoT

El backend cachea resultados de IoT por **5 minutos**.

```javascript
//Primera llamada - cache miss (lento)
const dato1 = await obtenerDispositivo('device-001');

//Segunda llamada - cache hit (rápido)
const dato2 = await obtenerDispositivo('device-001'); // misma respuesta

// Después de 5 minutos - cache expira
```

### Optimizaciones Recomendadas

1. **IoT**: No llames constantemente los mismos endpoints
2. **Listas**: **PAGINACIÓN NO SOPORTADA** - El backend retorna todas las filas. Implementa paginación del lado del cliente si es necesario.
3. **Carga inicial**: Carga datos esenciales primero, detalles después

---

## 9. Ejemplo: React Hook Personalizado

```javascript
// hooks/useApi.js
import { useState, useCallback } from 'react';

const BASE_URL = 'http://localhost:8080/api';

export function useApi() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const request = useCallback(async (url, options = {}) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await fetch(`${BASE_URL}${url}`, {
        headers: { 'Content-Type': 'application/json' },
        ...options
      });
      
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || `Error ${response.status}`);
      }
      
      return response.status === 204 ? null : response.json();
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  return { request, loading, error };
}

// Uso en componente
function UsuarioList() {
  const { request, loading, error } = useApi();
  const [usuarios, setUsuarios] = useState([]);

  useEffect(() => {
    request('/usuarios').then(setUsuarios);
  }, [request]);

  if (loading) return <div>Cargando...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <ul>
      {usuarios.map(u => <li key={u.id}>{u.nombre}</li>)}
    </ul>
  );
}
```

---

## 10. Quick Reference

| Recurso | Método | Endpoint | Body |
|---------|--------|----------|------|
| Usuarios | POST | /api/usuarios | `{nombre, email, rol}` |
| Usuarios | GET | /api/usuarios | - |
| Usuario | GET | /api/usuarios/{id} | - |
| Usuario | PUT | /api/usuarios/{id} | `{nombre, email, rol}` |
| Usuario | DELETE | /api/usuarios/{id} | - |
| Ofertas | POST | /api/offers | `{saleType, producerId, kwh, price}` |
| Ofertas | GET | /api/offers | - |
| Oferta | GET | /api/offers/{id} | - |
| Oferta | DELETE | /api/offers/{id} | - |
| Venta directa | POST | /api/offers/{id}/sale | `{buyerId}` |
| Subasta | POST | /api/offers/{id}/auction | `{buyerId, bidAmount}` |
| Dispositivos | GET | /api/iot/devices/{deviceId} | - |
| Dispositivos | GET | /api/iot/devices | - |
| Por ubicación | GET | /api/iot/devices/location/{loc} | - |
| Por tipo | GET | /api/iot/devices/type?type=X | - |