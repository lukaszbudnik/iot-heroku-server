package com.github.lukaszbudnik.iot.server.service;

import com.att.m2x.java.M2XClient;
import com.att.m2x.java.M2XDevice;
import com.att.m2x.java.M2XResponse;
import com.codahale.metrics.annotation.Timed;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/v1/stats")
public class StatsService {

    private static final Logger logger = LoggerFactory.getLogger(StatsService.class);

    private final M2XClient m2x;

    @Inject
    public StatsService(@Named("m2x.apiKey") String m2xApiKey) {
        m2x = new M2XClient(m2xApiKey);
    }

    @GET
    @Path("/{device}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response stats(@PathParam("device") String device) throws IOException {
        logger.info("Stats " + device);
        M2XDevice m2xdevice = m2x.device(device);
        JSONObject json = m2xdevice.details().json();
        M2XResponse charts = m2x.makeRequest("GET", "/charts", "?device=" + device, null);
        if (charts.success()) {
            json.put("charts", charts.json().get("charts"));
        }
        return Response.ok(json.toString()).build();
    }

}
