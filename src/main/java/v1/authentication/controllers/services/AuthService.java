package v1.authentication.controllers.services;

import v1.accounting.controllers.initializer.AccountingInitializer;
import v1.authentication.controllers.services.payloads.RegistrationRequest;
import v1.authentication.domains.Authenticator;
import v1.authentication.domains.Business;
import v1.configurations.security.JwtUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.WebApplicationException;

@ApplicationScoped
public class AuthService {

    @Inject
    JwtUtils jwtUtils;

    @Inject
    AccountingInitializer initializer;

    public Authenticator register(RegistrationRequest request) {

        Business business = saveBusiness(request);
        Authenticator auth = saveAuthenticator(business);

        initializer.initializeAccounting(business);

        return auth;
    }

    @Transactional
    public Business saveBusiness(RegistrationRequest request){
        Business exists = Business.findByName(request.businessName);
        if(exists != null) throw new WebApplicationException("Business name already taken!", 409);

        Business business = new Business(request.businessName, request.address, request.contact, request.email, request.logo);
        business.persist();

        return business;
    }

    @Transactional
    public Authenticator saveAuthenticator(Business business){
        String api = jwtUtils.generateJwtToken(business);

        Authenticator auth = new Authenticator(api, business);
        auth.persist();

        return auth;
    }
}
