# Reporte 2 — Índice de Eficiencia de Productores

## Contexto de negocio
No basta con saber cuánto vendió cada productor. Este reporte construye un
score de eficiencia que combina tasa de conversión, velocidad de venta y
rendimiento de precio para clasificar a cada productor en la plataforma.
La información no existe en ninguna tabla — se construye cruzando energy_offer
y transactions con lógica de negocio encima.

---

## FASE 1 — BACKEND

### Archivos a leer antes de implementar
```
src/main/java/com/enerlink/enerlink/energia/dominio/modelo/EnergyOffer.java
src/main/java/com/enerlink/enerlink/energia/infraestructura/persistencia/EnergyOfferEntity.java
src/main/java/com/enerlink/enerlink/energia/infraestructura/persistencia/EnergyOfferJpaRepository.java
src/main/java/com/enerlink/enerlink/energia/infraestructura/persistencia/EnergyOfferRepositoryAdapter.java
src/main/java/com/enerlink/enerlink/energia/aplicacion/servicio/EnergyOfferService.java
src/main/java/com/enerlink/enerlink/energia/infraestructura/persistencia/TransactionJpaRepository.java
src/main/java/com/enerlink/enerlink/energia/infraestructura/controlador/ReportController.java
src/main/resources/db/migration/ (ver el número del último V disponible)
```

---

### Paso 1 — Agregar campo `createdAt` a EnergyOfferEntity

**Archivo:** `src/main/java/com/enerlink/enerlink/energia/infraestructura/persistencia/EnergyOfferEntity.java`

Agregar:
```java
@Column(name = "created_at")
private java.time.LocalDateTime createdAt;
```
Con getter y setter.

---

### Paso 2 — Agregar campo `createdAt` a EnergyOffer (dominio)

**Archivo:** `src/main/java/com/enerlink/enerlink/energia/dominio/modelo/EnergyOffer.java`

Agregar:
```java
private java.time.LocalDateTime createdAt;
```
Con getter y setter.

---

### Paso 3 — Poblar `createdAt` en EnergyOfferService

**Archivo:** `src/main/java/com/enerlink/enerlink/energia/aplicacion/servicio/EnergyOfferService.java`

En el método `createOffer()`, antes de llamar a `repository.save(offer)`:
```java
offer.setCreatedAt(java.time.LocalDateTime.now());
```

---

### Paso 4 — Actualizar EnergyOfferRepositoryAdapter

**Archivo:** `src/main/java/com/enerlink/enerlink/energia/infraestructura/persistencia/EnergyOfferRepositoryAdapter.java`

En el método `save()`: agregar `entity.setCreatedAt(offer.getCreatedAt())`.
En todos los métodos de mapeo entity → domain: agregar `offer.setCreatedAt(entity.getCreatedAt())`.

---

### Paso 5 — Migración Flyway

**Archivo a crear:** `src/main/resources/db/migration/V4__add_created_at_to_energy_offer.sql`

(Usar V3 si el reporte 1 no se implementó antes, o V4 si ya existe V3)

```sql
ALTER TABLE energy_offer ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;

-- Backfill: asignar fecha aproximada a ofertas existentes
UPDATE energy_offer SET created_at = NOW() WHERE created_at IS NULL;
```

---

### Paso 6 — Query de eficiencia en ReportController

**Archivo:** `src/main/java/com/enerlink/enerlink/energia/infraestructura/controlador/ReportController.java`

Agregar endpoint al controlador existente:

```java
@GetMapping("/producer-efficiency")
public ResponseEntity<?> getProducerEfficiency() {

    List<Object[]> rows = transactionRepository.findProducerEfficiencyData();

    List<Map<String, Object>> result = rows.stream().map(row -> {
        String producerName    = (String)  row[0];
        long   totalOffers     = ((Number) row[1]).longValue();
        long   soldOffers      = ((Number) row[2]).longValue();
        double avgPriceSold    = ((Number) row[3]).doubleValue();
        double avgPriceBase    = ((Number) row[4]).doubleValue();
        double avgDaysToSell   = ((Number) row[5]).doubleValue();

        double conversionRate  = totalOffers > 0
            ? (double) soldOffers / totalOffers * 100 : 0;
        double pricePerformance = avgPriceBase > 0
            ? (avgPriceSold / avgPriceBase - 1) * 100 : 0;

        // Score: 40% conversión + 40% rendimiento precio + 20% velocidad (inversa)
        double speedScore = avgDaysToSell > 0 ? Math.max(0, 100 - avgDaysToSell * 5) : 100;
        double score = (conversionRate * 0.4) + (Math.min(pricePerformance + 100, 200) * 0.2) + (speedScore * 0.4);

        String classification = score >= 70 ? "EFICIENTE"
            : score >= 40 ? "PROMEDIO" : "INEFICIENTE";

        return Map.of(
            "producerName",     producerName,
            "totalOffers",      totalOffers,
            "soldOffers",       soldOffers,
            "conversionRate",   Math.round(conversionRate * 100.0) / 100.0,
            "avgPriceSold",     Math.round(avgPriceSold * 100.0) / 100.0,
            "avgPriceBase",     Math.round(avgPriceBase * 100.0) / 100.0,
            "pricePerformance", Math.round(pricePerformance * 100.0) / 100.0,
            "avgDaysToSell",    Math.round(avgDaysToSell * 10.0) / 10.0,
            "score",            Math.round(score * 10.0) / 10.0,
            "classification",   classification
        );
    })
    .sorted((a, b) -> Double.compare(
        ((Number) b.get("score")).doubleValue(),
        ((Number) a.get("score")).doubleValue()))
    .collect(Collectors.toList());

    return ResponseEntity.ok(Map.of(
        "producers", result,
        "summary", Map.of(
            "totalProducers",    result.size(),
            "efficient",         result.stream().filter(r -> "EFICIENTE".equals(r.get("classification"))).count(),
            "average",           result.stream().filter(r -> "PROMEDIO".equals(r.get("classification"))).count(),
            "inefficient",       result.stream().filter(r -> "INEFICIENTE".equals(r.get("classification"))).count()
        )
    ));
}
```

---

### Paso 7 — Query en TransactionJpaRepository

**Archivo:** `src/main/java/com/enerlink/enerlink/energia/infraestructura/persistencia/TransactionJpaRepository.java`

Agregar:

```java
@Query(value = """
    SELECT
        u.nombre                                              AS producerName,
        COUNT(DISTINCT eo.id)                                 AS totalOffers,
        COUNT(DISTINCT t.id)                                  AS soldOffers,
        COALESCE(AVG(t.price), 0)                             AS avgPriceSold,
        COALESCE(AVG(eo.price), 0)                            AS avgPriceBase,
        COALESCE(AVG(
            EXTRACT(EPOCH FROM (t.timestamp - eo.created_at)) / 86400
        ), 0)                                                 AS avgDaysToSell
    FROM users u
    JOIN energy_offer eo ON eo.producer_id = u.id
    LEFT JOIN transactions t ON t.seller_id = u.id
    WHERE u.rol IN ('PRODUCER', 'MIXED')
    GROUP BY u.id, u.nombre
    HAVING COUNT(DISTINCT eo.id) > 0
    ORDER BY soldOffers DESC
    """, nativeQuery = true)
List<Object[]> findProducerEfficiencyData();
```

---

### Endpoint final expuesto

```
GET /api/reports/producer-efficiency
```

### Respuesta esperada
```json
{
  "summary": {
    "totalProducers": 65,
    "efficient": 18,
    "average": 32,
    "inefficient": 15
  },
  "producers": [
    {
      "producerName": "Carlos García Ramírez",
      "totalOffers": 14,
      "soldOffers": 11,
      "conversionRate": 78.57,
      "avgPriceSold": 145.30,
      "avgPriceBase": 130.00,
      "pricePerformance": 11.77,
      "avgDaysToSell": 0.8,
      "score": 82.4,
      "classification": "EFICIENTE"
    }
  ]
}
```

---

### Archivos modificados o creados
| Archivo | Tipo |
|---------|------|
| EnergyOffer.java | Modificado |
| EnergyOfferEntity.java | Modificado |
| EnergyOfferService.java | Modificado |
| EnergyOfferRepositoryAdapter.java | Modificado |
| V4__add_created_at_to_energy_offer.sql | Creado |
| ReportController.java | Modificado |
| TransactionJpaRepository.java | Modificado |

---

## FASE 2 — FRONTEND (implementar después de tener el backend listo)

### Vista: `/reports/producer-efficiency`

**Componentes necesarios:**
- 3 KPI cards: Total productores / Eficientes / Ineficientes
- Tabla principal con columnas:
  Productor | Ofertas | Vendidas | Conversión % | Precio base | Precio obtenido | Rendimiento % | Velocidad | Score | Badge clasificación
- Badge con color por clasificación: verde=EFICIENTE, amarillo=PROMEDIO, rojo=INEFICIENTE
- BarChart (recharts) con top 10 productores por score

**API call:**
```typescript
getProducerEfficiency(): Promise<ProducerEfficiencyReport>
```
`GET /api/reports/producer-efficiency`
