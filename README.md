# ⚡ Enerlink – Plataforma de Intercambio Energético

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/SpringBoot-Backend-green)
![Architecture](https://img.shields.io/badge/Architecture-Hexagonal-orange)
![Patterns](https://img.shields.io/badge/Design%20Patterns-GoF-purple)
![Status](https://img.shields.io/badge/Status-Academic%20Project-lightgrey)

---

## 👨‍💻 Autor

**Juan David Rengifo Chavez**  
Ingeniería de Sistemas  
Unidades Tecnológicas de Santander  
2026

---

## 📚 Contexto Académico

Proyecto desarrollado para la asignatura:

**Patrones de Software**  
Docente: Eliecer Montero Ojeda  
UTS – 2026

---

## 📌 Descripción del Proyecto

Enerlink es una plataforma backend orientada al intercambio de excedentes energéticos entre usuarios productores y consumidores.

Permite:
- Publicación de ofertas energéticas
- Compra directa de energía
- Sistema de subastas
- Integración con dispositivos IoT
- Predicción energética mediante estrategias intercambiables

El proyecto tiene un enfoque estrictamente académico y está orientado a la aplicación práctica de:

- Arquitectura Hexagonal (Ports & Adapters)
- Patrones de Diseño GoF
- Principios SOLID
- Pruebas automatizadas
- Buenas prácticas de ingeniería de software

---

## 🎯 Objetivo General

Implementar la plataforma Enerlink aplicando Arquitectura Hexagonal y patrones de diseño GoF, garantizando una solución modular, extensible y validada mediante pruebas automatizadas.

---

## 🏗 Arquitectura del Sistema

Enerlink implementa **Arquitectura Hexagonal (Ports & Adapters)** con separación estricta de:

### 🔹 Dominio
- Entidades del negocio
- Reglas de negocio
- Enumeraciones
- Lógica central

### 🔹 Aplicación
- Casos de uso
- Orquestación de operaciones
- Definición de puertos

### 🔹 Infraestructura
- Controladores REST
- Persistencia
- Adaptadores IoT
- Logging
- Configuración Spring Boot

### 🎯 Beneficios

- Bajo acoplamiento
- Alta cohesión
- Facilidad de pruebas unitarias
- Independencia del framework
- Extensibilidad futura

---

## 🧠 Patrones de Diseño Aplicados

Enerlink implementa **8 patrones GoF** distribuidos en:

### 🔹 Creacionales
- Factory Method
- Builder

### 🔹 Estructurales
- Adapter
- Facade

### 🔹 Comportamiento
- Strategy
- Observer

### 🔹 Adicionales
- Singleton
- Template Method

Cada patrón fue aplicado respondiendo a una necesidad real del dominio y no como implementación forzada.

---

## 🧩 Principios SOLID

El sistema fue diseñado bajo los principios:

- Single Responsibility
- Open/Closed
- Liskov Substitution
- Interface Segregation
- Dependency Inversion

Especial énfasis en:
- Bajo acoplamiento
- Alta cohesión
- Separación clara entre capas

---

## 🧱 Modelo de Dominio

Entidades principales:

- User
- EnergyOffer
- Auction
- Bid
- Transaction
- Prediction
- IoTDeviceData

Reglas clave:
- Una oferta solo puede venderse una vez.
- Las subastas solo aceptan pujas mientras estén abiertas.
- Las pujas deben superar el valor actual.
- La predicción depende de una estrategia seleccionada.

---

## 📊 Requisitos del Sistema

### Funcionales
- Gestión de usuarios
- Publicación de ofertas
- Compra directa
- Subastas
- Registro de transacciones
- Integración IoT
- Predicción energética
- Historial de operaciones

### No Funcionales
- Arquitectura Hexagonal obligatoria
- Uso mínimo de 8 patrones GoF
- Cumplimiento de SOLID
- Cobertura mínima 80% en pruebas unitarias
- Tiempo de respuesta menor a 2 segundos
- Logging estructurado

---

## 🧪 Estrategia de Pruebas

Niveles implementados:

- Pruebas Unitarias (Dominio)
- Pruebas de Integración
- Pruebas Funcionales

Herramientas:
- JUnit
- Mockito
- Maven / Gradle

Enfoque:
Validación prioritaria del núcleo de dominio respetando la arquitectura hexagonal.

---

## 🔁 Control de Versiones

Estrategia de ramas:

- main → versión estable
- develop → integración
- feature/* → nuevas funcionalidades

Convención de commits:

- feat:
- fix:
- refactor:
- test:
- docs:
- chore:

---

## 📈 Monitoreo y Logging

Framework utilizado:
- SLF4J + Logback

Niveles:
- ERROR
- WARN
- INFO
- DEBUG

Eventos monitoreados:
- Registro de usuarios
- Publicación de ofertas
- Cierre de subastas
- Registro de transacciones
- Integración IoT
- Manejo de excepciones

---

## 🚀 Tecnologías Utilizadas

- Java 17
- Spring Boot
- JPA
- Maven
- JUnit
- Mockito
- Git

---

> Enerlink demuestra cómo estructurar un sistema de comercio energético aplicando arquitectura moderna y patrones de diseño de forma coherente y justificada.
