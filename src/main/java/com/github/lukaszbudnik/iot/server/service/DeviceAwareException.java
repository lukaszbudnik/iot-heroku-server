package com.github.lukaszbudnik.iot.server.service;

import org.json.JSONObject;

public class DeviceAwareException extends Exception {
    private final String deviceId;

    public DeviceAwareException(String message, String deviceId) {
        super(message);
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String jsonError() {
        JSONObject json = new JSONObject();
        json.put("error", getMessage());
        json.put("deviceId", deviceId);
        return json.toString();
    }
}
