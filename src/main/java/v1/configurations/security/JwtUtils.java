package v1.configurations.security;

import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtSignatureException;
import io.vertx.core.http.HttpServerRequest;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import v1.authentication.domains.Business;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@ApplicationScoped
public class JwtUtils {

	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

	@Inject
	JWTParser parser;

	@Inject
	JsonWebToken jwt;

	private final String jwtSecret = "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJTdG9ybXBhdGgiLCJzdWIiOiJtc2lsdmVybWFuIiwiaGFzTW90b3JjeWNsZSI6dHJ1ZX0.OnyDs-zoL3-rw1GaSl_KzZzHK9GoiNocu-YwZ_nQNZU";

	public String generateJwtToken(Business business) {
		return Jwt.subject(business.businessName).issuedAt(new Date().toInstant())
				.expiresAt(LocalDate.now().plusYears(1).atStartOfDay().toInstant(ZoneOffset.UTC))
				.upn(business.businessName).issuer("Accounting API Server").signWithSecret(jwtSecret);

	}

	public String getBusinessFromJwtToken(String token) {

		try {
			jwt = parser.verify(token, jwtSecret);

			return jwt.getSubject();

		} catch (ParseException e) {
			logger.error("Parse Exception: {}", e.getMessage());
			throw new WebApplicationException("Access Denied.", 401);

		} catch (Exception e) {
			logger.error("Null Exception: {}", e.getMessage());
			throw new WebApplicationException("Access Denied.", 401);
		}

	}

	public boolean validateJwtToken(String authToken, HttpServerRequest request) {

		try {
			jwt = parser.verify(authToken, jwtSecret);

			return true;

		} catch (JwtSignatureException e) {
			logger.error("Invalid JWT signature: {}", e.getMessage());
			return false;

		} catch (IllegalArgumentException e) {
			logger.error("JWT claims string is empty: {}", e.getMessage());
			return false;

		} catch (ParseException e) {
			logger.error("Parse Exception: {}", e.getMessage());
			return false;

		} catch (Exception e) {
			logger.error("Null Exception: {}", e.getMessage());
			return false;
		}

	}

}
