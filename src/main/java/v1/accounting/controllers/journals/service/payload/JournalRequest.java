package v1.accounting.controllers.journals.service.payload;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import java.util.List;

public class JournalRequest {

    @NotNull
    @Schema(required = true, example = "123456789")
    public Long date;

    @NotNull
    @Schema(required = true, example = "REF001")
    public String reference;
    
    @Schema(required = false, example = "Transaction between A and B")
    public String notes;
    
    @NotNull
    @Schema(required = true)
    public List<JournalEntryRequest> entries;

    @NotNull
    @Schema(required = true, example = "true")
    public Boolean approve;

    @NotNull
    @Schema(required = true, example = "true")
    public Boolean publish;

}
