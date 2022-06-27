package v1.accounting.controllers.journals.service.payload;

import v1.accounting.domains.Journal;
import v1.accounting.domains.Ledger;
import v1.authentication.domains.Business;
import v1.statics.TimeConverter;

import java.math.BigDecimal;
import java.time.Instant;

public class JournalEntryData {

	public Long id;

	public Long date;

	public String notes;

	public BigDecimal debit;

	public BigDecimal credit;

	public Long entryTime;

	// relationships
	public Journal journal;

	public Ledger ledger;

	public JournalEntryData() {
	}

	public JournalEntryData(Long date, String notes, BigDecimal debit, BigDecimal credit, Journal journal, Ledger ledger) {
		this.date = date;
		this.notes = notes;
		this.debit = debit;
		this.credit = credit;
		this.journal = journal;
		this.ledger = ledger;
		this.entryTime = TimeConverter.EpochMillis_Now();
	}

}
