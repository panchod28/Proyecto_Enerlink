package com.enerlink.enerlink.configuracion;

/**
 * PATRÓN SINGLETON — Implementación usando Enum (Joshua Bloch)
 *
 * Esta es la forma recomendada en Effective Java.
 *
 * Ventajas:
 * - Thread-safe automáticamente
 * - Protección contra serialización
 * - Protección contra reflexión
 * - Implementación simple y robusta
 *
 * No depende de Spring.
 */

public enum PredictionEngine {

    INSTANCE; // Única instancia garantizada por la JVM

    private static final double SEASONAL_FACTOR = 1.1;

    /**
     * Método de negocio: predice el consumo futuro (backward compatibility).
     *
     * @param consumoActual consumo actual en kWh
     * @return predicción de consumo futuro
     */
    public double predict(double consumoActual) {
        return predict(new double[]{consumoActual});
    }

    /**
     * Nuevo método: predice usando media móvil ponderada.
     *
     * @param historicalValues valores históricos (más recientes al final)
     * @return predicción basada en media móvil ponderada y factor estacional
     * @throws IllegalArgumentException si historicalValues es nulo o vacío
     */
    public double predict(double[] historicalValues) {
        if (historicalValues == null || historicalValues.length == 0) {
            throw new IllegalArgumentException("historicalValues cannot be null or empty");
        }

        int n = historicalValues.length;
        double weightSum = 0;
        double weightedValueSum = 0;

        // Pesos lineales: más recientes (índices mayores) tienen mayor peso
        for (int i = 0; i < n; i++) {
            int weight = i + 1;
            weightedValueSum += historicalValues[i] * weight;
            weightSum += weight;
        }

        double wma = weightedValueSum / weightSum;
        double prediction = wma;

        // Factor estacional: ajuste por datos insuficientes
        if (n < 3) {
            prediction *= SEASONAL_FACTOR;
        }

        return prediction;
    }
}