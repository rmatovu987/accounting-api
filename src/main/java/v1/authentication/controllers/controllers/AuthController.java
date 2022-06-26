package v1.authentication.controllers.controllers;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import v1.authentication.controllers.services.AuthService;
import v1.authentication.controllers.services.payloads.RegistrationRequest;
import v1.authentication.domains.Authenticator;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("authentication")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name="Authentication", description="Authentication endpoints")
public class AuthController {

    @Inject
    AuthService service;

    @POST
    @Transactional
    @Operation(summary="Registration", description="Register your business and get an api key to attach in your headers")
    @APIResponse(description = "Registered", responseCode = "200", content= @Content(schema = @Schema(implementation = Authenticator.class)))
    public Response register(RegistrationRequest request){
        return Response.ok(service.register(request)).status(200).build();
    }
}
