# Reporte 3 — Análisis del Mercado de Ofertas

## Contexto de negocio
¿Qué tan saludable es el mercado de Enerlink? Este reporte construye
indicadores de salud del mercado que no existen en ninguna tabla:
tasa de conversión global, evolución del precio promedio por kWh,
distribución de tipos de venta y actividad del mercado por período.
Combina energy_offer y transactions para revelar tendencias invisibles.

---

## FASE 1 — BACKEND

### Archivos a leer antes de implementar
```
src/main/java/com/enerlink/enerlink/energia/infraestructura/persistencia/EnergyOfferJpaRepository.java
src/main/java/com/enerlink/enerlink/energia/infraestructura/persistencia/TransactionJpaRepository.java
src/main/java/com/enerlink/enerlink/energia/infraestructura/controlador/ReportController.java
src/main/resources/db/migration/ (ver último V disponible)
```

**Nota:** Este reporte requiere el campo `createdAt` en `energy_offer` del Reporte 2.
Si el Reporte 2 ya fue implementado, este campo ya existe. Si no, aplicar los
Pasos 1-5 del Reporte 2 antes de continuar.

---

### Paso 1 — Query de resumen de mercado en EnergyOfferJpaRepository

**Archivo:** `src/main/java/com/enerlink/enerlink/energia/infraestructura/persistencia/EnergyOfferJpaRepository.java`

Agregar:

```java
// Distribución por tipo de oferta
@Query(value = """
    SELECT
        sale_type,
        COUNT(*)                              AS total,
        COUNT(*) FILTER (WHERE available = false) AS sold,
        COUNT(*) FILTER (WHERE available = true)  AS active,
        AVG(price)                            AS avgPrice,
        AVG(kwh)                              AS avgKwh
    FROM energy_offer
    GROUP BY sale_type
    """, nativeQuery = true)
List<Object[]> findMarketDistribution();

// Evolución del precio promedio por semana
@Query(value = """
    SELECT
        TO_CHAR(DATE_TRUNC('week', t.timestamp), 'YYYY-MM-DD') AS week,
        eo.sale_type,
        AVG(t.price)                                            AS avgPrice,
        COUNT(t.id)                                             AS transactions,
        SUM(t.kwh)                                              AS totalKwh
    FROM transactions t
    JOIN energy_offer eo ON eo.id = t.offer_id
    GROUP BY 1, 2
    ORDER BY 1 ASC
    """, nativeQuery = true)
List<Object[]> findWeeklyPriceTrend();
```

---

### Paso 2 — Endpoint en ReportController

**Archivo:** `src/main/java/com/enerlink/enerlink/energia/infraestructura/controlador/ReportController.java`

Agregar al controlador existente:

```java
@GetMapping("/market-summary")
public ResponseEntity<?> getMarketSummary() {

    // Distribución por tipo
    List<Object[]> distribution = offerJpaRepository.findMarketDistribution();
    List<Map<String, Object>> typeBreakdown = distribution.stream().map(row -> {
        long total = ((Number) row[1]).longValue();
        long sold  = ((Number) row[2]).longValue();
        double conversionRate = total > 0 ? (double) sold / total * 100 : 0;

        return Map.of(
            "saleType",       row[0],
            "total",          total,
            "sold",           sold,
            "active",         row[3],
            "conversionRate", Math.round(conversionRate * 100.0) / 100.0,
            "avgPrice",       Math.round(((Number) row[4]).doubleValue() * 100.0) / 100.0,
            "avgKwh",         Math.round(((Number) row[5]).doubleValue() * 100.0) / 100.0
        );
    }).collect(Collectors.toList());

    // KPIs globales
    long totalOffers = typeBreakdown.stream()
        .mapToLong(r -> ((Number) r.get("total")).longValue()).sum();
    long totalSold = typeBreakdown.stream()
        .mapToLong(r -> ((Number) r.get("sold")).longValue()).sum();
    double globalConversion = totalOffers > 0
        ? (double) totalSold / totalOffers * 100 : 0;

    // Tendencia semanal de precio
    List<Object[]> trendRows = offerJpaRepository.findWeeklyPriceTrend();
    List<Map<String, Object>> weeklyTrend = trendRows.stream().map(row -> Map.of(
        "week",         row[0],
        "saleType",     row[1],
        "avgPrice",     Math.round(((Number) row[2]).doubleValue() * 100.0) / 100.0,
        "transactions", row[3],
        "totalKwh",     Math.round(((Number) row[4]).doubleValue() * 100.0) / 100.0
    )).collect(Collectors.toList());

    return ResponseEntity.ok(Map.of(
        "kpis", Map.of(
            "totalOffers",       totalOffers,
            "totalSold",         totalSold,
            "totalActive",       totalOffers - totalSold,
            "globalConversion",  Math.round(globalConversion * 100.0) / 100.0
        ),
        "typeBreakdown",  typeBreakdown,
        "weeklyTrend",    weeklyTrend
    ));
}
```

Agregar inyección de `EnergyOfferJpaRepository` al constructor del ReportController:

```java
private final EnergyOfferJpaRepository offerJpaRepository;

public ReportController(TransactionJpaRepository transactionRepository,
                        EnergyOfferJpaRepository offerJpaRepository) {
    this.transactionRepository = transactionRepository;
    this.offerJpaRepository    = offerJpaRepository;
}
```

---

### Endpoint final expuesto

```
GET /api/reports/market-summary
```

### Respuesta esperada
```json
{
  "kpis": {
    "totalOffers":      1257,
    "totalSold":         317,
    "totalActive":       940,
    "globalConversion":  25.22
  },
  "typeBreakdown": [
    {
      "saleType":       "DIRECT",
      "total":           847,
      "sold":            210,
      "active":          637,
      "conversionRate":  24.79,
      "avgPrice":        145.30,
      "avgKwh":          620.50
    },
    {
      "saleType":       "AUCTION",
      "total":           410,
      "sold":            107,
      "active":          303,
      "conversionRate":  26.10,
      "avgPrice":        189.40,
      "avgKwh":          890.20
    }
  ],
  "weeklyTrend": [
    {
      "week":         "2026-04-28",
      "saleType":     "DIRECT",
      "avgPrice":     138.20,
      "transactions": 15,
      "totalKwh":     9450.00
    }
  ]
}
```

---

### Archivos modificados o creados
| Archivo | Tipo |
|---------|------|
| EnergyOfferJpaRepository.java | Modificado |
| ReportController.java | Modificado |

**Nota:** No requiere migraciones ni cambios al modelo de dominio si el campo
`createdAt` del Reporte 2 ya fue aplicado.

---

## FASE 2 — FRONTEND (implementar después de tener el backend listo)

### Vista: `/reports/market`

**Componentes necesarios:**
- 4 KPI cards: Total ofertas / Vendidas / Activas / Tasa de conversión global %
- PieChart / DonutChart (recharts PieChart con innerRadius):
  - Muestra distribución DIRECT vs AUCTION del total de ofertas
- BarChart agrupado (recharts BarChart):
  - Eje X: tipo de venta (DIRECT, AUCTION)
  - Dos barras por tipo: Total vs Vendidas
  - Muestra visualmente la tasa de conversión
- LineChart (recharts LineChart):
  - Eje X: semana
  - Dos líneas: precio promedio DIRECT y precio promedio AUCTION
  - Permite ver si los precios suben o bajan con el tiempo
- Tabla resumen por tipo: Tipo | Total | Vendidas | Activas | Conversión % | Precio promedio | kWh promedio

**API call:**
```typescript
getMarketSummary(): Promise<MarketSummaryReport>
```
`GET /api/reports/market-summary`
