package v1.authentication.controllers.services.payloads;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.Email;

public class RegistrationRequest {

    @Schema(required = true)
    public String businessName;

    @Schema(required = true)
    public String contact;

    @Schema(required = true)
    public String address;

    @Email
    @Schema(required = true)
    public String email;

    @Schema(required = true)
    public String logo;
}
