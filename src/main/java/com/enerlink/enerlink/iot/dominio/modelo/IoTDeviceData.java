package com.enerlink.enerlink.iot.dominio.modelo;

public class IoTDeviceData {

    private Long id;
    private String deviceId;
    private String deviceName;
    private DeviceType deviceType;
    private double currentReading;
    private String unit;
    private String location;
    private String status;
    private long timestamp;
    private Long userId;

    public IoTDeviceData() {
    }

    public IoTDeviceData(String deviceId, String deviceName, DeviceType deviceType,
                          double currentReading, String unit, String location,
                          String status, long timestamp) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.currentReading = currentReading;
        this.unit = unit;
        this.location = location;
        this.status = status;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public DeviceType getDeviceType() { return deviceType; }
    public void setDeviceType(DeviceType deviceType) { this.deviceType = deviceType; }

    public double getCurrentReading() { return currentReading; }
    public void setCurrentReading(double currentReading) { this.currentReading = currentReading; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    @Override
    public String toString() {
        return "IoTDeviceData{id=" + id + ", deviceId='" + deviceId + "', deviceName='" + deviceName +
                "', deviceType=" + deviceType + ", currentReading=" + currentReading +
                ", unit='" + unit + "', location='" + location + "', status='" + status +
                "', timestamp=" + timestamp + ", userId=" + userId + "}";
    }
}