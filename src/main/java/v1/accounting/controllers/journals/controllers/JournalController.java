package v1.accounting.controllers.journals.controllers;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import v1.accounting.controllers.journals.service.JournalService;
import v1.accounting.controllers.journals.service.payload.JournalRequest;
import v1.accounting.controllers.ledgers.service.LedgerService;
import v1.accounting.domains.Journal;
import v1.accounting.domains.Ledger;
import v1.authentication.domains.Authenticator;
import v1.statics.ResponseMessage;
import v1.statics._StatusTypes_Enum;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

@Path("journals")
@Produces("application/json")
@Consumes("application/json")
@Tag(name = "Journals", description = "Manage journals")
@SecurityRequirement(name = "Authorization")
public class JournalController {

	@Inject
	JournalService service;

	@Inject
	LedgerService ledgerService;

	@POST
	@Transactional
	@Operation(summary = "Save a new journal", description = "This will create a new journal.")
	@APIResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(implementation = Journal.class)))
	@APIResponse(description = "Total Credits must be equal to Total Debits", responseCode = "403")
	@APIResponse(description = "A journal with the same details already exists! Are you sure you want proceed?", responseCode = "409")
	public Response create(JournalRequest request, @Context SecurityContext ctx) {
		Principal caller = ctx.getUserPrincipal();
		String user = caller == null ? "anonymous" : caller.getName();

		Authenticator auth = Authenticator.findByName(user);
		if (auth != null) {
			return Response
					.ok(new ResponseMessage("Saved successfully!", service.createJournal(request, auth.business)))
					.build();
		}
		throw new WebApplicationException("Unauthorized!", 401);
	}

	@PUT
	@Transactional
	@Path("/{id}")
	@Operation(summary = "Update an existing journal", description = "This will update the journal details.")
	@APIResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(implementation = Journal.class)))
	@APIResponse(description = "Invalid journal selected", responseCode = "404")
	public Response update(@PathParam("id") Long id, JournalRequest request, @Context SecurityContext ctx) {
		Principal caller = ctx.getUserPrincipal();
		String user = caller == null ? "anonymous" : caller.getName();

		Authenticator auth = Authenticator.findByName(user);
		if (auth != null) {
			return Response.ok(new ResponseMessage("Updated successfully!",
					service.updateJournalDetails(id, request, auth.business))).build();
		}
		throw new WebApplicationException("Unauthorized!", 401);
	}

	@PUT
	@Transactional
	@Path("/approve/{id}")
	@Operation(summary = "Approve a journal", description = "This will approve the journal.")
	@APIResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(implementation = Journal.class)))
	@APIResponse(description = "Invalid journal selected", responseCode = "404")
	@APIResponse(description = "Only draft journals can be approved", responseCode = "403")
	public Response approveJournal(@PathParam("id") Long id, @Context SecurityContext ctx) {
		Principal caller = ctx.getUserPrincipal();
		String user = caller == null ? "anonymous" : caller.getName();

		Authenticator auth = Authenticator.findByName(user);
		if (auth != null) {
			return Response
					.ok(new ResponseMessage("Approved successfully!", service.approveJournal(id, auth.business)))
					.build();
		}
		throw new WebApplicationException("Unauthorized!", 401);
	}

	@PUT
	@Transactional
	@Path("/decline/{id}")
	@Operation(summary = "Decline a journal", description = "This will decline the journal.")
	@APIResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(implementation = Journal.class)))
	@APIResponse(description = "Invalid journal selected", responseCode = "404")
	@APIResponse(description = "Only approved journals can be declined", responseCode = "403")
	public Response declineJournal(@PathParam("id") Long id, @Context SecurityContext ctx) {
		Principal caller = ctx.getUserPrincipal();
		String user = caller == null ? "anonymous" : caller.getName();

		Authenticator auth = Authenticator.findByName(user);
		if (auth != null) {
			return Response
					.ok(new ResponseMessage("Declined successfully!", service.declineJournal(id, auth.business)))
					.build();
		}
		throw new WebApplicationException("Unauthorized!", 401);
	}

	@PUT
	@Transactional
	@Path("/publish/{id}")
	@Operation(summary = "Publish a journal", description = "This will publish the journal.")
	@APIResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(implementation = Journal.class)))
	@APIResponse(description = "Invalid journal selected", responseCode = "404")
	@APIResponse(description = "Only approved journals can be published", responseCode = "403")
	public Response publishJournal(@PathParam("id") Long id, @Context SecurityContext ctx) {
		Principal caller = ctx.getUserPrincipal();
		String user = caller == null ? "anonymous" : caller.getName();

		Authenticator auth = Authenticator.findByName(user);
		if (auth != null) {
			return Response
					.ok(new ResponseMessage("Published successfully!", service.publishJournal(id, auth.business)))
					.build();
		}
		throw new WebApplicationException("Unauthorized!", 401);
	}

	@PUT
	@Transactional
	@Path("/reject/{id}")
	@Operation(summary = "Reject a journal", description = "This will reject the journal.")
	@APIResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(implementation = Journal.class)))
	@APIResponse(description = "Invalid journal selected", responseCode = "404")
	@APIResponse(description = "Only draft journals can be rejected", responseCode = "403")
	public Response rejectJournal(@PathParam("id") Long id, @Context SecurityContext ctx) {
		Principal caller = ctx.getUserPrincipal();
		String user = caller == null ? "anonymous" : caller.getName();

		Authenticator auth = Authenticator.findByName(user);
		if (auth != null) {
			return Response
					.ok(new ResponseMessage("Rejected successfully!", service.rejectJournal(id, auth.business)))
					.build();
		}
		throw new WebApplicationException("Unauthorized!", 401);
	}

	@PUT
	@Transactional
	@Path("/undecline/{id}")
	@Operation(summary = "Undecline a journal", description = "This will undecline the journal.")
	@APIResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(implementation = Journal.class)))
	@APIResponse(description = "Invalid journal selected", responseCode = "404")
	@APIResponse(description = "Only declined journals can be undeclined", responseCode = "403")
	public Response undeclineJournal(@PathParam("id") Long id, @Context SecurityContext ctx) {
		Principal caller = ctx.getUserPrincipal();
		String user = caller == null ? "anonymous" : caller.getName();

		Authenticator auth = Authenticator.findByName(user);
		if (auth != null) {
			return Response
					.ok(new ResponseMessage("Successfully undeclined", service.undeclineJournal(id, auth.business)))
					.build();
		}
		throw new WebApplicationException("Unauthorized!", 401);
	}

	@PUT
	@Transactional
	@Path("/unreject/{id}")
	@Operation(summary = "Unreject a journal", description = "This will unreject the journal.")
	@APIResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(implementation = Journal.class)))
	@APIResponse(description = "Invalid journal selected", responseCode = "404")
	@APIResponse(description = "Only rejected journals can be unrejected", responseCode = "403")
	public Response unrejectJournal(@PathParam("id") Long id, @Context SecurityContext ctx) {
		Principal caller = ctx.getUserPrincipal();
		String user = caller == null ? "anonymous" : caller.getName();

		Authenticator auth = Authenticator.findByName(user);
		if (auth != null) {
			return Response
					.ok(new ResponseMessage("Successfully unrejected!", service.unrejectJournal(id, auth.business)))
					.build();
		}
		throw new WebApplicationException("Unauthorized!", 401);
	}

	@GET
	@Transactional
	@Path("/{id}")
	@Operation(summary = "Get details of a journal", description = "This will return the details of a journal.")
	@APIResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(implementation = Journal.class)))
	@APIResponse(description = "Invalid journal selected", responseCode = "404")
	public Response getDetails(@PathParam("id") Long id, @Context SecurityContext ctx) {
		Principal caller = ctx.getUserPrincipal();
		String user = caller == null ? "anonymous" : caller.getName();

		Authenticator auth = Authenticator.findByName(user);
		if (auth != null) {
			return Response.ok(new ResponseMessage("Successful!", service.getJournalDetails(id)))
					.build();
		}
		throw new WebApplicationException("Unauthorized!", 401);
	}

	@GET
	@Transactional
	@Operation(summary = "Returns journals", description = "This will return journals basing on the sent query parameters.")
	@APIResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = Journal.class)))
	public Response get(@QueryParam("startDate") Long startDate, @QueryParam("endDate") Long endDate,
			@QueryParam("isSystemGenerated") Boolean isSystemGenerated,
			@QueryParam("reference") String reference, @QueryParam("status") _StatusTypes_Enum status,
			@Context SecurityContext ctx) {
		Principal caller = ctx.getUserPrincipal();
		String user = caller == null ? "anonymous" : caller.getName();

		Authenticator auth = Authenticator.findByName(user);
		if (auth != null) {
			return Response.ok(new ResponseMessage("Fetched successfully!",
					service.getJournals(startDate, endDate, isSystemGenerated, reference, status, auth.business)))
					.build();
		}

		throw new WebApplicationException("Unauthorized!", 401);
	}

	@GET
	@Transactional
	@Path("/postable")
	@Operation(summary = "Returns ledgers that can be posted on in journals", description = "Optional parameter can be passed to fetch the "
			+ "chart for just the type,subtype or category or even parent ladger")
	@APIResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = Ledger.class)))
	public Response getpostable(@QueryParam("categoryId") Long categoryId, @Context SecurityContext ctx) {
		Principal caller = ctx.getUserPrincipal();
		String user = caller == null ? "anonymous" : caller.getName();

		Authenticator auth = Authenticator.findByName(user);
		if (auth != null) {
			return Response.ok(new ResponseMessage("Fetched successfully!",
					ledgerService.getPostable(auth.business, categoryId))).build();
		}
		throw new WebApplicationException("Unauthorized!", 401);
	}

	@GET
	@Transactional
	@Path("/journal-type-enums")
	@Operation(summary = "Get journal type enums", description = "")
	@APIResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = String.class)))
	public Response getpostable(@Context SecurityContext ctx) {
		Principal caller = ctx.getUserPrincipal();
		String user = caller == null ? "anonymous" : caller.getName();

		Authenticator auth = Authenticator.findByName(user);
		if (auth != null) {
			return Response.ok(new ResponseMessage("Fetched successfully!", service.getJournalTypes())).build();
		}
		throw new WebApplicationException("Unauthorized!", 401);
	}

	@GET
	@Transactional
	@Path("/journal-status-enums")
	@Operation(summary = "Get journal status enums", description = "")
	@APIResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = String.class)))
	public Response getpostables(@Context SecurityContext ctx) {
		Principal caller = ctx.getUserPrincipal();
		String user = caller == null ? "anonymous" : caller.getName();

		Authenticator auth = Authenticator.findByName(user);
		if (auth != null) {
			return Response.ok(new ResponseMessage("Fetched successfully!", service.getJournalStatuses())).build();
		}
		throw new WebApplicationException("Unauthorized!", 401);
	}

	@DELETE
	@Transactional
	@Path("/{id}")
	@Operation(summary = "Delete a journal", description = "This will delete a journal.")
	@APIResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(implementation = Journal.class)))
	public Response get(@PathParam("id") Long id, @Context SecurityContext ctx) {
		Principal caller = ctx.getUserPrincipal();
		String user = caller == null ? "anonymous" : caller.getName();

		Authenticator auth = Authenticator.findByName(user);
		if (auth != null) {
			return Response
					.ok(new ResponseMessage("Deleted successfully!", service.deleteJournal(id, auth.business)))
					.build();
		}
		throw new WebApplicationException("Unauthorized!", 401);
	}
}
