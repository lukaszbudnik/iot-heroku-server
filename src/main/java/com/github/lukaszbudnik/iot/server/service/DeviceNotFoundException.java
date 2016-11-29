package com.github.lukaszbudnik.iot.server.service;

public class DeviceNotFoundException extends DeviceAwareException {
    public DeviceNotFoundException(String message, String deviceId) {
        super(message, deviceId);
    }
}
