package com.github.lukaszbudnik.iot.server.service;

public class ProcessingException extends DeviceAwareException {
    public ProcessingException(String message, String deviceId) {
        super(message, deviceId);
    }
}