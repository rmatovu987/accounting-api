package v1.accounting.controllers.ledgers.service.payload;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotNull;

public class LedgerRequest {
    
    @Schema(required = false, example = "1")
    public Long categoryId;

    @NotNull
    @Schema(required = true, example = "Accounts Receivables")
    public String name;

    @NotNull
    @Schema(required = true, example = "Accounts Receivables")
    public String description;

    @Schema(required = false, example = "1")
    public Long parentLedgerId;
    
}
