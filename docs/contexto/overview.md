# Enerlink - Overview

## Sistema

Enerlink es una **plataforma de trading de energía** que permite a productores y consumidores intercambiar energía. El backend está construido con **Spring Boot** siguiendo **Arquitectura Hexagonal** (Ports & Adapters).

## Propósito Principal

- Gestión de usuarios con roles (Productor, Consumidor, Mixto)
- Publicación y gestión de ofertas de energía
- Integración con dispositivos IoT para monitoreo de consumo
- Predicción de consumo energético

## Módulos del Sistema

### 1. usuario
Gestión de usuarios del sistema con tres tipos de roles:
- **PRODUCER**: Produce energía
- **CONSUMER**: Consume energía
- **MIXED**: Ambos roles

### 2. energia
Gestión del marketplace de energía:
- Creación de ofertas de venta (DIRECT, AUCTION)
- Proceso de transacciones
- Decoradores para validación, fees, auditoría

### 3. iot
Integración con dispositivos IoT:
- Múltiples proveedores (SmartHome, EnergyCloud)
- Fetch de datos de dispositivos
- Cacheo de resultados (Caching Proxy)

### 4. configuracion
Servicios transversales:
- PredictionEngine (Singleton) - Predicciones de consumo

## Estilo Arquitectónico

**Arquitectura Hexagonal (Ports & Adapters)**

```
┌─────────────────────────────────────────────────────────────┐
│                    CONTROLADORES (REST)                     │
│                  (Driving Adapters - INPUT)                │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                  SERVICIOS DE APLICACIÓN                    │
│                    (Use Cases / Casos de Uso)               │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                      DOMINIO                                 │
│           (Entities, Ports, Domain Services)                │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                   PUERTOS (OUTPUT)                          │
│              (Repository Interfaces / Ports)                │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│               ADAPTADORES (INFRAESTRUCTURA)                 │
│              (JPA Repositories, IoT Clients)                │
└─────────────────────────────────────────────────────────────┘
```

## Tecnologías

- **Framework**: Spring Boot 4.0.3
- **Base de datos**: H2 (file-based)
- **ORM**: Hibernate / Spring Data JPA
- **Lenguaje**: Java 21

## Patrones Implementados

| Patrón | Módulo | Propósito |
|--------|--------|-----------|
| Singleton | configuracion | PredictionEngine única instancia |
| Factory Method | usuario, energia | Creación de objetos según tipo |
| Abstract Factory | energia | Factory de factories |
| Fluent Builder | energia | Construcción de Transaction |
| Prototype | energia | Clonación de EnergyOffer |
| Adapter | iot | Adaptadores de proveedores |
| Bridge | iot | Separación abstracción/implementación |
| Decorator | energia | Cadena de procesamiento de transacciones |
| Facade | energia | Interfaz unificada EnergyTrading |
| Composite | iot | Agregación de adaptadores |
| Proxy | iot | Cache de llamadas a proveedores |

## Base URL

```
http://localhost:8080
```

## Recursos H2 Console

- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: jdbc:h2:file:./data/enerlinkdb
- **Username**: sa
- **Password**: (vacío)
