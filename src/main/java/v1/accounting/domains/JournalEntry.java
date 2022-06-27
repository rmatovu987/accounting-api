package v1.accounting.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import v1.authentication.domains.Business;
import v1.statics.TimeConverter;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.util.List;

@Entity
public class JournalEntry extends PanacheEntity {

    public Long date;

    public String notes;

    @Column(nullable = false, precision = 26, scale = 8)
    public BigDecimal debit;

    @Column(nullable = false, precision = 26, scale = 8)
    public BigDecimal credit;

    public Long entryTime = TimeConverter.EpochMillis_Now();

    // relationships
    @ManyToOne
    @JsonbTransient
    @JoinColumn(nullable = false)
    public Journal journal;

    @ManyToOne
    @JoinColumn(nullable = false)
    public Ledger ledger;

    @ManyToOne
    @JsonbTransient
    @JoinColumn(nullable = false)
    public Business business;

    public JournalEntry() {
    }

    public JournalEntry(Long date, String notes, BigDecimal debit, BigDecimal credit, Journal journal, Ledger ledger,
                        Business business) {
        this.date = date;
        this.notes = notes;
        this.debit = debit;
        this.credit = credit;
        this.journal = journal;
        this.ledger = ledger;
        this.business = business;
    }

    public BigDecimal getDebit() {
        return this.debit.abs();
    }

    public BigDecimal getCredit() {
        return this.credit.abs();
    }

    public static JournalEntry getOne(Long date, BigDecimal debit, BigDecimal credit, Ledger ledger, Business business) {
        return find(
                "(?1 is null and date=?1) and (?2 is null or debit=?2) and (?3 is null or credit=?3) and " +
                        "(?4 is null or ledger=?4) and business=?5",
                date, debit, credit, ledger, business).firstResult();
    }

    public static List<JournalEntry> search(Long date1, Long date2, Ledger ledger, Journal journal, Business business) {
        return list(
                "business=?1 and (?2 is null or ledger=?2) and (?3 is null or journal=?3) and (date between ?4 and ?5)",
                business, ledger, journal, date1, date2);
    }
}
