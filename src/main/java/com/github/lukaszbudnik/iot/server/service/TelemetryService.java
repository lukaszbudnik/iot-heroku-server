package com.github.lukaszbudnik.iot.server.service;

import com.codahale.metrics.annotation.Timed;
import com.github.lukaszbudnik.iot.server.internal.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1/telemetry")
public class TelemetryService {

    private static final Logger logger = LoggerFactory.getLogger(TelemetryService.class);

    private final DeviceService deviceService;

    @Inject
    public TelemetryService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @POST
    @Path("/{deviceId}/{stream}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response update(@PathParam("deviceId") String deviceId, @PathParam("stream") String stream, @FormParam("v") List<String> values, @FormParam("t") List<String> timestamps) throws Exception {
        try {
            deviceService.sendDeviceMetrics(deviceId, stream, values, timestamps);
        } catch (DeviceNotFoundException dnfe) {
            return Response.status(Response.Status.NOT_FOUND).entity(dnfe.jsonError()).build();
        }

        return Response.accepted().build();
    }

}
