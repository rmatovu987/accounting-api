package v1.accounting.controllers.ledgers.endpoints;

import com.itextpdf.text.DocumentException;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import v1.accounting.controllers.ledgers.service.CommonLedgerQuerries;
import v1.accounting.controllers.ledgers.service.LedgerService;
import v1.accounting.controllers.ledgers.service.payload.COAbalance;
import v1.accounting.controllers.ledgers.service.payload.LedgerHistory;
import v1.accounting.controllers.ledgers.service.payload.LedgerRequest;
import v1.accounting.domains.Ledger;
import v1.authentication.domains.Authenticator;
import v1.statics.ResponseMessage;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Path("ledgers")
@Produces("application/json")
@Consumes("application/json")
@Tag(name = "Ledgers", description = "Manage ledgers")
@SecurityRequirement(name = "Authorization")
public class LedgerController {

    @Inject
    LedgerService service;

    @Inject
    CommonLedgerQuerries queries;

    @POST
    @Transactional
    @Operation(summary = "Save a new ledger", description = "This will create a new ledger.")
    @APIResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(implementation = Ledger.class)))
    @APIResponse(description = "Ledger with the same name already exists", responseCode = "409")
    @APIResponse(description = "Invalid parent ledger selected", responseCode = "404")
    public Response create(LedgerRequest request, @Context SecurityContext ctx) {
        Principal caller = ctx.getUserPrincipal();
        String user = caller == null ? "anonymous" : caller.getName();

        Authenticator auth = Authenticator.findByName(user);
        if (auth != null) {
            return Response.ok(new ResponseMessage("Ledger created!",
                    service.saveLedger(request, auth.business))).build();
        }
        throw new WebApplicationException("Unauthorized!", 401);
    }

    @PUT
    @Transactional
    @Path("/{id}")
    @Operation(summary = "Update an existing ledger", description = "This will update the ledger details.")
    @APIResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(implementation = Ledger.class)))
    @APIResponse(description = "Ledger with that name already exists", responseCode = "409")
    @APIResponse(description = "Invalid ledger selected", responseCode = "404")
    @APIResponse(description = "Invalid parent ledger selected", responseCode = "404")
    public Response update(@PathParam("id") Long id, LedgerRequest request, @Context SecurityContext ctx) {
        Principal caller = ctx.getUserPrincipal();
        String user = caller == null ? "anonymous" : caller.getName();

        Authenticator auth = Authenticator.findByName(user);
        if (auth != null) {
            return Response.ok(new ResponseMessage("Ledger updated",
                    service.updateLedger(id, request, auth.business))).build();
        }
        throw new WebApplicationException("Unauthorized!", 401);
    }

    @GET
    @Transactional
    @Path("/{id}")
    @Operation(summary = "Get details of a ledger", description = "This will return the details of a ledger.")
    @APIResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(implementation = Ledger.class)))
    @APIResponse(description = "Invalid ledger selected", responseCode = "404")
    public Response getDetails(@PathParam("id") Long id, @Context SecurityContext ctx) {
        Principal caller = ctx.getUserPrincipal();
        String user = caller == null ? "anonymous" : caller.getName();

        Authenticator auth = Authenticator.findByName(user);
        if (auth != null) {
            return Response.ok(new ResponseMessage("Ledger details fetched",
                    service.getDetails(id))).build();
        }
        throw new WebApplicationException("Unauthorized!", 401);
    }

    @GET
    @Transactional
    @Path("/chartofaccounts")
    @Operation(summary = "Returns ledgers fit to show on the chart of accounts", description = "Optional parameter can be passed to fetch the "
            + "chart for just the type,subtype or category")
    @APIResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = Ledger.class)))
    public Response getChart(@QueryParam("categoryId") Long categoryId, @Context SecurityContext ctx) {
        Principal caller = ctx.getUserPrincipal();
        String user = caller == null ? "anonymous" : caller.getName();

        Authenticator auth = Authenticator.findByName(user);
        if (auth != null) {
            return Response.ok(new ResponseMessage("Successfully fetched!",
                    service.getChart(auth.business, categoryId))).build();
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
            return Response.ok(new ResponseMessage("Successfully fetched!",
                    service.getPostable(auth.business, categoryId))).build();
        }
        throw new WebApplicationException("Unauthorized!", 401);
    }

    @GET
    @Transactional
    @Path("/ledger-history/{id}")
    @Operation(summary = "Returns ledger history", description = "")
    @APIResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = LedgerHistory.class)))
    public Response getpostableOpening(@PathParam("id") Long id, @QueryParam("startDate") Long startDate, @QueryParam("branchId") Long branchId,
                                       @QueryParam("endDate") Long endDate, @Context SecurityContext ctx) throws DocumentException, IOException {
        Principal caller = ctx.getUserPrincipal();
        String user = caller == null ? "anonymous" : caller.getName();

        Authenticator auth = Authenticator.findByName(user);
        if (auth != null) {
            return Response.ok(new ResponseMessage("Successfully fetched!",
                    service.getLedgerHistory(id, startDate, endDate, auth.business))).build();
        }
        throw new WebApplicationException("Unauthorized!", 401);
    }

    @GET
    @Transactional
    @Path("/chartledgerbalance/{id}")
    @Operation(summary = "Returns balance on ledger in chart of accounts", description = "")
    @APIResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(implementation = COAbalance.class)))
    public Response getbalance(@PathParam("id") Long id, @Context SecurityContext ctx) {
        Principal caller = ctx.getUserPrincipal();
        String user = caller == null ? "anonymous" : caller.getName();

        Authenticator auth = Authenticator.findByName(user);
        if (auth != null) {
            Ledger ledger = Ledger.findById(id);
            if (ledger == null)
                throw new WebApplicationException("Invalid ledger selected", 404);
            return Response.ok(new ResponseMessage("Successfully fetched!",
                    queries.getBalanceChart(ledger, auth.business))).build();
        }
        throw new WebApplicationException("Unauthorized!", 401);
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Operation(summary = "Delete a ledger", description = "This will delete a ledger.")
    @APIResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(implementation = Ledger.class)))
    public Response get(@PathParam("id") Long id, @Context SecurityContext ctx) {
        Principal caller = ctx.getUserPrincipal();
        String user = caller == null ? "anonymous" : caller.getName();

        Authenticator auth = Authenticator.findByName(user);
        if (auth != null) {
            service.deleteOne(id);
            return Response.ok(new ResponseMessage("Successfully deleted!")).build();
        }
        throw new WebApplicationException("Unauthorized!", 401);
    }
}
