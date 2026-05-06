# Reporte 4 — Volumen de Energía Comercializada

## Contexto de negocio
¿Cuántos kWh se mueven por la plataforma? Este reporte muestra el volumen
total de energía transaccionada por período, separado por tipo de venta.
Revela picos de actividad, tendencias de crecimiento y el peso relativo
de ventas directas vs subastas en el flujo energético de la plataforma.
Es el reporte con menos cambios al proyecto porque todos los datos ya existen.

---

## FASE 1 — BACKEND

### Archivos a leer antes de implementar
```
src/main/java/com/enerlink/enerlink/energia/infraestructura/persistencia/TransactionJpaRepository.java
src/main/java/com/enerlink/enerlink/energia/infraestructura/controlador/ReportController.java
```

**Nota:** Este reporte NO requiere cambios al modelo de datos.
Todos los campos necesarios (kwh, timestamp, offer_id, sale_type) ya existen.

---

### Paso 1 — Query de volumen en TransactionJpaRepository

**Archivo:** `src/main/java/com/enerlink/enerlink/energia/infraestructura/persistencia/TransactionJpaRepository.java`

Agregar:

```java
@Query(value = """
    SELECT
        CASE
            WHEN :groupBy = 'day'   THEN TO_CHAR(t.timestamp, 'YYYY-MM-DD')
            WHEN :groupBy = 'week'  THEN TO_CHAR(DATE_TRUNC('week', t.timestamp), 'YYYY-MM-DD')
            WHEN :groupBy = 'month' THEN TO_CHAR(t.timestamp, 'YYYY-MM')
        END                           AS period,
        eo.sale_type                  AS saleType,
        SUM(t.kwh)                    AS totalKwh,
        COUNT(t.id)                   AS transactionCount,
        AVG(t.kwh)                    AS avgKwh,
        SUM(t.kwh * t.price)          AS totalValue
    FROM transactions t
    JOIN energy_offer eo ON eo.id = t.offer_id
    GROUP BY 1, 2
    ORDER BY 1 ASC, 2
    """, nativeQuery = true)
List<Object[]> findVolumeByPeriodAndType(@Param("groupBy") String groupBy);
```

---

### Paso 2 — Endpoint en ReportController

**Archivo:** `src/main/java/com/enerlink/enerlink/energia/infraestructura/controlador/ReportController.java`

Agregar al controlador existente:

```java
@GetMapping("/volume")
public ResponseEntity<?> getEnergyVolume(
        @RequestParam(defaultValue = "month") String groupBy) {

    List<Object[]> rows = transactionRepository.findVolumeByPeriodAndType(groupBy);

    // Agrupar por período para combinar DIRECT y AUCTION en el mismo punto
    Map<String, Map<String, Object>> byPeriod = new java.util.LinkedHashMap<>();

    for (Object[] row : rows) {
        String period   = (String) row[0];
        String saleType = (String) row[1];
        double kwh      = ((Number) row[2]).doubleValue();
        long   count    = ((Number) row[3]).longValue();
        double avgKwh   = ((Number) row[4]).doubleValue();
        double value    = ((Number) row[5]).doubleValue();

        byPeriod.computeIfAbsent(period, k -> new java.util.LinkedHashMap<>())
            .put(saleType + "_kwh",   Math.round(kwh   * 100.0) / 100.0);
        byPeriod.get(period)
            .put(saleType + "_count", count);
        byPeriod.get(period)
            .put(saleType + "_value", Math.round(value * 100.0) / 100.0);
        byPeriod.get(period)
            .putIfAbsent("period", period);
    }

    List<Map<String, Object>> breakdown = new java.util.ArrayList<>(byPeriod.values());

    // KPIs globales
    double totalKwh = rows.stream()
        .mapToDouble(r -> ((Number) r[2]).doubleValue()).sum();
    long totalCount = rows.stream()
        .mapToLong(r -> ((Number) r[3]).longValue()).sum();
    double avgPerTx = totalCount > 0 ? totalKwh / totalCount : 0;
    double totalValue = rows.stream()
        .mapToDouble(r -> ((Number) r[5]).doubleValue()).sum();

    double directKwh = rows.stream()
        .filter(r -> "DIRECT".equals(r[1]))
        .mapToDouble(r -> ((Number) r[2]).doubleValue()).sum();
    double auctionKwh = rows.stream()
        .filter(r -> "AUCTION".equals(r[1]))
        .mapToDouble(r -> ((Number) r[2]).doubleValue()).sum();

    return ResponseEntity.ok(Map.of(
        "groupBy",  groupBy,
        "kpis", Map.of(
            "totalKwh",       Math.round(totalKwh   * 100.0) / 100.0,
            "totalValue",     Math.round(totalValue  * 100.0) / 100.0,
            "totalTx",        totalCount,
            "avgKwhPerTx",    Math.round(avgPerTx    * 100.0) / 100.0,
            "directKwh",      Math.round(directKwh   * 100.0) / 100.0,
            "auctionKwh",     Math.round(auctionKwh  * 100.0) / 100.0,
            "directShare",    totalKwh > 0
                ? Math.round(directKwh / totalKwh * 100 * 10.0) / 10.0 : 0
        ),
        "breakdown", breakdown
    ));
}
```

---

### Endpoint final expuesto

```
GET /api/reports/volume?groupBy=month
GET /api/reports/volume?groupBy=week
GET /api/reports/volume?groupBy=day
```

### Respuesta esperada
```json
{
  "groupBy": "month",
  "kpis": {
    "totalKwh":    184320.50,
    "totalValue":  2847193.80,
    "totalTx":     317,
    "avgKwhPerTx": 581.45,
    "directKwh":   128024.30,
    "auctionKwh":   56296.20,
    "directShare":  69.5
  },
  "breakdown": [
    {
      "period":          "2026-05",
      "DIRECT_kwh":      98430.20,
      "DIRECT_count":    210,
      "DIRECT_value":    1820450.00,
      "AUCTION_kwh":     38940.10,
      "AUCTION_count":   107,
      "AUCTION_value":   1026743.80
    }
  ]
}
```

---

### Archivos modificados o creados
| Archivo | Tipo |
|---------|------|
| TransactionJpaRepository.java | Modificado |
| ReportController.java | Modificado |

**No requiere migraciones. No requiere cambios al modelo de dominio.**

---

## FASE 2 — FRONTEND (implementar después de tener el backend listo)

### Vista: `/reports/volume`

**Componentes necesarios:**
- 3 botones toggle: Mes / Semana / Día → cambian el `groupBy` y refetch
- 4 KPI cards:
  - Total kWh comercializados
  - Valor total en USD
  - Número de transacciones
  - Promedio kWh por transacción
- BarChart apilado (recharts BarChart con stacked):
  - Eje X: período (mes/semana/día)
  - Barra 1 (verde): kWh de ventas DIRECT
  - Barra 2 (ámbar): kWh de subastas AUCTION
  - Permite ver la proporción y el volumen total en cada período
- PieChart pequeño: participación DIRECT vs AUCTION en kWh totales
- Tabla detalle por período:
  Período | kWh Directas | kWh Subastas | Total kWh | Transacciones | Valor USD

**API call:**
```typescript
getEnergyVolume(groupBy: 'day' | 'week' | 'month'): Promise<EnergyVolumeReport>
```
`GET /api/reports/volume?groupBy={groupBy}`
