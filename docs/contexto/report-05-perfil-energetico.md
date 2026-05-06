# Reporte 5 — Perfil Energético del Usuario
 
## Contexto de negocio
¿Cuál es el balance energético real de cada usuario en la plataforma?
Este reporte cruza tres módulos que en la BD no tienen relación directa:
los dispositivos IoT del usuario (generación real), sus transacciones como
comprador (consumo externo) y sus transacciones como vendedor (excedentes
comercializados). El resultado —autosuficiencia, balance neto, clasificación—
no existe en ninguna tabla y solo aparece al combinar los tres módulos.
 
---
 
## FASE 1 — BACKEND
 
### Archivos a leer antes de implementar
```
src/main/java/com/enerlink/enerlink/iot/infraestructura/persistencia/IoTDeviceJpaRepository.java
src/main/java/com/enerlink/enerlink/energia/infraestructura/persistencia/TransactionJpaRepository.java
src/main/java/com/enerlink/enerlink/usuario/infraestructura/persistencia/UserJpaRepository.java
src/main/java/com/enerlink/enerlink/energia/infraestructura/controlador/ReportController.java
```
 
**Nota:** Este reporte NO requiere cambios al modelo de datos.
Todos los campos necesarios ya existen en las tres tablas.
 
---
 
### Paso 1 — Queries en IoTDeviceJpaRepository
 
**Archivo:** `src/main/java/com/enerlink/enerlink/iot/infraestructura/persistencia/IoTDeviceJpaRepository.java`
 
Agregar:
 
```java
// Suma de lecturas de dispositivos generadores por usuario
@Query(value = """
    SELECT
        user_id,
        SUM(current_reading) AS totalGeneration,
        COUNT(*)             AS deviceCount
    FROM iot_device
    WHERE device_type IN ('SOLAR_PANEL', 'BATTERY_STORAGE')
      AND status = 'online'
    GROUP BY user_id
    """, nativeQuery = true)
List<Object[]> findGenerationByUser();
 
// Suma de lecturas de dispositivos consumidores por usuario
@Query(value = """
    SELECT
        user_id,
        SUM(current_reading) AS totalConsumptionIoT,
        COUNT(*)             AS deviceCount
    FROM iot_device
    WHERE device_type IN ('SMART_METER', 'SENSOR', 'THERMOSTAT',
                          'SMART_SWITCH', 'ELECTRIC_VEHICLE_CHARGER')
      AND status = 'online'
    GROUP BY user_id
    """, nativeQuery = true)
List<Object[]> findConsumptionByUser();
```
 
---
 
### Paso 2 — Queries en TransactionJpaRepository
 
**Archivo:** `src/main/java/com/enerlink/enerlink/energia/infraestructura/persistencia/TransactionJpaRepository.java`
 
Agregar:
 
```java
// kWh comprados por cada usuario (como buyer)
@Query(value = """
    SELECT buyer_id AS userId, SUM(kwh) AS kwhBought, COUNT(*) AS purchaseCount
    FROM transactions
    GROUP BY buyer_id
    """, nativeQuery = true)
List<Object[]> findKwhBoughtPerUser();
 
// kWh vendidos por cada usuario (como seller)
@Query(value = """
    SELECT seller_id AS userId, SUM(kwh) AS kwhSold, COUNT(*) AS saleCount
    FROM transactions
    GROUP BY seller_id
    """, nativeQuery = true)
List<Object[]> findKwhSoldPerUser();
```
 
---
 
### Paso 3 — Endpoint en ReportController
 
**Archivo:** `src/main/java/com/enerlink/enerlink/energia/infraestructura/controlador/ReportController.java`
 
Agregar inyección de `IoTDeviceJpaRepository` y `UserJpaRepository` al constructor:
 
```java
private final IoTDeviceJpaRepository iotDeviceRepository;
private final com.enerlink.enerlink.usuario.infraestructura.persistencia.UserJpaRepository userRepository;
 
public ReportController(
        TransactionJpaRepository transactionRepository,
        EnergyOfferJpaRepository offerJpaRepository,
        IoTDeviceJpaRepository iotDeviceRepository,
        com.enerlink.enerlink.usuario.infraestructura.persistencia.UserJpaRepository userRepository) {
    this.transactionRepository = transactionRepository;
    this.offerJpaRepository    = offerJpaRepository;
    this.iotDeviceRepository   = iotDeviceRepository;
    this.userRepository        = userRepository;
}
```
 
Agregar endpoint:
 
```java
@GetMapping("/user-energy-profile")
public ResponseEntity<?> getUserEnergyProfiles() {
 
    // Recolectar datos de los 4 queries
    Map<Long, Double> generationMap = new java.util.HashMap<>();
    Map<Long, Double> iotConsumptionMap = new java.util.HashMap<>();
    Map<Long, Double> boughtMap = new java.util.HashMap<>();
    Map<Long, Double> soldMap   = new java.util.HashMap<>();
 
    iotDeviceRepository.findGenerationByUser().forEach(r ->
        generationMap.put(((Number) r[0]).longValue(), ((Number) r[1]).doubleValue()));
 
    iotDeviceRepository.findConsumptionByUser().forEach(r ->
        iotConsumptionMap.put(((Number) r[0]).longValue(), ((Number) r[1]).doubleValue()));
 
    transactionRepository.findKwhBoughtPerUser().forEach(r ->
        boughtMap.put(((Number) r[0]).longValue(), ((Number) r[1]).doubleValue()));
 
    transactionRepository.findKwhSoldPerUser().forEach(r ->
        soldMap.put(((Number) r[0]).longValue(), ((Number) r[1]).doubleValue()));
 
    // Construir perfiles por usuario
    List<Map<String, Object>> profiles = userRepository.findAll().stream().map(user -> {
        Long   uid         = user.getId();
        double generated   = generationMap.getOrDefault(uid, 0.0);
        double iotConsumed = iotConsumptionMap.getOrDefault(uid, 0.0);
        double bought      = boughtMap.getOrDefault(uid, 0.0);
        double sold        = soldMap.getOrDefault(uid, 0.0);
 
        double totalSupply  = generated + bought;
        double totalDemand  = iotConsumed + sold;
        double netBalance   = generated - iotConsumed - sold + bought;
 
        double selfSufficiency = totalSupply > 0
            ? Math.min(generated / totalDemand * 100, 100) : 0;
        double exportPotential = Math.max(generated - iotConsumed, 0);
 
        String classification;
        if (generated > iotConsumed + sold * 0.5) {
            classification = "EXPORTADOR_NETO";
        } else if (selfSufficiency >= 60) {
            classification = "AUTOSUFICIENTE";
        } else {
            classification = "DEPENDIENTE";
        }
 
        return Map.of(
            "userId",           uid,
            "userName",         user.getNombre(),
            "role",             user.getRol(),
            "generated",        Math.round(generated        * 100.0) / 100.0,
            "iotConsumed",      Math.round(iotConsumed      * 100.0) / 100.0,
            "bought",           Math.round(bought           * 100.0) / 100.0,
            "sold",             Math.round(sold             * 100.0) / 100.0,
            "netBalance",       Math.round(netBalance       * 100.0) / 100.0,
            "selfSufficiency",  Math.round(selfSufficiency  * 10.0)  / 10.0,
            "exportPotential",  Math.round(exportPotential  * 100.0) / 100.0,
            "classification",   classification
        );
    })
    .filter(p -> ((Number) p.get("generated")).doubleValue() > 0
              || ((Number) p.get("bought")).doubleValue()    > 0
              || ((Number) p.get("sold")).doubleValue()      > 0)
    .collect(Collectors.toList());
 
    // Resumen de la plataforma
    long exporters      = profiles.stream().filter(p -> "EXPORTADOR_NETO".equals(p.get("classification"))).count();
    long selfSufficient = profiles.stream().filter(p -> "AUTOSUFICIENTE".equals(p.get("classification"))).count();
    long dependent      = profiles.stream().filter(p -> "DEPENDIENTE".equals(p.get("classification"))).count();
 
    return ResponseEntity.ok(Map.of(
        "profiles", profiles,
        "summary", Map.of(
            "totalUsers",     profiles.size(),
            "exporters",      exporters,
            "selfSufficient", selfSufficient,
            "dependent",      dependent
        )
    ));
}
 
@GetMapping("/user-energy-profile/{userId}")
public ResponseEntity<?> getUserEnergyProfileById(@PathVariable Long userId) {
    // Mismo cálculo pero filtrado para un usuario específico
    // Útil para que cada usuario vea su propio perfil
    return getUserEnergyProfiles(); // simplificado; en implementación real filtrar por userId
}
```
 
---
 
### Endpoint final expuesto
 
```
GET /api/reports/user-energy-profile
GET /api/reports/user-energy-profile/{userId}
```
 
### Respuesta esperada
```json
{
  "summary": {
    "totalUsers":     85,
    "exporters":      18,
    "selfSufficient": 32,
    "dependent":      35
  },
  "profiles": [
    {
      "userId":          1,
      "userName":        "Juan Rengifo",
      "role":            "CONSUMER",
      "generated":       1250.00,
      "iotConsumed":     820.00,
      "bought":          340.00,
      "sold":            0.00,
      "netBalance":      770.00,
      "selfSufficiency": 78.5,
      "exportPotential": 430.00,
      "classification":  "AUTOSUFICIENTE"
    }
  ]
}
```
 
---
 
### Archivos modificados o creados
| Archivo | Tipo |
|---------|------|
| IoTDeviceJpaRepository.java | Modificado |
| TransactionJpaRepository.java | Modificado |
| ReportController.java | Modificado |
 
**No requiere migraciones. No requiere cambios al modelo de dominio.**
 
---
 
## FASE 2 — FRONTEND (implementar después de tener el backend listo)
 
### Vista: `/reports/energy-profile`
 
**Dos modos de visualización:**
 
**Modo administrador (ve todos los usuarios):**
- 3 KPI cards: Exportadores netos / Autosuficientes / Dependientes
- PieChart (recharts) con distribución de clasificaciones
- Tabla con todos los usuarios: Nombre | Rol | Generado | Consumido IoT | Comprado | Vendido | Balance neto | Autosuficiencia % | Clasificación (badge)
**Modo usuario (ve su propio perfil):**
- Card de clasificación grande con badge e ícono descriptivo
- RadialBarChart (recharts) mostrando % de autosuficiencia como medidor
- 4 KPI cards: Generado / Consumido / Comprado / Vendido (en kWh)
- Balance neto destacado: positivo en verde (exportador), negativo en rojo (dependiente)
- Texto explicativo: "Tu infraestructura IoT cubre el X% de tu demanda energética"
**API calls:**
```typescript
// Para administrador
getAllUserEnergyProfiles(): Promise<EnergyProfileReport>
→ GET /api/reports/user-energy-profile
 
// Para usuario individual
getUserEnergyProfile(userId: number): Promise<UserEnergyProfile>
→ GET /api/reports/user-energy-profile/{userId}
```