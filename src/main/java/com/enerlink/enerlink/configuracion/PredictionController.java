package com.enerlink.enerlink.configuracion;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    // --- Simple prediction endpoint ---

    @PostMapping("/simple")
    public SimpleResponse simplePrediction(@RequestBody SimpleRequest request) {
        double prediction = PredictionEngine.INSTANCE.predict(request.currentValue);
        return new SimpleResponse(prediction);
    }

    public static class SimpleRequest {
        public double currentValue;
    }

    public static class SimpleResponse {
        public double prediction;

        public SimpleResponse(double prediction) {
            this.prediction = prediction;
        }
    }

    // --- Historical prediction endpoint ---

    @PostMapping("/historical")
    public HistoricalResponse historicalPrediction(@RequestBody HistoricalRequest request) {
        double prediction = PredictionEngine.INSTANCE.predict(request.historicalValues);
        return new HistoricalResponse(prediction, request.historicalValues.length);
    }

    public static class HistoricalRequest {
        public double[] historicalValues;
    }

    public static class HistoricalResponse {
        public double prediction;
        public int dataPoints;

        public HistoricalResponse(double prediction, int dataPoints) {
            this.prediction = prediction;
            this.dataPoints = dataPoints;
        }
    }
}
