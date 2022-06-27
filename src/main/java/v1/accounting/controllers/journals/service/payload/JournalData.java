package v1.accounting.controllers.journals.service.payload;

import v1.statics.TimeConverter;
import v1.statics._StatusTypes_Enum;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class JournalData {

    public Long id;

    public Long date;


    public String journalNo;

    public Boolean isSystemGenerated;


    public String type; //manual, integrated, system


    public String journalClass;


    public String reference;


    public String status;


    public BigDecimal amount = BigDecimal.ZERO;

    public String notes;

    public Long entryTime;

    /// mappers
    public List<JournalEntryData> entries = new ArrayList<>();


    public JournalData() {
    }

    public JournalData(Long date, String journalNo, Boolean isSystemGenerated, String type, String journalClass, String reference, String notes) {
        this.date = date;
        this.journalNo = journalNo;
        this.isSystemGenerated = isSystemGenerated;
        this.type = type;
        this.journalClass = journalClass;
        this.reference = reference;
        this.notes = notes;
        this.entryTime = TimeConverter.EpochMillis_Now();
        this.status = _StatusTypes_Enum.DRAFT.toString();
        this.entries = new ArrayList<>();
    }

}
