package v1.authentication.controllers.services;

import v1.authentication.controllers.services.payloads.RegistrationRequest;
import v1.authentication.domains.Authenticator;
import v1.authentication.domains.Business;
import v1.configurations.security.JwtUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

@ApplicationScoped
public class AuthService {

    @Inject
    JwtUtils jwtUtils;

    public Authenticator register(RegistrationRequest request) {

        Business exists = Business.findByName(request.businessName);
        if(exists != null) throw new WebApplicationException("Business name already taken!", 409);

        Business business = new Business(request.businessName, request.address, request.contact, request.email, request.logo);
        business.persist();

        String api = jwtUtils.generateJwtToken(business);

        Authenticator auth = new Authenticator(api, business);
        auth.persist();

        return auth;
    }
}
