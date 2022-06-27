package v1.configurations.handler;

import javax.json.Json;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

public class ErrorMapper {

	@Provider
	public static class ErrorMapperx implements ExceptionMapper<Exception> {

		@Override
		public Response toResponse(Exception exception) {
			int code = 500;
			if (exception instanceof WebApplicationException) {
				code = ((WebApplicationException) exception).getResponse().getStatus();
			}

			exception.printStackTrace();

			return Response.status(code)
					.entity(Json.createObjectBuilder().add("error", exception.getMessage()).add("code", code).build())
					.build();
		}

	}
}
