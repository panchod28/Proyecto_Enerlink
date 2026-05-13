# Reporte 6 — Actividad de Compradores

## Propósito del reporte

Este reporte responde una pregunta que los 5 reportes anteriores no cubren: **¿quiénes son los compradores más activos de Enerlink y cómo se comportan?**

El Reporte 5 (Eficiencia de Productores) genera un ranking del lado de la oferta. Este reporte es su espejo natural — genera un ranking del lado de la demanda. Juntos, completan la inteligencia de mercado de la plataforma: el administrador sabe quiénes producen mejor Y quiénes compran más.

---

## Pregunta de negocio que responde

- ¿Cuánto ha gastado cada comprador en la plataforma?
- ¿Cuántos kWh ha adquirido en total?
- ¿Prefiere venta directa o subasta?
- ¿Es un comprador frecuente, ocasional o prácticamente inactivo?
- ¿Cuánto gasta en promedio por transacción?

---

## Clasificación de compradores

Basada en el número de transacciones completadas como comprador:

| Clasificación     | Criterio                        |
|-------------------|---------------------------------|
| FRECUENTE         | 5 o más transacciones           |
| OCASIONAL         | 2 a 4 transacciones             |
| INACTIVO          | 1 transacción                   |

> Nota: solo aparecen en el reporte usuarios que tienen al menos 1 transacción como comprador (buyer_id en la tabla transactions).

---

## Fuente de datos

**Tabla principal:** `transactions`
- `buyer_id` → identifica al comprador
- `kwh` → energía adquirida por transacción
- `price` → precio pagado por kWh
- `kwh * price` → valor total de la transacción

**Join necesario:**
- `transactions` JOIN `users` ON `transactions.buyer_id = users.id` → para obtener el nombre del comprador y su rol
- `transactions` JOIN `energy_offer` ON `transactions.offer_id = energy_offer.id` → para obtener el `sale_type` (DIRECT o AUCTION)

**No se necesita** cruzar con `iot_device` ni con datos de generación. Es la query más simple de todos los reportes.

---

## Query SQL (nativa PostgreSQL)

```sql
SELECT
    u.nombre                                        AS buyerName,
    u.rol                                           AS role,
    COUNT(t.id)                                     AS totalTransactions,
    SUM(t.kwh)                                      AS totalKwhBought,
    SUM(t.kwh * t.price)                            AS totalSpent,
    AVG(t.kwh * t.price)                            AS avgSpentPerTx,
    COUNT(t.id) FILTER (WHERE eo.sale_type = 'DIRECT')  AS directCount,
    COUNT(t.id) FILTER (WHERE eo.sale_type = 'AUCTION') AS auctionCount,
    AVG(t.price)                                    AS avgPricePerKwh
FROM transactions t
JOIN users u ON u.id = t.buyer_id
JOIN energy_offer eo ON eo.id = t.offer_id
GROUP BY u.id, u.nombre, u.rol
ORDER BY SUM(t.kwh * t.price) DESC
```

---

## Lógica de negocio en ReportService

### Clasificación
```java
String classification = totalTransactions >= 5 ? "FRECUENTE"
    : totalTransactions >= 2 ? "OCASIONAL" : "INACTIVO";
```

### Preferencia de tipo de venta
```java
String preference = directCount >= auctionCount ? "DIRECT" : "AUCTION";
```

### Score de actividad (0–100)
Combina gasto total normalizado y frecuencia de compra:
```java
// Normalizar sobre el máximo comprador del conjunto
// Se calcula en el servicio después de mapear todos los compradores
// score = (totalSpent / maxSpent * 60) + (totalTransactions / maxTx * 40)
// Redondear a 1 decimal
```

> El score se calcula en dos pasadas: primero se mapean todos los compradores, luego se normalizan los scores sobre el máximo del conjunto. Mismo patrón usado en Reporte 5.

---

## Estructura de la respuesta del endpoint

```
GET /api/reports/buyer-activity?page=0&size=20
```

```json
{
  "totalElements": 45,
  "totalPages": 3,
  "number": 0,
  "size": 20,
  "last": false,
  "summary": {
    "totalBuyers": 45,
    "frequent": 8,
    "occasional": 15,
    "inactive": 22
  },
  "buyers": [
    {
      "buyerName": "Ana Gómez López",
      "role": "CONSUMER",
      "totalTransactions": 7,
      "totalKwhBought": 4520.50,
      "totalSpent": 875430.00,
      "avgSpentPerTx": 125061.43,
      "avgPricePerKwh": 193.67,
      "directCount": 5,
      "auctionCount": 2,
      "preference": "DIRECT",
      "score": 95.4,
      "classification": "FRECUENTE"
    }
  ]
}
```

---

## Paginación

Misma estrategia que Reporte 2 y Reporte 5:
- Se calculan todos los compradores en memoria
- Se computa el summary y los scores sobre la lista completa
- Se aplica subList(fromIndex, toIndex) al final
- Los parámetros `page` y `size` llegan como `@RequestParam` con defaults 0 y 20

---

## Método a agregar en TransactionJpaRepository

Nombre del método: `findBuyerActivityData`

```java
@Query(value = """
    SELECT
        u.nombre                                            AS buyerName,
        u.rol                                              AS role,
        COUNT(t.id)                                        AS totalTransactions,
        SUM(t.kwh)                                         AS totalKwhBought,
        SUM(t.kwh * t.price)                               AS totalSpent,
        AVG(t.kwh * t.price)                               AS avgSpentPerTx,
        COUNT(t.id) FILTER (WHERE eo.sale_type = 'DIRECT') AS directCount,
        COUNT(t.id) FILTER (WHERE eo.sale_type = 'AUCTION') AS auctionCount,
        AVG(t.price)                                       AS avgPricePerKwh
    FROM transactions t
    JOIN users u ON u.id = t.buyer_id
    JOIN energy_offer eo ON eo.id = t.offer_id
    GROUP BY u.id, u.nombre, u.rol
    ORDER BY SUM(t.kwh * t.price) DESC
    """, nativeQuery = true)
List<Object[]> findBuyerActivityData();
```

---

## Mapeo de columnas Object[]

| Índice | Campo           | Tipo     | Cast Java                           |
|--------|-----------------|----------|-------------------------------------|
| row[0] | buyerName       | String   | (String) row[0]                     |
| row[1] | role            | String   | (String) row[1]                     |
| row[2] | totalTransactions | long   | ((Number) row[2]).longValue()       |
| row[3] | totalKwhBought  | double   | ((Number) row[3]).doubleValue()     |
| row[4] | totalSpent      | double   | ((Number) row[4]).doubleValue()     |
| row[5] | avgSpentPerTx   | double   | ((Number) row[5]).doubleValue()     |
| row[6] | directCount     | long     | ((Number) row[6]).longValue()       |
| row[7] | auctionCount    | long     | ((Number) row[7]).longValue()       |
| row[8] | avgPricePerKwh  | double   | ((Number) row[8]).doubleValue()     |

---

## Método a agregar en ReportService

Nombre del método: `getBuyerActivity(int page, int size)`

Lógica paso a paso:
1. Llamar a `transactionRepository.findBuyerActivityData()`
2. Mapear cada `Object[]` a un `Map<String, Object>` con todos los campos
3. Calcular `classification` y `preference` por comprador
4. Encontrar `maxSpent` y `maxTx` del conjunto completo para normalizar el score
5. Calcular `score` = (totalSpent / maxSpent * 60) + (totalTransactions / maxTx * 40), redondeado a 1 decimal
6. Ordenar por `score` descendente (ya viene ordenado por totalSpent pero el score puede reordenar)
7. Calcular summary counts (frequent, occasional, inactive) sobre la lista completa
8. Aplicar paginación subList
9. Retornar Map con totalElements, totalPages, number, size, last, summary, buyers

---

## Endpoint a agregar en ReportController

```java
@GetMapping("/buyer-activity")
public ResponseEntity<?> getBuyerActivity(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(reportService.getBuyerActivity(page, size));
}
```

---

## Archivos a modificar

| Archivo | Acción |
|---------|--------|
| `TransactionJpaRepository.java` | Agregar método `findBuyerActivityData()` |
| `ReportService.java` | Agregar método `getBuyerActivity(int page, int size)` |
| `ReportController.java` | Agregar endpoint `GET /api/reports/buyer-activity` |

**No se modifica ningún otro archivo.**
**No se necesita migración Flyway** — no hay cambios en el esquema de base de datos.
**No se necesita modificar ningún modelo de dominio.**

---

## Verificación esperada del endpoint

```
GET http://localhost:8080/api/reports/buyer-activity?page=0&size=20
```

Respuesta esperada con los datos actuales:
- `totalBuyers` entre 40 y 55 (usuarios con al menos 1 compra)
- `frequent` entre 5 y 10 (compradores con 5+ transacciones)
- El primer comprador debe tener el mayor `totalSpent`
- `score` del primer comprador debe ser 100.0 (normalizado sobre el máximo)

---

## Relación con los otros reportes

| Reporte | Perspectiva |
|---------|------------|
| Reporte 1 — Comisiones | Plataforma (ingresos) |
| Reporte 2 — Volumen | Mercado (energía) |
| Reporte 3 — Análisis de Mercado | Ofertas (conversión) |
| Reporte 4 — Perfil Energético | Usuario (balance) |
| Reporte 5 — Eficiencia Productores | Productor (oferta) |
| **Reporte 6 — Actividad Compradores** | **Comprador (demanda)** |

El Reporte 6 completa la simetría del módulo: Reporte 5 cubre el lado de la oferta, Reporte 6 cubre el lado de la demanda.
