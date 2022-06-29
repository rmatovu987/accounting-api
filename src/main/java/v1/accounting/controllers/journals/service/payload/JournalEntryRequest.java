package v1.accounting.controllers.journals.service.payload;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class JournalEntryRequest {
    @Schema(required = false, example="1")
    public Long id;

    @Schema(required = false, example = "Buying stuff")
    public String notes;

    @Schema(required = true, example = "1200")
    public BigDecimal debit;

    @NotNull
    @Schema(required = true, example = "1200")
    public BigDecimal credit;
    
    @Schema(required = true, example = "1")
    public Long ledgerId;

    @Schema(required = true, example = "1")
    public Long journalId;

    public Boolean confirmation = Boolean.FALSE;


}
