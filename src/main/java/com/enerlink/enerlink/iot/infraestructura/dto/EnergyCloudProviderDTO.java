package com.enerlink.enerlink.iot.infraestructura.dto;

public class EnergyCloudProviderDTO {

    private ResourceData resource;
    private MeasurementData measurement;
    private StatusInfo status;

    public static class ResourceData {
        private String id;
        private String name;
        private String type;
        private String manufacturer;
        private String model;

        public String getId() { return id; }
        public String getName() { return name; }
        public String getType() { return type; }
        public String getManufacturer() { return manufacturer; }
        public String getModel() { return model; }

        public void setId(String id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public void setType(String type) { this.type = type; }
        public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
        public void setModel(String model) { this.model = model; }
    }

    public static class MeasurementData {
        private double current_value;
        private String uom;
        private long timestamp;
        private AccuracyInfo accuracy;

        public double getCurrentValue() { return current_value; }
        public String getUom() { return uom; }
        public long getTimestamp() { return timestamp; }
        public AccuracyInfo getAccuracy() { return accuracy; }

        public void setCurrentValue(double current_value) { this.current_value = current_value; }
        public void setUom(String uom) { this.uom = uom; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public void setAccuracy(AccuracyInfo accuracy) { this.accuracy = accuracy; }
    }

    public static class AccuracyInfo {
        private double percentage;
        private String method;

        public double getPercentage() { return percentage; }
        public String getMethod() { return method; }

        public void setPercentage(double percentage) { this.percentage = percentage; }
        public void setMethod(String method) { this.method = method; }
    }

    public static class StatusInfo {
        private String code;
        private String description;
        private boolean active;

        public String getCode() { return code; }
        public String getDescription() { return description; }
        public boolean isActive() { return active; }

        public void setCode(String code) { this.code = code; }
        public void setDescription(String description) { this.description = description; }
        public void setActive(boolean active) { this.active = active; }
    }

    public ResourceData getResource() { return resource; }
    public MeasurementData getMeasurement() { return measurement; }
    public StatusInfo getStatus() { return status; }

    public void setResource(ResourceData resource) { this.resource = resource; }
    public void setMeasurement(MeasurementData measurement) { this.measurement = measurement; }
    public void setStatus(StatusInfo status) { this.status = status; }
}