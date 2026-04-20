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

    /** Creates the resource; intended for CDI instantiation. */
    public IoTResource() {}

    @Inject
    IoTIntegrationService iotService;

    /**
     * Returns the current IoT integration status.
     *
     * @return a JSON response with {@code connected} flag and active {@code protocol} name
     */
    @GET
    @Path("/status")
    public Response getStatus() {
        return Response.ok(Map.of(
                "connected", iotService.isConnected(),
                "protocol", iotService.getProtocolName()
        )).build();
    }
}
