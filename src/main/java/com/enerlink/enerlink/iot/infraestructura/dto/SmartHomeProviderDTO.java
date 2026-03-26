package com.enerlink.enerlink.iot.infraestructura.dto;

public class SmartHomeProviderDTO {

    private String device_id;
    private String device_name;
    private String device_category;
    private MetricValue metrics;
    private LocationInfo location;
    private String operational_state;
    private long last_updated;

    public static class MetricValue {
        private String measurement_type;
        private double value;
        private String unit_of_measure;
        private double quality_score;

        public String getMeasurementType() { return measurement_type; }
        public double getValue() { return value; }
        public String getUnitOfMeasure() { return unit_of_measure; }
        public double getQualityScore() { return quality_score; }

        public void setMeasurementType(String measurement_type) { this.measurement_type = measurement_type; }
        public void setValue(double value) { this.value = value; }
        public void setUnitOfMeasure(String unit_of_measure) { this.unit_of_measure = unit_of_measure; }
        public void setQualityScore(double quality_score) { this.quality_score = quality_score; }
    }

    public static class LocationInfo {
        private String site_id;
        private String zone;
        private String address;

        public String getSiteId() { return site_id; }
        public String getZone() { return zone; }
        public String getAddress() { return address; }

        public void setSiteId(String site_id) { this.site_id = site_id; }
        public void setZone(String zone) { this.zone = zone; }
        public void setAddress(String address) { this.address = address; }
    }

    public String getDeviceId() { return device_id; }
    public String getDeviceName() { return device_name; }
    public String getDeviceCategory() { return device_category; }
    public MetricValue getMetrics() { return metrics; }
    public LocationInfo getLocation() { return location; }
    public String getOperationalState() { return operational_state; }
    public long getLastUpdated() { return last_updated; }

    public void setDeviceId(String device_id) { this.device_id = device_id; }
    public void setDeviceName(String device_name) { this.device_name = device_name; }
    public void setDeviceCategory(String device_category) { this.device_category = device_category; }
    public void setMetrics(MetricValue metrics) { this.metrics = metrics; }
    public void setLocation(LocationInfo location) { this.location = location; }
    public void setOperationalState(String operational_state) { this.operational_state = operational_state; }
    public void setLastUpdated(long last_updated) { this.last_updated = last_updated; }
}