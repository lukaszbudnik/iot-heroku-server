package com.github.lukaszbudnik.iot.server.internal;


import com.att.m2x.java.M2XClient;
import com.att.m2x.java.M2XDevice;
import com.att.m2x.java.M2XResponse;
import com.att.m2x.java.M2XStream;
import com.github.lukaszbudnik.iot.server.service.DeviceNotFoundException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import com.lambdaworks.redis.RedisURI;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Abstracts all m2x calls
 */
public class DeviceService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);

    private final M2XClient m2x;

    private final RedisClient redisClient;

    @Inject
    public DeviceService(@Named("m2x.apiKey") String m2xApiKey, @Named("redis") String redis) {
        m2x = new M2XClient(m2xApiKey);
        redisClient = new RedisClient(RedisURI.create(redis));
    }

    public String createDevice(String deviceId) throws IOException {
        M2XResponse response = m2x.createDevice(M2XClient.jsonSerialize(ImmutableMap.of(
                "name", "auto registered " + deviceId,
                "visibility", "private",
                "serial", deviceId
        )));

        logger.info(response.raw);

        String m2xDeviceId = response.json().getString("id");
        createDeviceMapping(deviceId, m2xDeviceId);

        return m2xDeviceId;
    }

    public JSONObject getDevice(String deviceId) throws IOException, DeviceNotFoundException {
        String m2xDeviceId = findDeviceMapping(deviceId);
        if (m2xDeviceId == null) {
            throw new DeviceNotFoundException("device not found", deviceId);
        }
        M2XDevice m2xdevice = m2x.device(m2xDeviceId);
        JSONObject json = m2xdevice.details().json();
        M2XResponse charts = m2x.makeRequest("GET", "/charts", "?device=" + deviceId, null);
        if (charts.success()) {
            json.put("charts", charts.json().get("charts"));
        }
        return json;
    }

    public void sendDeviceMetrics(String deviceId, String stream, List<String> values, List<String> timestamps) throws DeviceNotFoundException {
        String m2xDeviceId = findDeviceMapping(deviceId);
        if (m2xDeviceId == null) {
            throw new DeviceNotFoundException("device not found", deviceId);
        }
        executorService.submit(() -> {
            M2XDevice m2xdevice = m2x.device(deviceId);
            M2XStream m2xstream = m2xdevice.stream(stream);

            // build the payload
            List<Map<String, String>> payload = new ArrayList<>(values.size());
            for (int i = 0; i < values.size(); i++) {
                payload.add(ImmutableMap.of("value", values.get(i), "timestamp", timestamps.get(i)));
            }

            String json = M2XClient.jsonSerialize(ImmutableMap.of("values", payload));
            logger.info(String.format("[sendDeviceMetrics-request] [%s] [%s] [%s]", deviceId, stream, json));

            try {
                M2XResponse response = m2xstream.postValues(json);
                String jsonResponse = response.raw;
                logger.info(String.format("[sendDeviceMetrics-response] [%s] [%s] [%s]", deviceId, stream, jsonResponse));
            } catch (IOException e) {
                logger.error(String.format("[sendDeviceMetrics-error] [%s] [%s]", deviceId, stream), e);
            }

        });
    }

    public String createDeviceMapping(String deviceId, String m2xDeviceId) {
        RedisConnection connection = redisClient.connect();
        String response = connection.set(deviceId, m2xDeviceId);
        connection.close();
        return response;
    }

    public String findDeviceMapping(String deviceId) {
        RedisConnection<String, String> connection = redisClient.connect();
        String m2xDeviceId = connection.get(deviceId);
        connection.close();
        return m2xDeviceId;
    }

    public String createStreams(String deviceId, List<String> streams, List<String> units, List<String> types) throws DeviceNotFoundException {
        String m2xDeviceId = findDeviceMapping(deviceId);

        if (m2xDeviceId == null) {
            throw new DeviceNotFoundException("device not found", deviceId);
        }

        M2XDevice m2xdevice = m2x.device(m2xDeviceId);

        for (int i = 0; i < streams.size(); i++) {
            String stream = streams.get(i);
            String unit = units.get(i);
            String type = types.get(i);
            executorService.submit(() -> {
                M2XStream m2xstream = m2xdevice.stream(stream);
                try {
                    String json = M2XClient.jsonSerialize(ImmutableMap.of("display_name", stream, "type", type, "unit", ImmutableMap.of("label", unit)));
                    logger.info(String.format("[createStreams-streams-request] [%s] [%s] [%s]", deviceId, stream, json));

                    M2XResponse response = m2xstream.createOrUpdate(json.toString());
                    String jsonResponse = response.raw;
                    logger.info(String.format("[createStreams-streams-response] [%s] [%s] [%s]", deviceId, stream, jsonResponse));

                    JSONObject chartJson = new JSONObject();
                    chartJson.put("name", stream + " charts");
                    JSONObject series = new JSONObject();
                    series.put("device", m2xDeviceId);
                    series.put("stream", stream);
                    chartJson.put("series", ImmutableList.of(series));
                    json = chartJson.toString();

                    logger.info(String.format("[createStreams-charts-request] [%s] [%s] [%s]", deviceId, stream, json));
                    response = m2x.makeRequest("POST", "/charts", null, json);
                    jsonResponse = response.raw;
                    logger.info(String.format("[createStreams-charts-response] [%s] [%s] [%s]", deviceId, stream, jsonResponse));
                } catch (IOException e) {
                    logger.error(String.format("[createStreams-error] [%s] [%s]", deviceId, stream), e);
                }
            });

            return null;
        }

        return "OK";
    }
}
