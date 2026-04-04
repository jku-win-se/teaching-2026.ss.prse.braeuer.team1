package at.jku.se.resource;

import at.jku.se.iot.IoTIntegrationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

/**
 * REST endpoint exposing IoT integration status (FR-18).
 */
@ApplicationScoped
@Path("/api/iot")
@Produces(MediaType.APPLICATION_JSON)
public class IoTResource {

    @Inject
    IoTIntegrationService iotService;

    @GET
    @Path("/status")
    public Response getStatus() {
        return Response.ok(Map.of(
                "connected", iotService.isConnected(),
                "protocol", iotService.getProtocolName()
        )).build();
    }
}
