package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiInfo() {

        Map<String, Object> apiInfo = new HashMap<>();

        // API version info
        apiInfo.put("version", "1.0");
        apiInfo.put("name", "Smart Campus API");
        apiInfo.put("description", "Sensor and Room Management API");
        apiInfo.put("contact", "admin@smartcampus.ac.uk");

        // Available resource links (HATEOAS)
        Map<String, String> links = new HashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        apiInfo.put("resources", links);

        return Response.ok(apiInfo).build();
    }
}