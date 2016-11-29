package com.github.lukaszbudnik.iot.server.service;

import com.github.lukaszbudnik.iot.server.internal.DeviceService;
import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// take remote device ids and returns m2x device ids
@Path("/v1/registry")
public class RegistryService {

    private static final Logger logger = LoggerFactory.getLogger(RegistryService.class);

    private final DeviceService deviceService;

    @Inject
    public RegistryService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @POST
    @Path("/devices")
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@FormParam("deviceId") String deviceId) throws IOException {
        String backendDeviceId = deviceService.createDevice(deviceId);

        logger.info(String.format("Registered %s as backend device %s", deviceId, backendDeviceId));

        return Response.ok().entity(ImmutableMap.of("message", String.format("Device successfully created"), "deviceId", deviceId)).build();
    }

    @POST
    @Path("/devices/{deviceId}/streams")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createStream(@PathParam("deviceId") String deviceId, @FormParam("streams") List<String> streams, @FormParam("units") List<String> units, @FormParam("types") List<String> types) throws IOException {
        try {
            deviceService.createStreams(deviceId, streams, units, types);
        } catch (DeviceNotFoundException dnfe) {
            return Response.status(Response.Status.NOT_FOUND).entity(dnfe.jsonError()).build();
        }

        return Response.accepted().build();
    }

    @GET
    @Path("/devices/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response device(@PathParam("deviceId") String deviceId) throws IOException {
        try {
            JSONObject device = deviceService.getDevice(deviceId);
            return Response.ok(device.toString()).build();
        } catch (DeviceNotFoundException dnfe) {
            return Response.status(Response.Status.NOT_FOUND).entity(dnfe.jsonError()).build();
        }
    }

}
