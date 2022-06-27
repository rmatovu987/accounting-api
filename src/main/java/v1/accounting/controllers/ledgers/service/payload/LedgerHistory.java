package v1.accounting.controllers.ledgers.service.payload;

import java.math.BigDecimal;

public class LedgerHistory {

    public Long id;

    public BigDecimal debit;

    public BigDecimal credit;

    public Long date;

    public String notes;

    public String branch;

    public String currency;

    public Long journalId;

    public BigDecimal balance;

    public LedgerHistory() {
    }

    public LedgerHistory(Long id, BigDecimal debit, BigDecimal credit, Long date, String notes, String branch, String currency,
                         Long journalId, BigDecimal balance) {
        this.id = id;
        this.debit = debit;
        this.credit = credit;
        this.date = date;
        this.notes = notes;
        this.branch = branch;
        this.currency = currency;
        this.journalId = journalId;
        this.balance = balance;
    }
}
