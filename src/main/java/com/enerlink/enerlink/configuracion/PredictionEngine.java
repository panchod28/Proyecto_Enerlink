package com.enerlink.enerlink.configuracion;

import java.util.concurrent.ThreadLocalRandom;

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

    /**
     * Método de negocio: predice el consumo futuro.
     *
     * @param consumoActual consumo actual en kWh
     * @return predicción de consumo futuro
     */
    public double predict(double consumoActual) {

        double factorBase = 1.15;

        // Variación aleatoria entre -5% y +5%
        double variacion = ThreadLocalRandom.current().nextDouble(-0.05, 0.05);

        double factorFinal = factorBase + variacion;

        return consumoActual * factorFinal;
    }
}