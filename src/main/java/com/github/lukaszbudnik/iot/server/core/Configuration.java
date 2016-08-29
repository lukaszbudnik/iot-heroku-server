package com.github.lukaszbudnik.iot.server.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

public class Configuration extends io.dropwizard.Configuration {

    @JsonProperty(value = "iot.env")
    @NotEmpty
    private String env;

    @JsonProperty(value = "iot.properties")
    @NotEmpty
    private List<String> properties;

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public List<String> getProperties() {
        return properties;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }


}
