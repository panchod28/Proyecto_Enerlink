# Reporte 1 — Balance de Comisiones de la Plataforma

## Contexto de negocio
El dueño de Enerlink cobra una comisión sobre cada transacción ejecutada.
Este reporte responde: ¿cuánto dinero generó la plataforma en comisiones?
Permite drill-down de mes → semana → día.

---

## FASE 1 — BACKEND

### Archivos a leer antes de implementar
```
src/main/java/com/enerlink/enerlink/energia/dominio/modelo/Transaction.java
src/main/java/com/enerlink/enerlink/energia/infraestructura/persistencia/TransactionEntity.java
src/main/java/com/enerlink/enerlink/energia/infraestructura/persistencia/TransactionJpaRepository.java
src/main/java/com/enerlink/enerlink/energia/infraestructura/persistencia/TransactionRepositoryAdapter.java
src/main/java/com/enerlink/enerlink/energia/dominio/decorador/FeeTransactionDecorator.java
src/main/java/com/enerlink/enerlink/energia/aplicacion/servicio/EnergyTradingFacade.java
src/main/resources/db/migration/V1__initial_schema.sql
```

---

### Paso 1 — Modificar FeeTransactionDecorator

Actualmente la fee está en 0.0. Cambiar a 2% del valor total de la transacción.

**Archivo:** `src/main/java/com/enerlink/enerlink/energia/dominio/decorador/FeeTransactionDecorator.java`

El campo `feeRate` debe ser `0.02` (2%).
El método `getPrice()` debe retornar `wrapped.getPrice() * (1 + feeRate)`.
Agregar método `getCommission()` que retorne `wrapped.getPrice() * wrapped.getKwh() * feeRate`.

---

### Paso 2 — Agregar campo `commission` a Transaction

**Archivo:** `src/main/java/com/enerlink/enerlink/energia/dominio/modelo/Transaction.java`

Agregar campo:
```java
private double commission;
```
Con getter, setter y builder correspondiente.

---

### Paso 3 — Agregar campo `commission` a TransactionEntity

**Archivo:** `src/main/java/com/enerlink/enerlink/energia/infraestructura/persistencia/TransactionEntity.java`

Agregar:
```java
@Column(name = "commission")
private double commission;
```
Con getter y setter.

---

### Paso 4 — Migración Flyway

**Archivo a crear:** `src/main/resources/db/migration/V3__add_commission_to_transactions.sql`

```sql
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS commission DOUBLE PRECISION DEFAULT 0.0;

-- Actualizar transacciones existentes con comisión estimada del 2%
UPDATE transactions SET commission = price * kwh * 0.02 WHERE commission = 0.0;
```

---

### Paso 5 — Actualizar TransactionRepositoryAdapter

**Archivo:** `src/main/java/com/enerlink/enerlink/energia/infraestructura/persistencia/TransactionRepositoryAdapter.java`

En el método `save()`: agregar `entity.setCommission(transaction.getCommission())`.
En el método `mapToDomain()`: agregar `data.setCommission(entity.getCommission())`.

---

### Paso 6 — Actualizar EnergyTradingFacade

**Archivo:** `src/main/java/com/enerlink/enerlink/energia/aplicacion/servicio/EnergyTradingFacade.java`

En `applyDecoratorChain()`, al construir el Transaction final con builder, calcular y asignar la comisión:
```java
double commission = transaction.getKwh() * transaction.getPrice() * 0.02;
return Transaction.builder()
    ...
    .commission(commission)
    .build();
```

---

### Paso 7 — Crear ReportController

**Archivo a crear:** `src/main/java/com/enerlink/enerlink/energia/infraestructura/controlador/ReportController.java`

```java
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final TransactionJpaRepository transactionRepository;

    public ReportController(TransactionJpaRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/commissions")
    public ResponseEntity<?> getCommissions(
            @RequestParam(defaultValue = "month") String groupBy) {

        List<Object[]> rows = transactionRepository.findCommissionsByPeriod(groupBy);
        // Mapear a lista de { period, totalCommission, transactionCount, avgCommission }
        List<Map<String, Object>> result = rows.stream().map(row -> Map.of(
            "period",           row[0],
            "totalCommission",  row[1],
            "transactionCount", row[2],
            "avgCommission",    row[3]
        )).collect(Collectors.toList());

        double grandTotal = result.stream()
            .mapToDouble(r -> ((Number) r.get("totalCommission")).doubleValue())
            .sum();

        return ResponseEntity.ok(Map.of(
            "groupBy",    groupBy,
            "grandTotal", grandTotal,
            "breakdown",  result
        ));
    }
}
```

---

### Paso 8 — Query en TransactionJpaRepository

**Archivo:** `src/main/java/com/enerlink/enerlink/energia/infraestructura/persistencia/TransactionJpaRepository.java`

Agregar:

```java
@Query(value = """
    SELECT
        CASE
            WHEN :groupBy = 'day'   THEN TO_CHAR(timestamp, 'YYYY-MM-DD')
            WHEN :groupBy = 'week'  THEN TO_CHAR(DATE_TRUNC('week', timestamp), 'YYYY-MM-DD')
            WHEN :groupBy = 'month' THEN TO_CHAR(timestamp, 'YYYY-MM')
        END AS period,
        SUM(commission)           AS totalCommission,
        COUNT(*)                  AS transactionCount,
        AVG(commission)           AS avgCommission
    FROM transactions
    WHERE commission > 0
    GROUP BY 1
    ORDER BY 1 DESC
    """, nativeQuery = true)
List<Object[]> findCommissionsByPeriod(@Param("groupBy") String groupBy);
```

---

### Endpoint final expuesto

```
GET /api/reports/commissions?groupBy=month
GET /api/reports/commissions?groupBy=week
GET /api/reports/commissions?groupBy=day
```

### Respuesta esperada
```json
{
  "groupBy": "month",
  "grandTotal": 1247.83,
  "breakdown": [
    { "period": "2026-05", "totalCommission": 847.20, "transactionCount": 42, "avgCommission": 20.17 },
    { "period": "2026-04", "totalCommission": 400.63, "transactionCount": 21, "avgCommission": 19.08 }
  ]
}
```

---

### Archivos modificados o creados
| Archivo | Tipo |
|---------|------|
| FeeTransactionDecorator.java | Modificado |
| Transaction.java | Modificado |
| TransactionEntity.java | Modificado |
| TransactionRepositoryAdapter.java | Modificado |
| EnergyTradingFacade.java | Modificado |
| V3__add_commission_to_transactions.sql | Creado |
| ReportController.java | Creado |
| TransactionJpaRepository.java | Modificado |

---

## FASE 2 — FRONTEND (implementar después de tener el backend listo)

### Vista: `/reports/commissions`

**Componentes necesarios:**
- 3 botones toggle: Mes / Semana / Día → cambian el `groupBy`
- KPI card grande: "Total comisiones" con el `grandTotal`
- KPI card secundaria: "Transacciones procesadas" (suma de `transactionCount`)
- LineChart (recharts) con eje X = `period`, eje Y = `totalCommission`
- Tabla detallada: columnas Período | Comisiones | Transacciones | Promedio por transacción

**Flujo:**
1. Al montar la vista: `GET /api/reports/commissions?groupBy=month`
2. Al cambiar toggle: refetch con nuevo `groupBy`
3. Al hacer clic en una fila de la tabla (ej. un mes): refetch con groupBy un nivel más detallado

**API call en frontend:**
```typescript
getCommissions(groupBy: 'day' | 'week' | 'month'): Promise<CommissionsReport>
```
`GET /api/reports/commissions?groupBy={groupBy}`
