package com.github.lukaszbudnik.iot.server.service;

import com.att.m2x.java.M2XClient;
import com.att.m2x.java.M2XDevice;
import com.att.m2x.java.M2XResponse;
import com.att.m2x.java.M2XStream;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
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
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1/telemetry")
public class TelemetryService {

    private static final Logger logger = LoggerFactory.getLogger(TelemetryService.class);

    private final M2XClient m2x;

    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);

    @Inject
    public TelemetryService(@Named("m2x.apiKey") String m2xApiKey) {
        m2x = new M2XClient(m2xApiKey);
    }

    @POST
    @Path("/{device}/{stream}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Timed
    public Response update(@PathParam("device") String device, @PathParam("stream") String stream, @FormParam("v") List<String> values, @FormParam("t") List<String> timestamps) throws Exception {
        sendToM2x(device, stream, values, timestamps);

        return Response.accepted().build();
    }

    private void sendToM2x(@PathParam("device") String device, @PathParam("stream") String stream, @FormParam("v") List<String> values, @FormParam("t") List<String> timestamps) {
        executorService.submit(() -> {
            M2XDevice m2xdevice = m2x.device(device);
            M2XStream m2xstream = m2xdevice.stream(stream);

            // build the payload
            List<Map<String, String>> payload = new ArrayList<>(values.size());
            for (int i = 0; i < values.size(); i++) {
                payload.add(ImmutableMap.of("value", values.get(i), "timestamp", timestamps.get(i)));
            }

            String json = M2XClient.jsonSerialize(ImmutableMap.of("values", payload));
            logger.info(String.format("[update-request] [%s] [%s] [%s]", device, stream, json));

            try {
                M2XResponse response = m2xstream.postValues(json);
                String jsonResponse = response.raw;
                logger.info(String.format("[update-response] [%s] [%s] [%s]", device, stream, jsonResponse));
            } catch (IOException e) {
                logger.error(String.format("[update-error] [%s] [%s]", device, stream), e);
            }

        });
    }

}
