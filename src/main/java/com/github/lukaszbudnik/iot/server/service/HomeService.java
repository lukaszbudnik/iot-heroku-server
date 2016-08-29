package com.github.lukaszbudnik.iot.server.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Path("/")
public class HomeService {

    @GET
    public Response redirectToStats() {
        return Response.seeOther(UriBuilder.fromPath("/web/stats.html").build()).build();
    }
}
