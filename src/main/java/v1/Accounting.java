package v1;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.jboss.logging.Logger;

import javax.ws.rs.core.Application;

@QuarkusMain
@OpenAPIDefinition(info = @Info(title = "Accounting API", version = "1.0.0", contact = @Contact(name = "Example API Support", url = "http://exampleurl.com/contact", email = "techsupport@example.com"), license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html")), components = @Components(securitySchemes = @SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", in = SecuritySchemeIn.HEADER, securitySchemeName = "Authorization")))
public class Accounting extends Application implements QuarkusApplication {

	private static final Logger LOG = Logger.getLogger(Accounting.class);

	@Override
	public int run(String... args) throws Exception {

		LOG.info("Running Accounting Application...");
		Quarkus.waitForExit();

		return 0;
	}
}
