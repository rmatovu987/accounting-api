package v1.authentication.controllers.services;

import v1.authentication.controllers.services.payloads.RegistrationRequest;
import v1.authentication.domains.Authenticator;
import v1.authentication.domains.Business;

import javax.enterprise.context.ApplicationScoped;
import java.util.Random;

@ApplicationScoped
public class AuthService {

    public Authenticator register(RegistrationRequest request) {

        Business business = new Business(request.businessName, request.address, request.contact, request.email, request.logo);
        business.persist();

        String api = randomString(5) + "." + randomString(15);

        Authenticator auth = new Authenticator(api, business);
        auth.persist();

        return auth;
    }

    private String randomString(int length) {

        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        StringBuilder buffer = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomLimitedInt = leftLimit + (int) (new Random().nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }

        return buffer.toString();
    }
}
